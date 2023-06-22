package smartmining.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import smartmining.SMConstants;
import smartmining.SMStats;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SMMiningRetrofit extends BaseLogisticsHullMod {

    private static Map miningPower = new HashMap();
    static {
        miningPower.put(HullSize.FRIGATE, 0.08f);
        miningPower.put(HullSize.DESTROYER, 0.12f);
        miningPower.put(HullSize.CRUISER, 0.20f);
        miningPower.put(HullSize.CAPITAL_SHIP, 0.30f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesToRecover().modifyMult(id,1f- SMConstants.SUPPLIES_TO_RECOVER);
        stats.getDynamic().getMod(SMStats.SM_MINING_POWER).modifyMult(id+"_mult",1f + (Float) miningPower.get(hullSize));
        stats.getDynamic().getMod(SMStats.SM_MINING_POWER).modifyFlat(id+"_flat",SMStats.getWeaponMiningPower(stats.getVariant()));
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return null;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + ((Float) miningPower.get(HullSize.FRIGATE)).intValue() + "";
        if (index == 1) return "" + ((Float) miningPower.get(HullSize.DESTROYER)).intValue() + "";
        if (index == 2) return "" + ((Float) miningPower.get(HullSize.CRUISER)).intValue() + "";
        if (index == 3) return "" + ((Float) miningPower.get(HullSize.CAPITAL_SHIP)).intValue() + "";

        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        //tooltip.addPara(". ", opad);
        if (isForModSpec || ship == null) return;
        if (Global.getSettings().getCurrentState() == GameState.TITLE) return;

        tooltip.addPara("The total mining power for this ship is: %s.", opad, h,
                "" + getShipMiningPower(ship)+ "");
    }

    public int getShipMiningPower(ShipAPI ship){

        return (int)(ship.getMutableStats().getDynamic().getMod(SMStats.SM_MINING_POWER).computeEffective(0f));
    }
}
