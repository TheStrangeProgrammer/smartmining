package smartmining;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import smartmining.hullmods.SMHullmods;

import java.util.ArrayList;
import java.util.List;

public class SMStats {
    public static final String SM_MINING_POWER = "sm_mining_power";

    public static List<FleetMemberAPI> getMiningFleet(CampaignFleetAPI fleet){
        List<FleetMemberAPI> miningFleet = new ArrayList<>();
        for(FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()){
            if(!fleetMember.isMothballed()){
                if(fleetMember.getVariant().hasHullMod(SMHullmods.SM_MINING_RETROFIT)) {
                    miningFleet.add(fleetMember);
                }
            }
        }
        return miningFleet;
    }
    public static float getFleetMiningPower(CampaignFleetAPI fleet){
        float miningPowerSum = 0;
        for(FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()){
            if(!fleetMember.isMothballed()){
                miningPowerSum+=getShipMiningPower(fleetMember);
            }
        }
        return miningPowerSum;
    }

    public static float getShipMiningPower(FleetMemberAPI fleetMember){
        if(fleetMember.getVariant().hasHullMod(SMHullmods.SM_MINING_RETROFIT)){
            return fleetMember.getStats().getDynamic().getMod(SMStats.SM_MINING_POWER).computeEffective(0f);
        }
        //return getWeaponMiningPower(fleetMember.getVariant());
        return 0;
    }

    public static float getWeaponMiningPower(ShipVariantAPI variant){
        float sumPower = 0;
        for (String slot : variant.getFittedWeaponSlots())
        {
            if(variant.getWeaponSpec(slot).hasTag("sm_mining")){
                sumPower+=getWeaponOP(variant.getWeaponSpec(slot));
                //sumPower+=variant.getWeaponSpec(slot).getDerivedStats().getDps();
            }

        }
        for (String slot : variant.getFittedWings())
        {
            sumPower+=getWeaponMiningPower(Global.getSettings().getFighterWingSpec(slot).getVariant())*Global.getSettings().getFighterWingSpec(slot).getNumFighters();
        }
        return sumPower;
    }
    public static float getWeaponOP(WeaponSpecAPI weaponSpec){
        return weaponSpec.getOrdnancePointCost(Global.getSector().getPlayerStats());
    }

    public static int getMiningHeavyMachinery(float miningPower){
        return Math.round(miningPower)/2;
    }
}
