package smartmining.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import smartmining.*;

import smartmining.campaign.intel.SMIntelMiningReport;
import smartmining.events.SMEvents;


import java.awt.*;
import java.util.Random;
import java.util.List;

public class SMMiningAbility extends BaseToggleAbility {

    private final float BURN_DEBUFF = -0.2f;
    private final float DETECTED_DEBUFF = 0.2f;
    private final IntervalUtil miningAsteroidTracker = new IntervalUtil(1f, 1f);
    private final IntervalUtil miningEventTracker = new IntervalUtil(1f, 5f);
    private final IntervalUtil miningReportTracker = new IntervalUtil(3f, 3f);


    //private static String commodity
    private Random random = new Random();
    @Override
    protected void activateImpl() {
        miningAsteroidTracker.setElapsed(0f);
        miningEventTracker.setElapsed(0f);
        miningReportTracker.setElapsed(0f);
        SMIntelMiningReport.commodities.clear();
    }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        String terrain = SMMisc.getTerrainId(fleet);
        float miningPower = SMStats.getFleetMiningPower(fleet);
        if(terrain==null){
            deactivate();
            return;
        }

        if(fleet.getCargo().getSpaceUsed()>=fleet.getCargo().getMaxCapacity() ||
                miningPower<=0||
                Math.round(fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY))<SMStats.getMiningHeavyMachinery(miningPower)
        ){
            deactivate();
        }


        fleet.getStats().getDetectedRangeMod().modifyMult("sm_mining", 1f + DETECTED_DEBUFF * level, "Smart Mining");
        fleet.getStats().getFleetwideMaxBurnMod().modifyMult("sm_mining", 1f + BURN_DEBUFF * level, "Smart Mining");

        if(level>=1 && amount>0){
            float days = Global.getSector().getClock().convertToDays(amount);
            List<FleetMemberAPI> miningFleet = SMStats.getMiningFleet(fleet);

            if(miningAsteroidTracker.intervalElapsed()){
                miningAsteroidTracker.setElapsed(0);
                runMiningEvent(fleet,miningFleet, terrain, miningPower,random);
            }
            if(miningReportTracker.intervalElapsed()) {
                miningReportTracker.setElapsed(0);
                runMiningReport();
            }
            if(miningEventTracker.intervalElapsed()) {
                miningEventTracker.setElapsed(0);
                SMEvents.runEvent(fleet,miningFleet, terrain,miningPower,random);
            }
            miningAsteroidTracker.advance(days);
            miningEventTracker.advance(days);
            miningReportTracker.advance(days);
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
                Math.round(fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY))>=SMStats.getMiningHeavyMachinery(miningPower);
    }
    @Override
    public void advance(float amount) {
        super.advance(amount);
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
