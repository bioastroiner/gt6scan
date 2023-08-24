package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.network.PacketScannerData;
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
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.UT;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bioast.mods.gt6m.GT6M_Mod.LOG;
import static bioast.mods.gt6m.scanner.utils.HLPs.*;
import static com.cleanroommc.modularui.drawable.BufferBuilder.bufferbuilder;

public class ScannerMultiTool extends MultiItemTool implements IItemGuiHolder {

    int chunkSize = 9;
    int oresFound = 0;
    boolean hasDrawn = false;
    Chunk[][] chunks = new Chunk[chunkSize][chunkSize];
    int[][] block = new int[16 * chunkSize][16 * chunkSize]; // store color
    OreDictMaterial[][] mats = new OreDictMaterial[16 * chunkSize][16 * chunkSize];

    boolean scanned_on_server;

    public ScannerMultiTool() {
        super(GT6M_Mod.MODID, VALs.SCANNER_MULTI_NAME);
        addTool(0, "Scanner", "Open it", new ScannerToolStats(6), "toolScanner");
    }

    // TODO optimize
    public static void drawRect(float x0, float y0, float w, float h, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        float x1 = x0 + w, y1 = y0 + h;
        Tessellator.instance.startDrawingQuads();
        bufferbuilder.pos(x0, y0, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
        bufferbuilder.pos(x0, y1, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
        bufferbuilder.pos(x1, y1, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
        bufferbuilder.pos(x1, y0, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
        Tessellator.instance.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        scanned_on_server = false;
        List<String> chat = new ArrayList<>();
        if (!aWorld.isRemote) {
            chat.add("--Server--");
            oresFound = 0;
            //Map tMap = aWorld.getChunkFromBlockCoords((int) aPlayer.posX, (int) aPlayer.posZ).chunkTileEntityMap;
            final int PLAYER_CHUNK_INDEX = (chunkSize + 1) / 2;
            final Chunk PLAYER_CHUNK = chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX] = aWorld.getChunkFromBlockCoords((int) aPlayer.posX, (int) aPlayer.posZ);
            chat.add(" -Player Chunk Index: " + PLAYER_CHUNK_INDEX);
            chat.add(" -Player Chunk Pos  : " + PLAYER_CHUNK.xPosition + " - " + PLAYER_CHUNK.zPosition);

            //TODO fix bound exception
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    final int CHUNK_X = (PLAYER_CHUNK.xPosition - PLAYER_CHUNK_INDEX) + i;
                    final int CHUNK_Z = (PLAYER_CHUNK.zPosition + PLAYER_CHUNK_INDEX) - j;
                    chunks[i][j] = aWorld.getChunkFromChunkCoords(CHUNK_X, CHUNK_Z);
                }
            }
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    Chunk currentChunk = chunks[i][j];
                    chat.add("Chunk (" + i + "," + j + "): " + "X: " + currentChunk.xPosition + ", Z: " + currentChunk.zPosition);
                    // do stuff with the chunks
                    Map tMap = currentChunk.chunkTileEntityMap;
                    Map<ChunkPosition, TileEntity> tTileMap;
                    if (tMap != null) {
                        try {
                            tTileMap = (Map<ChunkPosition, TileEntity>) (tMap);
                            PacketScannerData packet = new PacketScannerData();
                            tTileMap.forEach((chunkPos, tile) -> {
                                if (tile instanceof PrefixBlockTileEntity pTile) {
                                    PrefixBlock pBlock = prefixBlock(pTile);
                                    if (pBlock.mPrefix.mFamiliarPrefixes.contains(OP.ore)) {
                                        oresFound++;
                                        // it's an Ore we found One!
                                        short matID = pTile.mMetaData;
                                        int x = pTile.getX();
                                        int y = pTile.getY();
                                        int z = pTile.getZ();
                                        //packet.addOreHere(x,y,z,matID);
                                        OreDictMaterial tMaterial = mat(pTile);
                                        // do the rest of logic over Client
                                        MapOre.from(pTile.getX(), pTile.getY(), pTile.getZ(), tMaterial);
                                    }
                                }
                            });
                            //CommonProxy.NetwrokHandler.sendToPlayer(packet, (EntityPlayerMP) aPlayer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            GuiInfos.PLAYER_ITEM_MAIN_HAND.open(aPlayer);
        }
        chat.add("Found " + oresFound + " Ores.");
        UT.Entities.sendchat(aPlayer, chat, false);
        return super.onItemRightClick(aStack, aWorld, aPlayer);
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack itemStack) {
        //guiSyncHandler.syncValue(0,SyncHandlers.)

    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        hasDrawn = false;
        return ModularScreen.simple("ores_screen", guiContext -> {
            ModularPanel panel = ModularPanel.defaultPanel(guiContext);
            LOG.info(player.worldObj.getChunkFromBlockCoords((int) player.posX, (int) player.posZ).getChunkCoordIntPair());
            ChunkCoordIntPair centerChunk = player.worldObj.getChunkFromBlockCoords((int) player.posX, (int) player.posZ).getChunkCoordIntPair();
            World world = player.getEntityWorld();
            int playerX = (int) player.posX;
            int playerZ = (int) player.posZ;
            int borderColor = col(MT.LightGray);
            int backgroundColor = col(MT.White);
            int oreColor = 0;
            boolean smallOres;
            ChunkCoordIntPair topLeftChunk = new ChunkCoordIntPair(centerChunk.chunkXPos - 4, centerChunk.chunkZPos + 4);
            // the first 0,0 pixel on top left corner to real world coordinates
            int x0 = topLeftChunk.func_151349_a(0).chunkPosX;
            int z0 = topLeftChunk.func_151349_a(0).chunkPosZ;
            int playerXGui = Math.abs(playerX - x0);
            int playerZGui = Math.abs(playerZ - z0);
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
                            boolean isOre = false;
                            xc = x + chunkOffsetX;
                            zc = z + chunkOffsetZ;
                            // Here we assign colors to blocks ALL BLOCKS GET COLOR
                            //TODO
                            block[xc][zc] = backgroundColor;
                            if (isOre) {
                                block[xc][zc] = oreColor;
                            }
                            if (x == 15 || z == 15) // We Skip 16th block to draw Borders
                                block[xc][zc] = borderColor;
                        }
                    }
                }
            }
            Widget mapWidget = new IDrawable() {
                @Override
                public void draw(GuiContext context, int x, int y, int width, int height) {
                    //if(hasDrawn) return;
                    for (int i = 0; i < block.length; i++) {
                        for (int j = 0; j < block[i].length; j++) {
                            if (block[i][j] == col(MT.LightGray)) continue;
                            GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
                            if (i == playerXGui && j == playerZGui)
                                GuiDraw.drawBorder(i, j, 2f, 2f, Color.argb(1f, 0f, 0f, 0.3f), 0.5f);
                        }
                    }
                    hasDrawn = true;
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
//
//class Grid{
//    public int originChunkX;
//    public int originChunkZ;
//    /*
//        (0,0)-----------------> +x
//        |
//        |
//        |
//        |          (.) center
//        |
//        |
//       \./                     (sizeX,sizeY)
//        +z
//         */
//    public int sizeX,sizeZ;
//    public int xOrigin,zOrigin;
//    public int centerX,centerZ;
//    public int centerChunkX,centerChunkZ;
////    public int worldX(int gridX){
////
////    }
//
//    public Grid(Chunk centerChunk){
//        //centerX=xWorldAt;centerZ=zWorldAt;
//
//        centerChunkX = centerChunk.getChunkCoordIntPair().chunkXPos;
//        centerChunkZ = centerChunk.getChunkCoordIntPair().chunkZPos;
//
//        originChunkX = centerChunkX - 4;
//        originChunkZ = centerChunkZ + 4;
//
////        ChunkCoordIntPair topLeftChunk = new ChunkCoordIntPair(centerChunk.chunkXPos-4,centerChunk.chunkZPos+4);
//        // the first 0,0 pixel on top left corner to real world coordinates
////        int x0 = topLeftChunk.func_151349_a(0).chunkPosX;
////        int z0 = topLeftChunk.func_151349_a(0).chunkPosZ;
////        int playerXGui = Math.abs(playerX - x0);
////        int playerZGui = Math.abs(playerZ - z0);
//    }
//
//}

class MapOre {
    public static final Map<OreDictMaterial, List<MapOre>> REGISTERED_ORES = new HashMap<>();
    int x;
    int y;
    int z;
    OreDictMaterial material;

    public MapOre(int x, int y, int z, OreDictMaterial mat) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = mat;
        List<MapOre> list = new ArrayList<>();
        if (!REGISTERED_ORES.containsKey(mat)) {
            list.add(this);
            REGISTERED_ORES.put(mat, list);
        } else {
            REGISTERED_ORES.get(mat).add(this);
        }
    }

    public static MapOre from(int x, int y, int z, OreDictMaterial mat) {
        return new MapOre(x, y, z, mat);
    }

    public static void clean() {
        REGISTERED_ORES.clear();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public OreDictMaterial mat() {
        return material;
    }

    public void setMaterial(OreDictMaterial material) {
        this.material = material;
    }

    public int color() {
        return col(material);
    }

    public List<MapOre> similarOres() {
        return REGISTERED_ORES.get(material);
    }

    public int howMany() {
        return REGISTERED_ORES.get(material).size();
    }
}

