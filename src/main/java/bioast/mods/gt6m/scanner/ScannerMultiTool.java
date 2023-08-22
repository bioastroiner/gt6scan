package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.scanner.item.ScannerToolStats;
import bioast.mods.gt6m.scanner.utils.HLPs;
import bioast.mods.gt6m.scanner.utils.VALs;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;
import gregapi.data.MT;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.UT;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

import static bioast.mods.gt6m.GT6M_Mod.LOG;
import static bioast.mods.gt6m.scanner.utils.HLPs.col;
import static bioast.mods.gt6m.scanner.utils.HLPs.oresLarge;

public class ScannerMultiTool extends MultiItemTool implements IItemGuiHolder {
    public ScannerMultiTool() {
        super(GT6M_Mod.MODID, VALs.SCANNER_MULTI_NAME);
        addTool(0, "Scanner", "Open it", new ScannerToolStats(6), "toolScanner");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        if (!aWorld.isRemote) {
            GuiInfos.PLAYER_ITEM_MAIN_HAND.open(aPlayer);
        }
        LOG.info(getUnlocalizedName());
        return super.onItemRightClick(aStack, aWorld, aPlayer);
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack itemStack) {
    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        return ModularScreen.simple("ores_screen", guiContext -> {
            ModularPanel panel = ModularPanel.defaultPanel(guiContext);
            LOG.info(player.worldObj.getChunkFromBlockCoords((int) player.posX, (int) player.posZ).getChunkCoordIntPair());
            ChunkCoordIntPair centerChunk = player.worldObj.getChunkFromBlockCoords((int) player.posX, (int) player.posZ).getChunkCoordIntPair();
            World world = player.getEntityWorld();
            int playerX = (int) player.posX;
            int playerZ = (int) player.posZ;
            int borderColor = col(MT.C);
            int backgroundColor = col(MT.As);
            int oreColor;
            boolean smallOres;
            ChunkCoordIntPair topLeftChunk = new ChunkCoordIntPair(centerChunk.chunkXPos-4,centerChunk.chunkZPos-4);
            // the first 0,0 pixel on top left corner to real world coordinates
            int x0 = topLeftChunk.func_151349_a(0).chunkPosX;
            int z0 = topLeftChunk.func_151349_a(0).chunkPosZ;
            int playerXGui = Math.abs(playerX - x0);
            int playerZGui = Math.abs(playerZ - z0);

            int chunkSize = 9; // should be odd for eas
            //Chunk[][] chunks = new Chunk[chunkSize][chunkSize];

            int[][] block = new int[16 * chunkSize][16 * chunkSize]; // store color
            for (int cz = 0; cz < chunkSize; cz++) {
                for (int cx = 0; cx < chunkSize; cx++) {
                    //Chunk chunk = chunks[cx][cz];
                    //Map<ChunkPosition, OreDictMaterial> map = oresLarge(chunk);
                    //TODO
                    int chunkOffsetX = cx * 16;
                    int chunkOffsetZ = cz * 16;
                    for (int z = 0; z < 16; z++) { // j -> columns
                        for (int x = 0; x < 16; x++) { // i -> rows // we like to iterate rows first
                            int xc, zc;
                            xc = x + chunkOffsetX;
                            zc = z + chunkOffsetZ;
                            // Here we assign colors to blocks ALL BLOCKS GET COLOR
                            //TODO
                            block[xc][zc] = col(MT.White);
                            if (x == 15 || z == 15) // We Skip 16th block to draw Borders
                                block[xc][zc] = col(MT.LightGray);
                        }
                    }
                }
            }
            Widget mapWidget = new IDrawable() {
                @Override
                public void draw(GuiContext context, int x, int y, int width, int height) {
                    for (int i = 0; i < block.length; i++) {
                        for (int j = 0; j < block[i].length; j++) {
                            GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
                            if(i==playerXGui&&j==playerZGui)
                                GuiDraw.drawRect(i,j,2f,2f, Color.argb(1f,0f,0f,0.3f));
                        }
                    }
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

    public class MapperSyncHandler extends GuiSyncHandler{
        public MapperSyncHandler(EntityPlayer player) {
            super(player);
        }
    }
}
