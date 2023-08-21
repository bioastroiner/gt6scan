package bioast.mods.gt6m.scanner.item;

import bioast.mods.gt6m.scanner.item.behaviour.ScannerBehavior;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.multiitem.tools.ToolStats;
import gregapi.old.Textures;
import gregapi.render.IIconContainer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ScannerToolStats extends ToolStats {
    public final int tier;

    public ScannerToolStats(int tier) {
        this.tier = tier;
    }

    public float getMaxDurabilityMultiplier() {
        if (tier - 6 == 0) return (float) Math.pow(((float) ((tier - 6F) * 2F)), 0.0D);
        else return (float) ((tier - 6F) * 2F);
    }

    @Override
    public boolean isMinableBlock(Block aBlock, byte aMetaData) {
        return false;
    }

    @Override
    public short[] getRGBa(boolean aIsToolHead, ItemStack aStack) {
        return aIsToolHead ? MultiItemTool.getPrimaryMaterial(aStack).mRGBaSolid
                : MultiItemTool.getSecondaryMaterial(aStack).mRGBaSolid;
    }

    @Override
    public void onStatsAddedToTool(MultiItemTool aItem, int aID) {
        aItem.addItemBehavior(aID, new ScannerBehavior());
    }

    @Override
    public int getBaseQuality() {
        return 2;
    }

    @Override
    public IIconContainer getIcon(boolean aIsToolHead, ItemStack aStack) {
        return new Textures.ItemIcons.CustomIcon("gt6m.scanner/ELECTRIC_LuV_PRO_PICK_HEAD");
    }
}
