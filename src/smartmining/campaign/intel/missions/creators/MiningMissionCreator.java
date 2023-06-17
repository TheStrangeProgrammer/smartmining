package smartmining.campaign.intel.missions.creators;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketDemandAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager.GenericMissionCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import smartmining.campaign.intel.missions.MiningMissionIntel;

public class MiningMissionCreator implements GenericMissionCreator {
    @Override
    public float getMissionFrequencyWeight() {
        return 10f;
    }

    @Override
    public EveryFrameScript createMissionIntel() {
        MarketAPI market = pickMarket();
        if (market == null) return null;

        return new MiningMissionIntel(market);
    }

    protected MarketAPI pickMarket(){
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        for(MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()){
            if (isValidTarget(market)) picker.add(market);
        }
        MarketAPI market = picker.pick();

        for (EveryFrameScript s : GenericMissionManager.getInstance().getActive()) {
            if (s instanceof MiningMissionIntel) {
                MiningMissionIntel intel = (MiningMissionIntel) s;
                if (market == intel.getMarket()) {
                    return null;
                }
            }
        }

        return market;
    }
    protected boolean isValidTarget(MarketAPI market){
        if (market.isHidden()) return false;
        if(market.getFaction().isNeutralFaction()) return false;
        if (market.getFaction().isPlayerFaction()) return false;
        if(market.getPlanetEntity()==null) return false;
        if (Misc.isImportantForReason(market.getPlanetEntity().getMemoryWithoutUpdate(), "smm")) return false;
        if(market.getCommodityData(Commodities.ORE).getDeficitQuantity()<=0 &&
                market.getCommodityData(Commodities.RARE_ORE).getDeficitQuantity()<=0 &&
                market.getCommodityData(Commodities.VOLATILES).getDeficitQuantity()<=0){

            return false;
        }
        return true;
    }
}
