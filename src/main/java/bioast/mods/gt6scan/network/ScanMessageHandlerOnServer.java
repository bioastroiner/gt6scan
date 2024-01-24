package bioast.mods.gt6scan.network;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.CS;
import gregapi.data.MT;
import gregapi.util.OM;
import gregtech.blocks.BlockDiggable;
import gregtech.blocks.stone.BlockCrystalOres;
import gregtech.blocks.stone.BlockRockOres;
import gregtech.blocks.stone.BlockVanillaOresA;
import gregtech.tileentity.misc.MultiTileEntityFluidSpring;
import gregtech.tileentity.placeables.MultiTileEntityRock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bioast.mods.gt6scan.utils.HLPs.prefixBlock;

public class ScanMessageHandlerOnServer implements IMessageHandler<ScanRequestToServer, ScanResponceToClient> {
    List<OreData> scannedOres = new ArrayList<>();
    //Map<Short, Integer> sortedOres = new HashMap<>();
    int chunkSize = 9;
    int oresFound = 0;
    int x_origin;
    int z_origin;

    public static Chunk[][] getChunksAroundLoc(World aWorld, int posX, int posZ, int chunkSize) {
        Chunk[][] chunks = new Chunk[chunkSize][chunkSize];
        final int CENTER_CHUNK_INDEX = (chunkSize - 1) / 2;
        chunks[CENTER_CHUNK_INDEX][CENTER_CHUNK_INDEX] = aWorld.getChunkFromBlockCoords(posX, posZ);
        final Chunk PLAYER_CHUNK = chunks[CENTER_CHUNK_INDEX][CENTER_CHUNK_INDEX];
        chunks[0][0] = aWorld.getChunkFromChunkCoords(PLAYER_CHUNK.xPosition - ((chunkSize - 1) / 2), PLAYER_CHUNK.zPosition - ((chunkSize - 1) / 2));
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                if (i == 0 && j == 0) continue;
                if (i == CENTER_CHUNK_INDEX && j == CENTER_CHUNK_INDEX) continue;
                chunks[i][j] = aWorld.getChunkFromChunkCoords(chunks[0][0].xPosition + i, chunks[0][0].zPosition + j);
            }
        }
        return chunks;
    }

    @Override
    public ScanResponceToClient onMessage(ScanRequestToServer message, MessageContext ctx) {
        if (ctx.side != Side.SERVER) {
            return null;
        }

        // Do the Scanning Logic over here and send it as a response to Client
        processMessage(ScanMode.values()[message.mode], ctx.getServerHandler().playerEntity.getEntityWorld(), message.x, message.z);

        return new ScanResponceToClient(scannedOres, x_origin, z_origin, message.mode);
    }

    private void processMessage(ScanMode mode, World aWorld, int xo, int zo) {
        scannedOres.clear();
        oresFound = 0;
        Chunk[][] chunks = getChunksAroundLoc(aWorld, xo, zo, chunkSize);
        x_origin = (chunks[0][0].xPosition << 4);
        z_origin = (chunks[0][0].zPosition << 4);
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                if (mode.isTE()) findTileEntityBlocks(chunks[i][j], mode);
                else {
                    for (int k = 0; k < 16; k++) {
                        for (int l = 0; l < 16; l++) {
                            int highest_y = chunks[i][j].getHeightValue(k, l);
                            for (int m = highest_y; m >= 0; m--) {
                                Block block1 = chunks[i][j].getBlock(k, m, l);
                                short matID;
                                int x = chunks[i][j].xPosition * 16 + k;
                                int z = chunks[i][j].zPosition * 16 + l;
                                int y = m;
                                if (mode == ScanMode.DENSE_AND_NORMAL) {
                                    if (block1 instanceof BlockRockOres) {
                                        matID = BlockRockOres.ORE_MATERIALS[chunks[i][j].getBlockMetadata(k, m, l)].mID;
                                        oresFound++;
                                        scannedOres.add(new OreData(x, y, z, matID));
                                        //TODO
                                        // this block can easially cause server to crash because of 2097050 bytes Packet Limit
                                        // so we skip the lower y levels for dense ores
                                        break;
                                    } else if (block1 instanceof BlockCrystalOres) {
                                        matID = BlockCrystalOres.ORE_MATERIALS[chunks[i][j].getBlockMetadata(k, m, l)].mID;
                                        oresFound++;
                                        scannedOres.add(new OreData(x, y, z, matID));
                                        //break;
                                    } else if (block1 instanceof BlockVanillaOresA) {
                                        matID = BlockVanillaOresA.ORE_MATERIALS[chunks[i][j].getBlockMetadata(k, m, l)].mID;
                                        oresFound++;
                                        scannedOres.add(new OreData(x, y, z, matID));
                                        //break;
                                    } else if (block1 instanceof BlockDiggable) {
                                        int meta = aWorld.getBlockMetadata(x, y, z);
                                        matID = 0;
                                        switch (meta) {
                                            case 0:
                                                break;
                                            case 1:
                                                matID = MT.ClayBrown.mID;
                                                break;
                                            case 2:
                                                matID = MT.Peat.mID;
                                                break;
                                            case 3:
                                                matID = MT.ClayRed.mID;
                                                break;
                                            case 4:
                                                matID = MT.Bentonite.mID;
                                                break;
                                            case 5:
                                                matID = MT.Palygorskite.mID;
                                                break;
                                            case 6:
                                                matID = MT.Kaolinite.mID;
                                                break;
                                        }
                                        if (matID != 0) {
                                            scannedOres.add(new OreData(x, y, z, matID));
                                            oresFound++;
                                        }
                                    }
                                } else if (mode == ScanMode.FLUID) {
                                    //TODO HOLY SHIT FLUIDS ARE A MESS!!!
                                    if (block1 == Blocks.lava) {
                                        matID = MT.Lava.mID;
                                        if (aWorld.provider.isHellWorld) {
                                            scannedOres.add(new OreData(x, y, z, matID));
                                            oresFound++;
                                            break;
                                        }
                                        scannedOres.add(new OreData(x, y, z, matID));
                                        oresFound++;
                                    }
                                    if (block1 instanceof IFluidBlock || block1 == Blocks.lava || block1 == Blocks.water) {
                                        if (!(block1 instanceof IFluidBlock)) break;
                                        String fluidName = ((IFluidBlock) block1).getFluid().getName();
                                        matID = MT.Air.mID;
                                        if (fluidName.contains("natural")) matID = MT.MethaneIce.mID;

                                        if (fluidName.contains("oil")) matID = MT.Oil.mID;
                                        if (fluidName.contains("honey")) matID = MT.Honey.mID;
                                        if (fluidName.contains("sulfuric")) matID = MT.H2SO4.mID;
                                        if (fluidName.contains("acid")) matID = MT.H2SO4.mID;
                                        if (fluidName.contains("poison")) matID = MT.DirtyWater.mID;
                                        if (fluidName.contains("infused")) matID = MT.InfusedWater.mID;
                                        if (fluidName.contains("mana")) matID = MT.Magic.mID;
                                        if (block1 == CS.BlocksGT.WaterGeothermal) matID = MT.DistWater.mID;
                                        if (fluidName.contains("water")) {
                                            matID = MT.Water.mID;
                                            scannedOres.add(new OreData(x, y, z, matID));
                                            oresFound++;
                                            break;
                                        }
//										if (block1 == CS.BlocksGT.Swamp) {
//											matID = MT.DirtyWater.mID;
//											scannedOres.add(new OreData(x, y, z, matID));
//											oresFound++;
//											break;
//										}
//										if (block1 == CS.BlocksGT.River) {
//											matID = MT.FreshWater.mID;
//											scannedOres.add(new OreData(x, y, z, matID));
//											oresFound++;
//											break;
//										}

//										if (block1 == CS.BlocksGT.Ocean) {
//											/*skip ocean*/
////											matID = MT.SeaWater.mID;
////											scannedOres.add(new OreData(x, y, z, matID));
////											oresFound++;
////											break;
//										}
                                        if (matID != MT.Air.mID) {
                                            scannedOres.add(new OreData(x, y, z, matID));
                                            oresFound++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void findTileEntityBlocks(Chunk chunk, ScanMode mode) {
        var tMap = chunk.chunkTileEntityMap;
        Map<ChunkPosition, TileEntity> tTileMap;
        if (tMap != null) {
            try {
                tTileMap = (Map<ChunkPosition, TileEntity>) (tMap);
                tTileMap.forEach((chunkPos, tile) -> {
                    if (tile instanceof PrefixBlockTileEntity pTile && (mode == ScanMode.LARGE || mode == ScanMode.SMALL || mode == ScanMode.BEDROCK)) {
                        PrefixBlock pBlock = prefixBlock(pTile);
                        boolean isBedrock = false;
                        if (mode == ScanMode.BEDROCK) {
                            isBedrock = pBlock.mNameInternal.contains("bedrock");
                            //ScannerMod.debug.info(pBlock.mNameInternal);
                        }
                        if (isBedrock || pBlock.mPrefix.mFamiliarPrefixes.contains(mode.PREFIX)) {
                            short matID = pTile.mMetaData;
                            int x = pTile.getX();
                            int y = pTile.getY();
                            int z = pTile.getZ();
                            scannedOres.add(new OreData(x, y, z, matID));
                            oresFound++;
                        }
                    }
                    if (mode == ScanMode.FLUID_BEDROCK && tile instanceof MultiTileEntityFluidSpring) {
                        FluidStack fluidStack = ((MultiTileEntityFluidSpring) tile).mFluid;
                        String name = fluidStack.getFluid().getName();
                        short matID = 0;
                        if (name.contains("oil")) matID = MT.Oil.mID;
                        if (name.contains("water")) matID = MT.Water.mID;
                        if (name.contains("lava")) matID = MT.Lava.mID;
                        if (name.contains("natural")) matID = MT.CH4.mID;
                        int x = ((MultiTileEntityFluidSpring) tile).getX();
                        int y = ((MultiTileEntityFluidSpring) tile).getY();
                        int z = ((MultiTileEntityFluidSpring) tile).getZ();
                        if (matID != 0) {
                            scannedOres.add(new OreData(x, y, z, matID));
                            oresFound++;
                        }
                    }
                    if (tile instanceof MultiTileEntityRock && mode == ScanMode.ROCK) {
                        short matID = OM.anydata_(((MultiTileEntityRock) tile).mRock).mMaterial.mMaterial.mID;
                        int x = ((MultiTileEntityRock) tile).getX();
                        int y = ((MultiTileEntityRock) tile).getY();
                        int z = ((MultiTileEntityRock) tile).getZ();
                        this.scannedOres.add(new OreData(x, y, z, matID));
                        this.oresFound++;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
