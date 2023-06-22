package smartmining.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.combat.CombatEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import smartmining.SMConstants;
import smartmining.SMDrop;
import smartmining.SMMisc;
import smartmining.campaign.abilities.SMAbilities;
import smartmining.campaign.intel.missions.creators.MiningMissionCreator;

import java.io.IOException;
import java.util.*;


public class SmartMiningModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        if(!Global.getSector().getPlayerFleet().hasAbility(SMAbilities.SM_MINING_ABILITY)) {
            Global.getSector().getCharacterData().addAbility(SMAbilities.SM_MINING_ABILITY);
        }
        loadSettings();
        loadWeapons();
        SMDrop.loadDropTable();
        addScriptsIfNeeded();
    }
    protected void addScriptsIfNeeded() {
        GenericMissionManager manager = GenericMissionManager.getInstance();
        if (!manager.hasMissionCreator(MiningMissionCreator.class)) {
            manager.addMissionCreator(new MiningMissionCreator());
        }
    }
    protected void loadSettings(){
        try{
            JSONObject settingsFile = Global.getSettings().getMergedJSONForMod(SMConstants.SETTINGS_PATH, SMConstants.MOD_ID);
            JSONObject cr_loss = settingsFile.getJSONObject("cr_loss_per_day");
            SMConstants.CR_LOSS_PER_DAY_MIN = (float)cr_loss.getDouble("min");
            SMConstants.CR_LOSS_PER_DAY_MAX = (float)cr_loss.getDouble("max");

            SMConstants.ACCIDENT_PROBABILITY = (float)settingsFile.getDouble("accident_probability");
            SMConstants.CACHE_PROBABILITY = (float)settingsFile.getDouble("cache_probability");
            SMConstants.SENSOR_DETECTED_DEBUFF = (float)settingsFile.getDouble("sensor_detected_debuff");
            SMConstants.BURN_DEBUFF = (float)settingsFile.getDouble("burn_debuff");
            SMConstants.SUPPLIES_TO_RECOVER = (float)settingsFile.getDouble("supplies_to_recover_buff");
        } catch (IOException | JSONException exception) {
            throw new RuntimeException("Failed to load drops", exception);
        }
    }
    protected void loadWeapons(){
        try{
            JSONObject settingsFile = Global.getSettings().getMergedJSONForMod(SMConstants.SETTINGS_PATH, SMConstants.MOD_ID);
            JSONArray weapons = settingsFile.getJSONArray("weapons");

            for(int i=0;i<weapons.length();i++){
                for(WeaponSpecAPI weaponSpecs : Global.getSettings().getAllWeaponSpecs()){
                    if(weaponSpecs.getWeaponId().equals(weapons.get(i))) {
                        weaponSpecs.addTag("sm_mining");
                    }
                }
            }

        } catch (IOException | JSONException exception) {
            throw new RuntimeException("Failed to load drops", exception);
        }
    }
}
