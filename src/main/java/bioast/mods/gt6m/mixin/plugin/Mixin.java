package bioast.mods.gt6m.mixin.plugin;


import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.Arrays;
import java.util.List;

import static bioast.mods.gt6m.mixin.plugin.Side.*;
import static bioast.mods.gt6m.mixin.plugin.TargetedMod.*;

/**
 *  IMPORTANT: Do not make any references to any mod from this file. This file is loaded quite early on and if
 *  you refer to other mods you load them as well. The consequence is: You can't inject any previously loaded classes!
 *  Exception: Tags.java, as long as it is used for Strings only!
 */
public enum Mixin {
    // Gregtech
    GT_TOOLSTATS_MIXIN("gregtech.GT_ToolStats_Mixin", BOTH, GREGTECH),
    GT_MULTIITEMTOOL_MIXIN("gregtech.GT_MultiItemTool_Mixin", BOTH, GREGTECH)
    ;
    public final String mixinClass;
    public final List<TargetedMod> targetedMods;
    private final Side side;

    Mixin(String mixinClass, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = side;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == BOTH
            || side == SERVER && FMLLaunchHandler.side().isServer()
            || side == CLIENT && FMLLaunchHandler.side().isClient())
            && loadedMods.containsAll(targetedMods);
    }
}

enum Side {
    BOTH,
    CLIENT,
    SERVER;
}
