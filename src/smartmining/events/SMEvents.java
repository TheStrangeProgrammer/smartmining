package smartmining.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;

import com.fs.starfarer.api.util.WeightedRandomPicker;

import org.apache.log4j.Logger;

import smartmining.SMConstants;
import smartmining.SMDrop;
import smartmining.SMMisc;
import smartmining.campaign.intel.reports.SMIntelAccidentReport;
import smartmining.campaign.intel.reports.SMIntelCacheReport;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SMEvents {
    public static Logger log = Global.getLogger(OfficerManagerEvent.class);

    public static void runEvent(CampaignFleetAPI fleet, List<FleetMemberAPI> miningFleet, String terrain,float miningPower, Random random){

        if(random.nextFloat()< SMConstants.ACCIDENT_PROBABILITY){
            accidentHappened(fleet,miningFleet,terrain,miningPower,random);

        } else if(random.nextFloat()<SMConstants.CACHE_PROBABILITY){
            foundCache(fleet,miningFleet,terrain,miningPower,random);
        }

    }
    public static void accidentHappened(CampaignFleetAPI fleet,List<FleetMemberAPI> miningFleet,String terrain,float miningPower,Random random){
        int randomAmount = SMMisc.RandomIntBetween(1,3,random);
        WeightedRandomPicker<Integer> probabilityPicker = new WeightedRandomPicker<>();
        probabilityPicker.add(1);
        probabilityPicker.add(2);
        probabilityPicker.add(3);
        probabilityPicker.add(4);
        probabilityPicker.add(5);
        for(int i=0;i<randomAmount;i++){


            int pick = probabilityPicker.pickAndRemove();
            if(pick == 1){
                SMAccidents.lostFuel(fleet,miningPower,random);
            } else if(pick == 2){
                SMAccidents.lostCrew(fleet,miningPower,random);
            } else if(pick == 3){
                SMAccidents.lostHeavyMachinery(fleet,miningPower,random);
            } else if(pick == 4){
                SMAccidents.damageHull(miningFleet,miningPower,random);
            } else if(pick == 5){
                SMAccidents.damageCR(miningFleet,miningPower,random);
            }

        }

        SMIntelAccidentReport intel = new SMIntelAccidentReport(terrain);
        Global.getSector().getCampaignUI().addMessage(intel);
        SMIntelAccidentReport.commodities.clear();

    }
    public static void foundCache(CampaignFleetAPI fleet,List<FleetMemberAPI> miningFleet,String terrain,float miningPower,Random random){
        WeightedRandomPicker<String> probabilityPicker = new WeightedRandomPicker<>();

        for(Map.Entry<String, Float> probability : SMDrop.cacheProbabilityMap.entrySet()){

            probabilityPicker.add(probability.getKey(),probability.getValue());

        }
        String pick = probabilityPicker.pick();
        List<SMDrop> cacheDrops = SMDrop.cacheMap.get(pick);

        WeightedRandomPicker<SMDrop> dropPicker = new WeightedRandomPicker<>();
        dropPicker.addAll(cacheDrops);

        int randomAmount = SMMisc.RandomIntBetween(1,Math.min(3,cacheDrops.size()),random);

        for(int i=0;i<randomAmount;i++){
            SMDrop drop = dropPicker.pickAndRemove();
            float rarity = drop.getRarity(random);
            int totalDrops = (int)Math.round(Math.ceil(rarity*miningPower));

            fleet.getCargo().addCommodity(drop.commodity,totalDrops);
            SMIntelCacheReport.commodities.put(drop.commodity,(float)totalDrops);
        }

        SMIntelCacheReport intel = new SMIntelCacheReport();
        Global.getSector().getCampaignUI().addMessage(intel);
        SMIntelCacheReport.commodities.clear();
    }

}
