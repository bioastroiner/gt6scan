package bioast.mods.gt6scan.utils;

import bioast.mods.gt6scan.ScannerMod;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.UT;
import gregapi.util.WD;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

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
            ScannerMod.debug.error("Not a PrefixBlock");
        }
    return tBlock;
    }

    public static Map<ChunkPosition,OreDictMaterial> ores(Chunk aChunk,OreDictPrefix compare){
        Map<ChunkPosition,OreDictMaterial> map = new HashMap<>();
        Map<ChunkPosition,TileEntity> chunkTileEntityMap = new HashMap<>();
        try {
            chunkTileEntityMap = aChunk.chunkTileEntityMap;
        } catch (Exception e) {
            // ignore
        }
        for(ChunkPosition cPos:chunkTileEntityMap.keySet()){
            PrefixBlockTileEntity tTile = null;
            try{
                tTile = (PrefixBlockTileEntity) chunkTileEntityMap.get(cPos);
            } catch (Exception e) {continue;}
            if(prefix(tTile).mFamiliarPrefixes.contains(compare)){
                map.put(cPos,mat(tTile));
            }
        }
        return map;
    }

    public static Map<ChunkPosition,OreDictMaterial> oresSmall(Chunk aChunk){
        return ores(aChunk,OP.oreSmall);
    }

    public static Map<ChunkPosition,OreDictMaterial> oresLarge(Chunk aChunk){
        return ores(aChunk,OP.ore);
    }

    public static Map<ChunkPosition,OreDictMaterial> oresBedrock(Chunk aChunk){
        return ores(aChunk,OP.oreBedrock);
    }

    public static OreDictPrefix prefix(TileEntity aTile){
        return prefixBlock(aTile).mPrefix;
    }

    public static OreDictMaterial mat(TileEntity aTile){
        return OreDictMaterial.MATERIAL_ARRAY[((PrefixBlockTileEntity)aTile).mMetaData];
    }

    public static int col(OreDictMaterial aMat){
        return UT.Code.getRGBaInt(aMat.mRGBaSolid);
    }

    /**
     * @return gets the most top GT ores in a chunk and maps it into ChunkPositions
     */
    public static Map<ChunkPosition,OreDictMaterial> oreTop(Chunk aChunk,OreDictPrefix aPrefix){
        Map<ChunkPosition,OreDictMaterial> aMap = new HashMap<>();
        ores(aChunk,aPrefix).forEach((chunkPosition, oreDictMaterial) -> {
            for (int i = 0; i < chunkPosition.chunkPosX; i++) {

            }
        });
        return aMap;
    }

//    public static void scanChunk(Chunk aChunk){
//        Map<ChunkPosition,OreDictMaterial> oresLarge = oresLarge(aChunk);
//
//    }
}
