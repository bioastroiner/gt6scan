package bioast.mods.gt6m.scanner.item.behaviour;

import bioast.mods.gt6m.scanner.NetwrokHandler;
import bioast.mods.gt6m.scanner.PacketScanner;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.OP;
import gregapi.item.multiitem.MultiItem;
import gregapi.item.multiitem.behaviors.IBehavior.AbstractBehaviorDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

import static bioast.mods.gt6m.scanner.utils.HLPs.*;
import static gregapi.util.UT.Entities.sendchat;

public class ScannerBehavior extends AbstractBehaviorDefault {

    @Override
    public boolean onItemUse(MultiItem aItem, ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, byte aSide, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public ItemStack onItemRightClick(MultiItem aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        // On Client
        if(aWorld.isRemote) {
//            PacketScanner packet = new PacketScanner(aPlayer.chunkCoordX,aPlayer.chunkCoordZ, (int) aPlayer.posX, (int) aPlayer.posZ, 1);
//            NetwrokHandler.INSTANCE.sendToServer(packet);
//            NetwrokHandler.INSTANCE.mChannel.get(Side.CLIENT).generatePacketFrom(aPlayer);
//            NetwrokHandler.INSTANCE.mChannel.get(Side.CLIENT).findChannelHandlerNameForType(NetwrokHandler.class);
//            GT6M_Mod.proxy.openProspectorGUI();
            return super.onItemRightClick(aItem, aStack, aWorld, aPlayer);
        }
        // On Server
        final int cX = ((int) aPlayer.posX) >> 4;final int cZ = ((int) aPlayer.posZ) >> 4;
        int size = 1;
        final List<Chunk> chunks = new ArrayList<>();
        sendchat(aPlayer,"Scanning...");
        for (int i = -size; i <= size; i++)
            for (int j = -size; j <= size; j++)
                if (i != -size && i != size && j != -size && j != size)
                    chunks.add(aWorld.getChunkFromChunkCoords(cX + i, cZ + j));
        size = size - 1;
        final PacketScanner packet = new PacketScanner(cX, cZ, (int) aPlayer.posX, (int) aPlayer.posZ, size);
        // loop trough a chunk
        for (Chunk c:chunks) for(Object cPosObj:c.chunkTileEntityMap.keySet()) /*for (int x = 0; x < 16; x++)for (int z = 0; z < 16; z++)for(int y = 1;y < c.getHeightValue(x,z); y++)*/{
            /* Do Ore Block Logic Here */
            ChunkPosition cPos = (ChunkPosition) cPosObj;
            TileEntity tTileEntity = (TileEntity) c.chunkTileEntityMap.get(cPosObj);
            if(tTileEntity instanceof PrefixBlockTileEntity){
                String tType = prefix(tTileEntity).mFamiliarPrefixes.contains(OP.ore)? "LargeOre": "SamllOre";
                //sendchat(aPlayer,"found" + " " + tType + ": " + OreDictMaterial.MATERIAL_ARRAY[((PrefixBlockTileEntity)tTileEntity).mMetaData] + " At Chunk: " + " X: " + c.xPosition + " Z: " + c.zPosition);
                if(tType.startsWith("Large")){
                    packet.addBlock(tTileEntity.xCoord,tTileEntity.yCoord,tTileEntity.zCoord, ((PrefixBlockTileEntity) tTileEntity).mMetaData);
                }
            }
//            final Block tBlock = c.getBlock(x, y, z);
//            short tMetaID = (short) c.getBlockMetadata(x, y, z);
//
//            OreDictMaterial tMaterial;
//            // For now we only interested in Normal Ores, //TODO Smalls
//            if(HLPs.orem(tBlock,tMetaID) && ((PrefixBlock)tBlock).mPrefix.equals(OP.ore)){
//                tMaterial = HLPs.oreId(tBlock,tTileEntity);
//                packet.addBlock(c.xPosition * 16 + x, y, c.zPosition * 16 + z, tMetaID);
//                sendchat(aPlayer,"found: " + tMaterial + " :" + tMetaID);
//            }
            //if (!aPlayer.capabilities.isCreativeMode) tool.doDamage(aStack, this.mCosts * chunks.size());
        }
        NetwrokHandler.INSTANCE.sendToClient(packet, (EntityPlayerMP) aPlayer);
        return super.onItemRightClick(aItem, aStack, aWorld, aPlayer);

    }
}
