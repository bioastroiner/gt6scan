package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.network.DataOre;
import bioast.mods.gt6m.network.OreSyncHandler;
import bioast.mods.gt6m.scanner.item.ScannerToolStats;
import bioast.mods.gt6m.scanner.utils.VALs;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.UT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bioast.mods.gt6m.scanner.utils.HLPs.col;
import static bioast.mods.gt6m.scanner.utils.HLPs.prefixBlock;

public class ScannerMultiTool extends MultiItemTool implements IItemGuiHolder {
    List<DataOre> scannedOres = new ArrayList<>();
    int chunkSize = 9;
    int oresFound = 0;
    int x_origin;
    int z_origin;

    Chunk[][] chunks = new Chunk[chunkSize][chunkSize];
    int[][] block = new int[16 * chunkSize][16 * chunkSize]; // store color

    public ScannerMultiTool() {
        super(GT6M_Mod.MODID, VALs.SCANNER_MULTI_NAME);
        addTool(0, "Scanner", "Open it", new ScannerToolStats(6), "toolScanner");
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack itemStack) {
        //guiSyncHandler.syncValue(0,SyncHandlers.)
        guiSyncHandler.syncValue(0, new OreSyncHandler(() -> this.scannedOres, value -> this.scannedOres = value));
        guiSyncHandler.syncValue(1, SyncHandlers.intNumber(() -> this.oresFound, val -> this.oresFound = val));
        guiSyncHandler.syncValue(2, SyncHandlers.intNumber(() -> this.chunkSize, val -> this.chunkSize = val));
        guiSyncHandler.syncValue(3, SyncHandlers.intNumber(() -> this.x_origin, val -> this.x_origin = val));
        guiSyncHandler.syncValue(4, SyncHandlers.intNumber(() -> this.z_origin, val -> this.z_origin = val));

    }

    @Override
    public ItemStack onItemRightClick(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        List<String> chat = new ArrayList<>();
        if (!aWorld.isRemote) {
            this.scannedOres.clear();
            this.oresFound = 0;
            chat.add("--Server--");
            final int PLAYER_CHUNK_INDEX = (chunkSize - 1) / 2;
            final Chunk PLAYER_CHUNK = chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX] = aWorld.getChunkFromBlockCoords((int) aPlayer.posX, (int) aPlayer.posZ);
            chat.add(" -Player Chunk Index: " + PLAYER_CHUNK_INDEX);
            chat.add(" -Player Chunk Pos  : " + PLAYER_CHUNK.xPosition + " - " + PLAYER_CHUNK.zPosition);
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    final int CHUNK_X = (PLAYER_CHUNK.xPosition - PLAYER_CHUNK_INDEX) + i;
                    final int CHUNK_Z = (PLAYER_CHUNK.zPosition + PLAYER_CHUNK_INDEX) - j;
                    chunks[i][j] = aWorld.getChunkFromChunkCoords(CHUNK_X, CHUNK_Z);
                }
            }
            x_origin = chunks[0][0].getChunkCoordIntPair().getCenterXPos() - 7;
            z_origin = chunks[0][0].getChunkCoordIntPair().getCenterZPosition() - 8;
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    Chunk currentChunk = chunks[i][j];
                    chat.add("Chunk (" + i + "," + j + "): " + "X: " + currentChunk.xPosition + ", Z: " + currentChunk.zPosition);
                    Map tMap = currentChunk.chunkTileEntityMap;
                    Map<ChunkPosition, TileEntity> tTileMap;
                    if (tMap != null) {
                        try {
                            tTileMap = (Map<ChunkPosition, TileEntity>) (tMap);
                            tTileMap.forEach((chunkPos, tile) -> {
                                if (tile instanceof PrefixBlockTileEntity pTile) {
                                    PrefixBlock pBlock = prefixBlock(pTile);
                                    if (pBlock.mPrefix.mFamiliarPrefixes.contains(OP.ore)) {
                                        short matID = pTile.mMetaData;
                                        int x = pTile.getX();
                                        int y = pTile.getY();
                                        int z = pTile.getZ();
                                        this.scannedOres.add(new DataOre(x, y, z, matID));
                                        this.oresFound++;
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            /* CLIENT CODE */
            int borderColor = col(MT.Gray);
            int backgroundColor = col(MT.White);
            int oreColor = 0;
            boolean smallOres;
            for (int chunkGridX = 0; chunkGridX < chunkSize; chunkGridX++) {
                for (int chunkGridZ = 0; chunkGridZ < chunkSize; chunkGridZ++) {
                    int chunkOffsetX = chunkGridZ * 16;
                    int chunkOffsetZ = chunkGridX * 16;
                    for (int z = 0; z < 16; z++) { // j -> columns
                        for (int x = 0; x < 16; x++) { // i -> rows // we like to iterate rows first
                            int blockGridX, blockGridZ;
                            boolean isOre = false;
                            blockGridX = x + chunkOffsetX;
                            blockGridZ = z + chunkOffsetZ;
                            int blockWorldX = x_origin + chunkGridZ * 16 + blockGridX;
                            int blockWorldZ = z_origin - chunkGridX * 16 - blockGridZ;
                            int lastY = 0;
                            try {
                                if (scannedOres != null && !scannedOres.isEmpty())
                                    for (DataOre data : scannedOres) {
                                        if (data.x == blockWorldX && data.z == blockWorldZ) {
                                            if (lastY < data.y) {
                                                oreColor = col(OreDictMaterial.MATERIAL_ARRAY[data.matID]);
                                                lastY = data.y;
                                            }
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
                            if (x == 15 || z == 15) // We Skip 16th block to draw Borders
                                block[blockGridX][blockGridZ] = borderColor;
                            if (chunkGridZ == 4 && chunkGridX == 4 && x == 7 && z == 7)
                                block[blockGridX][blockGridZ] = col(MT.Red);
                        }
                    }
                }
            }
            GuiInfos.PLAYER_ITEM_MAIN_HAND.open(aPlayer);
        }
        chat.add("Found " + oresFound + " Ores.");
        UT.Entities.sendchat(aPlayer, chat, false);
        return super.onItemRightClick(aStack, aWorld, aPlayer);
    }


    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        return ModularScreen.simple("ores_screen", guiContext -> {
            ModularPanel panel = ModularPanel.defaultPanel(guiContext);
            Widget mapWidget = new IDrawable() {
                @Override
                public void draw(GuiContext context, int x, int y, int width, int height) {
                    //if(hasDrawn) return;
                    for (int i = 0; i < block.length; i++) {
                        for (int j = 0; j < block[i].length; j++) {
                            if (block[i][j] == col(MT.White)) continue;
                            GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
//                            if (i == playerXGui && j == playerZGui)
//                                GuiDraw.drawBorder(i, j, 2f, 2f, Color.argb(1f, 0f, 0f, 0.3f), 0.5f);
                        }
                    }
                    //GuiDraw.drawDropCircleShadow();
                }

                @Override
                public Widget<?> asWidget() {
                    return IDrawable.super.asWidget();
                }
            }.asWidget().pos(10, 10).right(10).bottom(10);
            panel.child(mapWidget);
            return panel;
        });
    }

}

