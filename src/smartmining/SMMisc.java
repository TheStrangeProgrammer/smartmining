package smartmining;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class SMMisc {
    public static float RandomFloatBetween(float min, float max,Random random){
        if(min>=max) return min;
        return min + random.nextFloat() * (max - min);
    }
    public static int RandomIntBetween(int min, int max,Random random){
        if(min>=max) return min;
        return min + random.nextInt()%max;
    }
    public static Map<String,JSONObject> JsonToMap(JSONObject json){
        try {
            Map<String,JSONObject> map = new HashMap<String,JSONObject>();
            Iterator<String> keys = json.sortedKeys();
            while (keys.hasNext()) {
                String id = keys.next();
                JSONObject value = json.getJSONObject(id);
                map.put(id,value);
            }
            return map;
        } catch (JSONException exception) {
            throw new RuntimeException("Failed to map json", exception);
        }
    }
    public static String getTerrainId(CampaignFleetAPI fleet){
        for (CampaignTerrainAPI terrain : fleet.getContainingLocation().getTerrainCopy()) {
            if (terrain.getPlugin() instanceof AsteroidFieldTerrainPlugin) {
                if (terrain.getPlugin().containsEntity(fleet)) {
                    return Terrain.ASTEROID_FIELD;
                }
            } else if (terrain.getPlugin() instanceof AsteroidBeltTerrainPlugin) {
                if (terrain.getPlugin().containsEntity(fleet)) {
                    return Terrain.ASTEROID_BELT;
                }
            } else if (terrain.getPlugin() instanceof RingSystemTerrainPlugin) {
                if (terrain.getPlugin().containsEntity(fleet)) {
                    return Terrain.RING;
                }
            } else if (terrain.getPlugin() instanceof NebulaTerrainPlugin) {
                if (terrain.getPlugin().containsEntity(fleet)) {
                    return Terrain.NEBULA;
                }
            } else if (terrain.getPlugin() instanceof StarCoronaTerrainPlugin) {
                if (terrain.getPlugin().containsEntity(fleet)) {
                    return Terrain.CORONA;
                }
            } else if (terrain.getPlugin() instanceof PulsarBeamTerrainPlugin) {
                if (terrain.getPlugin().containsEntity(fleet)) {
                    return Terrain.PULSAR_BEAM;
                }
            }
        }
        return null;

    }
}
