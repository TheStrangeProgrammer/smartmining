package smartmining.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import smartmining.*;

import smartmining.campaign.intel.reports.SMIntelMiningReport;
import smartmining.events.SMEvents;


import java.awt.*;
import java.util.Random;
import java.util.List;

public class SMMiningAbility extends BaseToggleAbility {

    private final IntervalUtil miningAsteroidTracker = new IntervalUtil(1f, 1f);
    private final IntervalUtil miningEventTracker = new IntervalUtil(2f, 5f);
    private final IntervalUtil miningReportTracker = new IntervalUtil(3f, 3f);


    //private static String commodity
    private Random random = new Random();
    @Override
    protected void activateImpl() {
        miningAsteroidTracker.setElapsed(1f);
        miningEventTracker.setElapsed(1f);
        miningReportTracker.setElapsed(1f);
        SMIntelMiningReport.commodities.clear();
    }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        if(fleet.getCargo().getSpaceUsed()>=fleet.getCargo().getMaxCapacity()) deactivate();
        disableIncompatible();
        String terrain = SMMisc.getTerrainId(fleet);
        if(terrain==null){
            deactivate();
            return;
        }

        fleet.getStats().getDetectedRangeMod().modifyMult("sm_mining", 1f + SMConstants.SENSOR_DETECTED_DEBUFF * level, "Smart Mining");
        fleet.getStats().getFleetwideMaxBurnMod().modifyMult("sm_mining", 1f + SMConstants.BURN_DEBUFF * level, "Smart Mining");

        if(level>=1 && amount>0){
            float days = Global.getSector().getClock().convertToDays(amount);


            if(miningAsteroidTracker.intervalElapsed()){
                miningAsteroidTracker.setElapsed(0f);

                List<FleetMemberAPI> miningFleet = SMStats.getMiningFleet(fleet);

                float miningPower = SMStats.getFleetMiningPower(fleet);


                if(miningPower<=0 || Math.round(fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY))<SMStats.getMiningHeavyMachinery(miningPower)){
                    deactivate();
                    return;
                }

                runMiningEvent(fleet,miningFleet, terrain, miningPower,random);

                if(miningReportTracker.intervalElapsed()) {
                    miningReportTracker.setElapsed(0f);
                    runMiningReport();
                }
                if(miningEventTracker.intervalElapsed()) {
                    miningEventTracker.setElapsed(0f);
                    SMEvents.runEvent(fleet,miningFleet, terrain,miningPower,random);
                }
                miningEventTracker.advance(1f);
                miningReportTracker.advance(1f);
            }
            miningAsteroidTracker.advance(days);

        }
    }

    @Override
    protected void deactivateImpl() {
        cleanupImpl();
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
    }

    @Override
    protected String getActivationText() {
        return "Start Mining";
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }
    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        LabelAPI title = tooltip.addTitle("Mining" + status);
        title.highlightLast(status);
        title.setHighlightColor(gray);

        float pad = 10f;

        tooltip.addPara("Mine ore and transplutonic ore inside a ring or an asteroid field.", pad);


        CampaignFleetAPI fleet = getFleet();
        if (fleet != null) {
            float miningPower = SMStats.getFleetMiningPower(fleet);


            tooltip.addPara("Fleet mining power: " + miningPower, pad);
            tooltip.addPara("Heavy Machinery needed: " + Math.round(miningPower)/2, pad);

            if(miningPower<=0){
                tooltip.addPara("Your fleet has no mining power.", Misc.getNegativeHighlightColor(), pad);
            }
            if(Math.round(fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY))<SMStats.getMiningHeavyMachinery(miningPower)){
                tooltip.addPara("You do not have enough Heavy Machinery.", Misc.getNegativeHighlightColor(), pad);
            }

            if(SMMisc.getTerrainId(fleet) == null){
                tooltip.addPara("Your fleet is not currently inside a ring or an asteroid field.", Misc.getNegativeHighlightColor(), pad);
            }

        }


        addIncompatibleToTooltip(tooltip, expanded);
    }
    @Override
    public boolean isUsable() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return false;
        float miningPower = SMStats.getFleetMiningPower(fleet);

        return super.isUsable() &&
                SMMisc.getTerrainId(fleet) != null &&
                miningPower>0 &&
                Math.round(fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY))>=SMStats.getMiningHeavyMachinery(miningPower) &&
                isUsingIncompatible();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
    }
    @Override
    public boolean isCompatible(AbilityPlugin other) {

        return !other.getId().equals(Abilities.SUSTAINED_BURN) &&
                !other.getId().equals(Abilities.EMERGENCY_BURN) &&
                !other.getId().equals(Abilities.GO_DARK);
    }
    public boolean isUsingIncompatible() {
        for (AbilityPlugin other : this.getFleet().getAbilities().values()) {
            if (other == this) continue;
            if (!isCompatible(other) && other.isActiveOrInProgress()) {
                return false;
            }
        }
        return true;
    }
    protected void runMiningEvent(CampaignFleetAPI fleet,List<FleetMemberAPI> miningFleet,String terrain,float miningPower,Random random){
        for(FleetMemberAPI fleetMember : miningFleet){
            fleetMember.getRepairTracker().applyCREvent(
                    -SMMisc.RandomFloatBetween(SMConstants.CR_LOSS_PER_DAY_MIN,SMConstants.CR_LOSS_PER_DAY_MAX,random)
                    ,"sm_mining","Mining");
        }

        for(SMDrop drop : SMDrop.resourceMap.get(terrain)) {
            float rarity = drop.getRarity(random);
            float mined = rarity * miningPower;
            fleet.getCargo().addCommodity(drop.commodity, mined);
            fleet.addFloatingText("Mining", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);

            if(SMIntelMiningReport.commodities.containsKey(drop.commodity)){
                SMIntelMiningReport.commodities.put(drop.commodity, SMIntelMiningReport.commodities.get(drop.commodity)+mined);
            } else{
                SMIntelMiningReport.commodities.put(drop.commodity,mined);
            }


        }
    }
    protected void runMiningReport(){
        SMIntelMiningReport intel = new SMIntelMiningReport();
        Global.getSector().getCampaignUI().addMessage(intel);
        SMIntelMiningReport.commodities.clear();
    }


}
