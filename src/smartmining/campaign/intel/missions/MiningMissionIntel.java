package smartmining.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;

import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;

import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Set;

public class MiningMissionIntel extends BaseMissionIntel {

    protected MarketAPI market;

    protected int reward;
    protected int amountNeeded;
    protected String commodity;

    public MiningMissionIntel(MarketAPI market){
        this.market = market;
        if (market.isHidden()) return;
        if (market.getFaction().isPlayerFaction()) return;


        int oreDeficit  = market.getCommodityData(Commodities.ORE).getDeficitQuantity();
        int rareOreDeficit  = market.getCommodityData(Commodities.RARE_ORE).getDeficitQuantity();
        int volatilesDeficit  = market.getCommodityData(Commodities.VOLATILES).getDeficitQuantity();
        if(oreDeficit>0){
            setDuration(60f);

            reward = 5 * oreDeficit;
            commodity = Commodities.ORE;
            amountNeeded = oreDeficit;
        } else if(rareOreDeficit>0){
            setDuration(60f);

            reward = 20 * rareOreDeficit;
            commodity = Commodities.RARE_ORE;
            amountNeeded = rareOreDeficit;
        } else if(volatilesDeficit>0){
            setDuration(60f);

            reward = 40 * volatilesDeficit;
            commodity = Commodities.VOLATILES;
            amountNeeded = volatilesDeficit;
        }
        initRandomCancel();
        setPostingLocation(market.getPrimaryEntity());
        Global.getSector().getIntelManager().queueIntel(this);
    }

    public MarketAPI getMarket(){

        return market;
    }
    @Override
    public void advanceMission(float amount) {
        if (market.getCommodityData(commodity).getDeficitQuantity()<=0) {
            Global.getSector().getPlayerFleet().getCargo().getCredits().add(reward);
            CoreReputationPlugin.MissionCompletionRep rep =
                    new CoreReputationPlugin.MissionCompletionRep(
                            CoreReputationPlugin.RepRewards.TINY,
                            RepLevel.WELCOMING,
                            -CoreReputationPlugin.RepRewards.TINY,
                            RepLevel.INHOSPITABLE);

            ReputationActionResponsePlugin.ReputationAdjustmentResult result =
                    Global.getSector().adjustPlayerReputation(
                        new CoreReputationPlugin.RepActionEnvelope(
                            CoreReputationPlugin.RepActions.MISSION_SUCCESS,
                            rep,
                            null,
                            null,
                            true,
                            false),
                        market.getFaction().getId());

            setMissionResult(new MissionResult(reward, result));
            setMissionState(MissionState.COMPLETED);
            endMission();
            sendUpdateIfPlayerHasIntel(missionResult, false);
        }
    }
    @Override
    public void endMission() {
        //market.getPlanetEntity().getMemoryWithoutUpdate().unset("$sm_mining_mission_target");
        //market.getPlanetEntity().getMemoryWithoutUpdate().unset("$sm_mining_mission_eventRef");
        Misc.setFlagWithReason(market.getPlanetEntity().getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
                "smm", false, 0f);
        Global.getSector().getListenerManager().removeListener(this);

        endAfterDelay();
    }
    @Override
    public void missionAccepted() {
        //market.getPlanetEntity().getMemoryWithoutUpdate().set("$sm_mining_mission_target", true, getDuration());
        //market.getPlanetEntity().getMemoryWithoutUpdate().set("$sm_mining_mission_eventRef", this, getDuration());
        Misc.setFlagWithReason(market.getPlanetEntity().getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
                "smm", true, getDuration());

        Global.getSector().getListenerManager().addListener(this);
    }

    @Override
    protected MissionResult createTimeRanOutFailedResult() {
        return createAbandonedResult(true);
    }

    @Override
    protected MissionResult createAbandonedResult(boolean withPenalty) {
        if (withPenalty) {
            CoreReputationPlugin.MissionCompletionRep rep = new CoreReputationPlugin.MissionCompletionRep(CoreReputationPlugin.RepRewards.TINY, RepLevel.WELCOMING,
                    -CoreReputationPlugin.RepRewards.TINY, RepLevel.INHOSPITABLE);
            ReputationActionResponsePlugin.ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
                    new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.MISSION_FAILURE, rep,
                            null, null, true, false),
                    market.getFaction().getId());
            return new MissionResult(0, result);
        }
        return new MissionResult();
    }
    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        if (isUpdate) {
            // 3 possible updates: de-posted/expired, failed, completed
            if (isFailed() || isCancelled()) {
                return;
            } else if (isCompleted()) {
                if (missionResult.payment > 0) {
                    info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
                }
                CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, market.getFaction(), null,
                        null, null, info, tc, isUpdate, 0f);
            }
        } else {
            // either in small description, or in tooltip/intel list
            if (missionResult != null) {
                if (missionResult.payment > 0) {
                    info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
                    initPad = 0f;
                }

                if (missionResult.rep1 != null) {
                    CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, market.getFaction(), null,
                            null, null, info, tc, isUpdate, initPad);
                    initPad = 0f;
                }
            } else {
                float betweenPad = 0f;
                if (mode != ListInfoMode.IN_DESC) {
                    info.addPara("Faction: " + market.getFaction().getDisplayName(), initPad, tc,
                            market.getFaction().getBaseUIColor(),
                            market.getFaction().getDisplayName());
                    initPad = betweenPad;
                } else {
                    betweenPad = 0f;
                }

                info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(reward));
                addDays(info, "to complete", duration - elapsedDays, tc, betweenPad);
            }
        }

        unindent(info);
    }
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);
    }

    @Override
    public String getSortString() {
        return "Mine";
    }

    @Override
    protected String getName() {
        return "Mine " + Global.getSettings().getCommoditySpec(commodity).getName() + getPostfixForState();
    }
    @Override
    public FactionAPI getFactionForUIColors() {
        return market.getFaction();
    }
    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }


    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addImage(market.getFaction().getLogo(), width, 128, opad);

        String name = market.getPlanetEntity().getName();

        String authorities = "authorities";
        if (!market.getFaction().getId().equals(market.getFactionId())) {
            authorities = "concerns";
        }

        info.addPara("%s " + authorities + " " + market.getOnOrAt() + " " + market.getName() +
                        " have posted a reward for mining resources for " + name + ", " +
                        market.getPlanetEntity().getSpec().getAOrAn() + " " + market.getPlanetEntity().getTypeNameWithWorld().toLowerCase() + ".",
                opad, market.getFaction().getBaseUIColor(), Misc.ucFirst(market.getFaction().getPersonNamePrefix()));


        if (isPosted() || isAccepted()) {
            addBulletPoints(info, ListInfoMode.IN_DESC);

            info.addPara("Resources needed to complete:",opad,market.getFaction().getBaseUIColor());
            info.addPara("%s "+Global.getSettings().getCommoditySpec(commodity).getName(), opad, tc, h, Integer.toString(amountNeeded));

            addGenericMissionState(info);

            addAcceptOrAbandonButton(info, width, "Accept", "Abandon");
        } else {
            addGenericMissionState(info);

            addBulletPoints(info, ListInfoMode.IN_DESC);
        }

    }
    public String getIcon() {
        return "graphics/hullmods/ablative_armor.png";
    }
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MISSIONS);
        tags.add(market.getFaction().getId());
        return tags;
    }
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return market.getPlanetEntity();
    }
}
