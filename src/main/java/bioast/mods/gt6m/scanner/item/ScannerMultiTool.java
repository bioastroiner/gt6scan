package bioast.mods.gt6m.scanner.item;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.scanner.network.OreData;
import bioast.mods.gt6m.scanner.network.OreDataSyncHandler;
import bioast.mods.gt6m.scanner.utils.VALs;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
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
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bioast.mods.gt6m.scanner.utils.HLPs.col;
import static bioast.mods.gt6m.scanner.utils.HLPs.prefixBlock;

public class ScannerMultiTool extends MultiItemTool implements IItemGuiHolder {
    List<OreData> scannedOres = new ArrayList<>();
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
        guiSyncHandler.syncValue(0, new OreDataSyncHandler(() -> this.scannedOres, value -> this.scannedOres = value));
        guiSyncHandler.syncValue(1, SyncHandlers.intNumber(() -> this.oresFound, val -> this.oresFound = val));
        guiSyncHandler.syncValue(2, SyncHandlers.intNumber(() -> this.chunkSize, val -> this.chunkSize = val));
        guiSyncHandler.syncValue(3, SyncHandlers.intNumber(() -> this.x_origin, val -> this.x_origin = val));
        guiSyncHandler.syncValue(4, SyncHandlers.intNumber(() -> this.z_origin, val -> this.z_origin = val));

    }

    @Override
    public ItemStack onItemRightClick(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        List<String> chat = new ArrayList<>();
        if (!aWorld.isRemote) serverLogic(aStack, (WorldServer) aWorld, aPlayer, chat);
        else clientLogic(aStack, (WorldClient) aWorld, aPlayer);
        chat.add("Found " + oresFound + " Ores.");
        UT.Entities.sendchat(aPlayer, chat, false);
        return super.onItemRightClick(aStack, aWorld, aPlayer);
    }

    private void clientLogic(ItemStack aStack, WorldClient aWorld, EntityPlayer aPlayer) {
        /* CLIENT CODE */
        int borderColor = col(MT.Gray);
        int backgroundColor = col(MT.White);
        int oreColor = 0;
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
                            if (scannedOres != null && !scannedOres.isEmpty())
                                for (OreData data : scannedOres) {
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
                        if (x == 15 || z == 15 || x == 0 || z == 0) // We Skip 16th block to draw Borders
                            block[blockGridX][blockGridZ] = borderColor;
                        if (chunkGridZ == (chunkSize - 1) / 2 && chunkGridX == (chunkSize - 1) / 2 && x == 7 && z == 7)
                            block[blockGridX][blockGridZ] = col(MT.Red);
                    }
                }
            }
        }
        GuiInfos.PLAYER_ITEM_MAIN_HAND.open(aPlayer);
    }

    private void serverLogic(ItemStack aStack, WorldServer aWorld, EntityPlayer aPlayer, List<String> chat) {
        this.scannedOres.clear();
        this.oresFound = 0;
        chat.add("--Server--");
        final int PLAYER_CHUNK_INDEX = (chunkSize - 1) / 2;
        chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX] = aWorld.getChunkFromBlockCoords((int) aPlayer.posX, (int) aPlayer.posZ);
        final Chunk PLAYER_CHUNK = chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX];
        chunks[0][0] = aWorld.getChunkFromChunkCoords(PLAYER_CHUNK.xPosition - ((chunkSize - 1) / 2), PLAYER_CHUNK.zPosition - ((chunkSize - 1) / 2));
        chat.add(" -Player Chunk Index: " + PLAYER_CHUNK_INDEX);
        chat.add(" -Player Chunk Pos  : " + PLAYER_CHUNK.xPosition + " - " + PLAYER_CHUNK.zPosition);
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                if (i == 0 && j == 0) continue;
                if (i == PLAYER_CHUNK_INDEX && j == PLAYER_CHUNK_INDEX) continue;
                chunks[i][j] = aWorld.getChunkFromChunkCoords(chunks[0][0].xPosition + i, chunks[0][0].zPosition + j);
            }
        }
        x_origin = (chunks[0][0].xPosition << 4);
        z_origin = (chunks[0][0].zPosition << 4);
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
                                    this.scannedOres.add(new OreData(x, y, z, matID));
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
    }


    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        return ModularScreen.simple("ores_screen", guiContext -> {
            ModularPanel panel = ModularPanel.defaultPanel(guiContext);
            Widget mapWidget = ((IDrawable) (context, x, y, width, height) -> {
                //if(hasDrawn) return;
                for (int i = 0; i < 16 * chunkSize; i++) {
                    for (int j = 16 * chunkSize - 1; j > 0; j--) {
                        if (block[i][j] == col(MT.White)) continue;
                        GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
                    }
                    //                        for (int j = 0; j < 16*chunkSize; j++) {
                    //                            //if (block[i][j] == col(MT.White)) continue;
                    //                            GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
                    //                        }
                }
            }).asWidget().pos(10, 10).right(10).bottom(10);
            panel.child(mapWidget);
            return panel;
        });
    }

}

