package bioast.mods.gt6m.scanner.utils;

import bioast.mods.gt6m.GT6M_Mod;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.WD;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class HLPs {
    public static boolean ore(Block aBlock, Short aMeta) {
        return WD.ore(aBlock, aMeta);
    }
    public static boolean orem(Block aBlock, Short aMeta) {
        return WD.ore(aBlock, aMeta) && prefix(aBlock);
    }

    public static boolean prefix(Block aBlock){
        return aBlock instanceof PrefixBlock;
    }

    public static OreDictMaterial oreId(Block aBlock, TileEntity aTile) {
        if (prefix(aBlock) && aTile!=null) {
            return ((PrefixBlock)aBlock).getMetaMaterial(aTile);
        }
        return null;
    }

    public static PrefixBlock prefixBlock(TileEntity aTile){
        PrefixBlockTileEntity tTile = null;
        PrefixBlock tBlock = null;
        try {
            tTile = ((PrefixBlockTileEntity)aTile);
            World tWorld = aTile.getWorldObj();
            tBlock = (PrefixBlock) tWorld.getBlock(tTile.xCoord, tTile.yCoord, tTile.zCoord);
        } catch (Exception e){
            GT6M_Mod.LOG.error("Not a PrefixBlock");
        }
    return tBlock;
    }

    public static OreDictPrefix prefix(TileEntity aTile){
        return prefixBlock(aTile).mPrefix;
    }
}
