package smartmining;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;

import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class SMDrop {

    public static Map<String, List<SMDrop>> resourceMap = new HashMap<>();
    public static Map<String, List<SMDrop>> cacheMap = new HashMap<>();
    public static Map<String, Float> cacheProbabilityMap = new HashMap<>();

    public static Logger log = Global.getLogger(OfficerManagerEvent.class);
    public static void loadDropTable(){
        try{
            JSONObject settingsFile = Global.getSettings().getMergedJSONForMod(SMConstants.SETTINGS_PATH, SMConstants.MOD_ID);
            JSONObject resources = settingsFile.getJSONObject("resources");

            Map<String,JSONObject> dropsForTerrain = SMMisc.JsonToMap(resources);

            for(Map.Entry<String,JSONObject> dropForTerrain : dropsForTerrain.entrySet()){
                JSONObject commodities =  dropForTerrain.getValue().getJSONObject("commodities");

                List<SMDrop> drop = new ArrayList<>();
                Map<String,JSONObject> commoditiesForDrop = SMMisc.JsonToMap(commodities);
                for(Map.Entry<String,JSONObject> commodityForDrop : commoditiesForDrop.entrySet()){

                    drop.add(new SMDrop(
                            dropForTerrain.getKey(),
                            commodityForDrop.getKey(),
                            commodityForDrop.getValue().getDouble("min"),
                            commodityForDrop.getValue().getDouble("max")
                    ));
                }


                SMDrop.resourceMap.put(dropForTerrain.getKey(),drop);
            }

            JSONObject hiddenCaches = settingsFile.getJSONObject("hidden_caches");

            Map<String,JSONObject> dropsForCache = SMMisc.JsonToMap(hiddenCaches);

            for(Map.Entry<String,JSONObject> dropForCache : dropsForCache.entrySet()){
                JSONObject commodities =  dropForCache.getValue().getJSONObject("commodities");

                cacheProbabilityMap.put(dropForCache.getKey(),(float)dropForCache.getValue().getDouble("probability"));

                List<SMDrop> drop = new ArrayList<>();
                Map<String,JSONObject> commoditiesForDrop = SMMisc.JsonToMap(commodities);
                for(Map.Entry<String,JSONObject> commodityForDrop : commoditiesForDrop.entrySet()){
                    if(commodityForDrop.getValue().has("limit")){
                        drop.add(new SMDrop(
                                dropForCache.getKey(),
                                commodityForDrop.getKey(),
                                commodityForDrop.getValue().getDouble("min"),
                                commodityForDrop.getValue().getDouble("max"),
                                commodityForDrop.getValue().getInt("limit")
                        ));
                    } else {
                        drop.add(new SMDrop(
                                dropForCache.getKey(),
                                commodityForDrop.getKey(),
                                commodityForDrop.getValue().getDouble("min"),
                                commodityForDrop.getValue().getDouble("max")
                        ));
                    }


                }


                SMDrop.cacheMap.put(dropForCache.getKey(),drop);
            }
        } catch (IOException | JSONException exception) {
            throw new RuntimeException("Failed to load drops", exception);
        }
    }

    public String terrain;
    public String commodity;
    public float min;
    public float max;
    public int limit;
    public SMDrop(String terrain,String commodity,double min,double max){
        this.terrain = terrain;
        this.commodity = commodity;
        this.min = (float) min;
        this.max = (float) max;
        this.limit = 0;
    }
    public SMDrop(String terrain,String commodity,double min,double max,int limit){
        this.terrain = terrain;
        this.commodity = commodity;
        this.min = (float) min;
        this.max = (float) max;
        this.limit = limit;
    }
    public float getRarity(Random random){
        return SMMisc.RandomFloatBetween(min,max,random);
    }


    public static void DisplayGathered(CampaignFleetAPI fleet, CampaignTerrainAPI terrain, float power, Random random){



    }
}
