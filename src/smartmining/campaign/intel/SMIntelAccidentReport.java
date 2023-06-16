package smartmining.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SMIntelAccidentReport extends BaseIntelPlugin {
    public static Map<String,Float> commodities = new HashMap();
    public static Map<String,Float> damagedHullShips = new HashMap();
    public static Map<String,Float> damagedCRShips = new HashMap();
   // public static Map<String,Float> commodities = new HashMap();
    private String terrain;
    public SMIntelAccidentReport(String terrain){
        this.terrain=terrain;
    }
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;

        info.addPara("Mining Accident", c, 0f);

        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        bullet(info);

        for(Map.Entry<String, Float> items : commodities.entrySet()){
            info.addPara(Global.getSettings().getCommoditySpec(items.getKey()).getName() +": "+Math.round(items.getValue()), opad);
        }
        for(Map.Entry<String, Float> items : damagedHullShips.entrySet()){
            info.addPara(items.getKey() +" suffers "+Math.round(items.getValue()) + " Hull damage", opad);
        }
        for(Map.Entry<String, Float> items : damagedCRShips.entrySet()){
            info.addPara(items.getKey() +" suffers "+Misc.getRoundedValueMaxOneAfterDecimal(items.getValue()*100) + "% reduced CR", opad);
        }
    }
}
