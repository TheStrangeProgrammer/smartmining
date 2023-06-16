package smartmining.events;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import smartmining.SMConstants;
import smartmining.SMMisc;
import smartmining.campaign.intel.SMIntelAccidentReport;

import java.util.List;
import java.util.Random;

public class SMAccidents {
    public static boolean lostCrew(CampaignFleetAPI fleet, float miningPower, Random random){
        int randomAmount = SMMisc.RandomIntBetween(1,Math.round(miningPower)/100,random);

        fleet.getCargo().removeCommodity(Commodities.CREW,randomAmount);
        SMIntelAccidentReport.commodities.put(Commodities.CREW,(float)randomAmount);
        return true;
    }
    public static boolean lostFuel(CampaignFleetAPI fleet, float miningPower, Random random){
        int randomAmount = SMMisc.RandomIntBetween(1,Math.round(miningPower)/100,random);

        fleet.getCargo().removeCommodity(Commodities.FUEL,randomAmount);
        SMIntelAccidentReport.commodities.put(Commodities.FUEL,(float)randomAmount);
        return true;
    }
    public static boolean lostHeavyMachinery(CampaignFleetAPI fleet, float miningPower, Random random){
        int randomAmount = SMMisc.RandomIntBetween(1,Math.round(miningPower)/50,random);

        fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY,randomAmount);
        SMIntelAccidentReport.commodities.put(Commodities.HEAVY_MACHINERY,(float)randomAmount);
        return true;
    }
    public static boolean damageCR(List<FleetMemberAPI> miningFleet, float miningPower, Random random){
        WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>();
        picker.addAll(miningFleet);

        int randomAmountOfShips = SMMisc.RandomIntBetween(1,miningFleet.size(),random);

        for(int i=0;i<randomAmountOfShips;i++){
            FleetMemberAPI fleetMember = picker.pickAndRemove();
            float damage = 100f;
            fleetMember.getStatus().applyDamage(damage);
            if (fleetMember.getStatus().getHullFraction() < 0.01f) {
                fleetMember.getStatus().setHullFraction(0.01f);
            }
            SMIntelAccidentReport.damagedHullShips.put(fleetMember.getShipName(),damage);
        }
        return true;
    }
    public static boolean damageHull(List<FleetMemberAPI> miningFleet, float miningPower, Random random){
        WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>();
        picker.addAll(miningFleet);

        int randomAmountOfShips = SMMisc.RandomIntBetween(1,miningFleet.size(),random);

        for(int i=0;i<randomAmountOfShips;i++){
            FleetMemberAPI fleetMember = picker.pickAndRemove();
            float damage = -SMMisc.RandomFloatBetween(SMConstants.CR_LOSS_PER_DAY_MIN+0.1f,SMConstants.CR_LOSS_PER_DAY_MAX+0.1f,random);
            fleetMember.getRepairTracker().applyCREvent(damage,"sm_mining_accident","Mining Accident");
            SMIntelAccidentReport.damagedCRShips.put(fleetMember.getShipName(),-damage);
        }
        return true;
    }
}
