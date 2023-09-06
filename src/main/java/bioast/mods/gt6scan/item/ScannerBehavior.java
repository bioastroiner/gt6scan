package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.ScannerMod;
import bioast.mods.gt6scan.network.OreData;
import bioast.mods.gt6scan.network.OreDataSyncHandler;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.manager.GuiInfo;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.CS;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.TD;
import gregapi.item.multiitem.MultiItem;
import gregapi.item.multiitem.behaviors.IBehavior;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.OM;
import gregapi.util.UT;
import gregtech.blocks.stone.BlockCrystalOres;
import gregtech.blocks.stone.BlockRockOres;
import gregtech.blocks.stone.BlockVanillaOresA;
import gregtech.tileentity.misc.MultiTileEntityFluidSpring;
import gregtech.tileentity.placeables.MultiTileEntityRock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bioast.mods.gt6scan.utils.HLPs.col;
import static bioast.mods.gt6scan.utils.HLPs.prefixBlock;

public class ScannerBehavior extends IBehavior.AbstractBehaviorDefault implements IItemGuiHolder {
    //Mode mode = Mode.LARGE;
    List<OreData> scannedOres = new ArrayList<>();
    Map<Short, Integer> sortedOres = new HashMap<>();
    int chunkSize = 9;
    int oresFound = 0;
    int x_origin;
    int z_origin;

    /* CLIENT ONLY*/ int[][] block = new int[16 * chunkSize][16 * chunkSize]; // store color
    /* CLIENT ONLY*/ short[][] blockMat = new short[16 * chunkSize][16 * chunkSize]; // like blocks but stores materialID

    public ScannerBehavior(int sizeIn) {
        chunkSize = sizeIn;
    }

    private static Chunk[][] getChunksAroundPlayer(World aWorld, EntityPlayer aPlayer, int chunkSize) {
        Chunk[][] chunks = new Chunk[chunkSize][chunkSize];
        final int PLAYER_CHUNK_INDEX = (chunkSize - 1) / 2;
        chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX] = aWorld.getChunkFromBlockCoords((int) aPlayer.posX, (int) aPlayer.posZ);
        final Chunk PLAYER_CHUNK = chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX];
        chunks[0][0] = aWorld.getChunkFromChunkCoords(PLAYER_CHUNK.xPosition - ((chunkSize - 1) / 2), PLAYER_CHUNK.zPosition - ((chunkSize - 1) / 2));
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                if (i == 0 && j == 0) continue;
                if (i == PLAYER_CHUNK_INDEX && j == PLAYER_CHUNK_INDEX) continue;
                chunks[i][j] = aWorld.getChunkFromChunkCoords(chunks[0][0].xPosition + i, chunks[0][0].zPosition + j);
            }
        }
        return chunks;
    }

    private IWidget wItem(OreDictMaterial mat, ScanMode mode) {
        OreDictPrefix prefix = OP.oreRaw;
        if (mode == ScanMode.SMALL) prefix = OP.crushed;
        if (mode == ScanMode.FLUID) prefix = OP.bucket;
        if (mode == ScanMode.ROCK) prefix = OP.rockGt;
        return new ItemDrawable().setItem(prefix.dat(mat).getStack(1)).asWidget();
    }

    public ItemStack changeMode(EntityPlayer aPlayer, ItemStack aStack, ScanMode currentMode) {
        //Switch mode
        int nextMode = currentMode.ordinal() + 1;
        if (nextMode >= ScanMode.values().length) nextMode = 0;
        try {
            currentMode = ScanMode.values()[nextMode];
        } catch (ArrayIndexOutOfBoundsException e) {
            currentMode = ScanMode.LARGE;
        }
        UT.NBT.set(aStack, UT.NBT.makeInt("mode", nextMode));
        UT.Entities.sendchat(aPlayer, "Mode: " + currentMode.name());
        return aStack;
    }

    public ScanMode getMode(ItemStack aStack) {
        NBTTagCompound tag = UT.NBT.getOrCreate(aStack);
        if (!tag.hasKey("mode")) {
            UT.NBT.makeInt(tag, 0);
            UT.NBT.set(aStack, tag);
            return ScanMode.LARGE;
        } else {
            return ScanMode.values()[tag.getInteger("mode")];
        }
    }

    @Override
    public ItemStack onItemRightClick(MultiItem aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        UT.Sounds.play(CS.SFX.IC_SCANNER, 20, 1.0F, aPlayer);
        if (!UT.Entities.isCreative(aPlayer)) {
            if (aItem.getEnergyStored(TD.Energy.LU, aStack) < CS.V[6]) return aStack;
        }
        List<String> chat = new ArrayList<>();
        List<String> chat_debug = new ArrayList<>();
        if (aStack != null && (aPlayer == null || aPlayer.isSneaking()) && !aWorld.isRemote) {
            changeMode(aPlayer, aStack, getMode(aStack));
            return aStack;
        }
        if (!aWorld.isRemote) {
            serverLogic(aStack, aWorld, aPlayer, chat_debug);
            if (!UT.Entities.isCreative(aPlayer))
                aItem.useEnergy(TD.Energy.LU, aStack, sortedOres.keySet().size() * CS.V[6] * Math.min(oresFound / 100, 30), aPlayer, aPlayer.inventory, aWorld, (int) aPlayer.posX, (int) aPlayer.posY, (int) aPlayer.posZ, !UT.Entities.isCreative(aPlayer));
        }
        //chat.add("Booting Up the Device In " + mode.name() + " mode...");
        //chat.add("Found " + oresFound + " Ores.");
        UT.Entities.sendchat(aPlayer, chat, false);
        //UT.Entities.sendchat(aPlayer, chat_debug, false);
        return aStack;
    }

    @Override
    public List<String> getAdditionalToolTips(MultiItem aItem, List<String> aList, ItemStack aStack) {
        aList.add("Shift Right Click to Change Mode.");
        aList.add("Right Click to Open GUI.");
        aList.add(getMode(aStack) + " Mode.");
        //aList.add(oresFound + " many found.");
        return aList;
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player, ItemStack itemStack) {
        guiSyncHandler.syncValue(0, new OreDataSyncHandler(() -> this.scannedOres, value -> this.scannedOres = value));
        guiSyncHandler.syncValue(1, SyncHandlers.intNumber(() -> this.oresFound, val -> this.oresFound = val));
        guiSyncHandler.syncValue(2, SyncHandlers.intNumber(() -> this.chunkSize, val -> this.chunkSize = val));
        guiSyncHandler.syncValue(3, SyncHandlers.intNumber(() -> this.x_origin, val -> this.x_origin = val));
        guiSyncHandler.syncValue(4, SyncHandlers.intNumber(() -> this.z_origin, val -> this.z_origin = val));
        //guiSyncHandler.syncValue(5, SyncHandlers.enumValue(Mode.class, () -> this.mode, val -> this.mode = val));
    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        ScanMode mode = getMode(itemStack);
        clientLogic();
        return ModularScreen.simple("map", guiContext -> {
            ModularPanel panel = ModularPanel.defaultPanel(guiContext);
            panel.flex().align(Alignment.Center).size(300, 166);
            if(ScannerMod.config.get("client","fullScreen",false)) panel.flex().full();
            IWidget mapWidget = ((IDrawable) (context, x, y, width, height) -> {
                // TODO get rid of this for loop it would be a HUGE deal to optimization
                for (int i = 0; i < 16 * chunkSize; i++) {
                    for (int j = 16 * chunkSize - 1; j >= 0; j--) {
                        if (block[i][j] == col(MT.White)) continue;
                        GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
                    }
                }
                String corX = "X -> " + (guiContext.getAbsMouseX() - x + x_origin - 9);
                String corZ = "Z -> " + (guiContext.getAbsMouseY() - y + z_origin - 9);
                String corN = " ";
                int text_color = col(MT.White);
                try {
                    short mat = blockMat[(guiContext.getAbsMouseX() - x - 10)][(guiContext.getAbsMouseY() - y - 10)];
                    if (mat != 0) {
                        corN = OreDictMaterial.MATERIAL_ARRAY[mat].mNameLocal;
                        text_color = col(OreDictMaterial.MATERIAL_ARRAY[mat]);
                    } else corN = "NaN";
                } catch (Exception e) {
                    corN = "NaN";
                }
                String cor = corX + " , " + corZ + " , " + corN;
                GuiDraw.drawText(cor, 0, -10, 1f, text_color, true);
            }).asWidget().pos(10, 10).right(10).bottom(10);
            Grid listWidget = new Grid().scrollable().size(150, 150).pos(280 - 125, 0);
            sortedOres.forEach((matID, amount) -> {
                OreDictMaterial mat = OreDictMaterial.MATERIAL_ARRAY[matID];
                IWidget itemWidget = wItem(mat, mode);
                IWidget nameWidget = new TextWidget(IKey.str(mat.mNameLocal + ": " + amount)).color(UT.Code.getRGBaInt((mat.fRGBaSolid))).shadow(true);
                listWidget.row(itemWidget, nameWidget).nextRow();
            });
            panel.child(mapWidget);
            panel.child(listWidget);
            return panel;
        });
    }

    private void serverLogic(ItemStack aStack, World aWorld, EntityPlayer aPlayer, List<String> chat) {
        //TODO server logic optimazation
        ScanMode mode = getMode(aStack);
        this.scannedOres.clear();
        this.oresFound = 0;
        Chunk[][] chunks = getChunksAroundPlayer(aWorld, aPlayer, chunkSize);
        x_origin = (chunks[0][0].xPosition << 4);
        z_origin = (chunks[0][0].zPosition << 4);
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                if (mode.isTE()) findTileEntityBlocks(chunks[i][j], mode);
                else {
                    for (int k = 0; k < 16; k++) {
                        for (int l = 0; l < 16; l++) {
                            //TODO check packet size
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
                                    }
//									else if(block1 instanceof BlockDiggable){
//										BlockDiggable.IS_CLAY
//									}
                                }
                            }
                        }
                    }
                }
            }
        }
        /*Handles Server to Client Communication*/
        final GuiInfo gui = GuiInfo.builder().clientGui(context -> {
            return createGuiScreen(context.getPlayer(), aStack);
        }).serverGui((context, guiSyncHandler) -> {
            this.buildSyncHandler(guiSyncHandler, context.getPlayer(), aStack);
        }).build();
        gui.open(aPlayer);
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
                        if (isBedrock || pBlock.mPrefix.mFamiliarPrefixes.contains(mode.OP)) {
                            short matID = pTile.mMetaData;
                            int x = pTile.getX();
                            int y = pTile.getY();
                            int z = pTile.getZ();
                            this.scannedOres.add(new OreData(x, y, z, matID));
                            this.oresFound++;
                        }
                    }
                    if (mode == ScanMode.FLUID && tile instanceof MultiTileEntityFluidSpring) {
                        short matID = OreDictMaterial.FLUID_MAP.get(((MultiTileEntityFluidSpring) tile).mFluid.getFluid().getName()).mMaterial.mID;
                        int x = ((MultiTileEntityFluidSpring) tile).getX();
                        int y = ((MultiTileEntityFluidSpring) tile).getY();
                        int z = ((MultiTileEntityFluidSpring) tile).getZ();
                        this.scannedOres.add(new OreData(x, y, z, matID));
                        this.oresFound++;
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

    private void clientLogic() {
        ScannerMod.debug.info(x_origin);
        ScannerMod.debug.info(z_origin);

        /* CLIENT CODE */
        blockMat = new short[chunkSize * 16][chunkSize * 16];
        int borderColor = col(MT.Gray);
        int backgroundColor = col(MT.White);
        int oreColor = 0;
        sortedOres.clear();
        for (int chunkGridX = 0; chunkGridX < chunkSize; chunkGridX++) {
            for (int chunkGridZ = 0; chunkGridZ < chunkSize; chunkGridZ++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int blockGridX, blockGridZ;
                        boolean isOre = false;
                        blockGridX = x + chunkGridX * 16;
                        blockGridZ = z + chunkGridZ * 16;
                        int blockWorldX = x_origin + blockGridX;
                        int blockWorldZ = z_origin + blockGridZ;
                        int lastY = 0;
                        try {
                            if (scannedOres != null && !scannedOres.isEmpty()) for (OreData data : scannedOres) {
                                if (data.x == blockWorldX && data.z == blockWorldZ) {
                                    if (lastY <= data.y) {
                                        oreColor = col(OreDictMaterial.MATERIAL_ARRAY[data.matID]);
                                        lastY = data.y;
                                    }
                                    if (sortedOres.containsKey(data.matID)) {
                                        int lastCount = sortedOres.get(data.matID);
                                        sortedOres.put(data.matID, lastCount + 1);
                                    } else sortedOres.put(data.matID, 1);
                                    blockMat[blockGridX][blockGridZ] = data.matID;
                                    isOre = true;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        block[blockGridX][blockGridZ] = backgroundColor;
                        if (isOre) {
                            block[blockGridX][blockGridZ] = oreColor;
                        }
                        if (x == 15 || z == 15 || x == 0 || z == 0) // We Skip 16th block to draw Borders
                            block[blockGridX][blockGridZ] = borderColor;
                        if (chunkGridZ == (chunkSize - 1) / 2 && chunkGridX == (chunkSize - 1) / 2 && x == 7 && z == 7)
                            block[blockGridX][blockGridZ] = col(MT.Red);
                    }
                }
            }
        }
    }
}
