package smartmining.campaign.intel.reports;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SMIntelCacheReport extends BaseIntelPlugin {
    public static Map<String,Float> commodities = new HashMap();
    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;

        info.addPara("Hidden Cache Discovered", c, 0f);

        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        bullet(info);

        for(Map.Entry<String, Float> items : commodities.entrySet()){
            info.addPara(Global.getSettings().getCommoditySpec(items.getKey()).getName() +": "+Math.round(items.getValue()), opad);
        }
    }
}
