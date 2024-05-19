package bioast.mods.gt6scan.network.scanmessage;

import bioast.mods.gt6scan.network.OreData;
import bioast.mods.gt6scan.network.ScanMode;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.UT;
import journeymap.client.model.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bioast.mods.gt6scan.utils.HLPs.col;
import static bioast.mods.gt6scan.utils.ModularUIUtils.wItem;
import static com.cleanroommc.modularui.drawable.BufferBuilder.bufferbuilder;

@SideOnly(Side.CLIENT)
public class HandlerClient implements IMessageHandler<ScanResponse, IMessage>, IGuiHolder {
    int chunkSize;
    int x_origin;
    int z_origin;
    ScanMode mode = ScanMode.LARGE;
    int[][] blockColorStorage;// store color
    short[][] blockMatStorage; // like blocks but stores materialID
    Map<Short, Integer> sortedOres = new HashMap<>();
    List<OreData> scannedOres = new ArrayList<>();

    @Override
    public IMessage onMessage(ScanResponse message, MessageContext ctx) {
        if (ctx.side != Side.CLIENT) return null;

        Minecraft minecraft = Minecraft.getMinecraft();

        scannedOres = message.scannedOres;
        x_origin = message.x;
        z_origin = message.z;
        chunkSize = message.chunkSize;
        blockColorStorage = new int[16 * chunkSize][16 * chunkSize];
        blockMatStorage = new short[16 * chunkSize][16 * chunkSize];
        mode = ScanMode.values()[message.mode];
        refresh();
        GuiData data = new GuiData(minecraft.thePlayer);
        ClientGUI.open(createScreen(data, buildUI(data, new GuiSyncManager(data.getPlayer()))));
        return null;
    }

    private void refresh() {
        blockMatStorage = new short[chunkSize * 16][chunkSize * 16];
        //blockColorStorage = new int[chunkSize * 16][chunkSize * 16];
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
                                    blockMatStorage[blockGridX][blockGridZ] = data.matID;
                                    isOre = true;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        blockColorStorage[blockGridX][blockGridZ] = backgroundColor;
                        if (isOre) {
                            blockColorStorage[blockGridX][blockGridZ] = oreColor;
                        }
                        if (x == 15 || z == 15 || x == 0 || z == 0) // We Skip 16th block to draw Borders
                            blockColorStorage[blockGridX][blockGridZ] = borderColor;
                        if (chunkGridZ == (chunkSize - 1) / 2 && chunkGridX == (chunkSize - 1) / 2 && x == 7 && z == 7)
                            blockColorStorage[blockGridX][blockGridZ] = col(MT.Red);
                    }
                }
            }
        }
    }

    @Override
    public ModularScreen createScreen(GuiData data, ModularPanel mainPanel) {
        ItemStack itemStack = data.getMainHandItem();
        EntityPlayer player = data.getPlayer();
        mainPanel.flex().align(Alignment.Center).size(300, 166);
        IWidget mapWidget = ((IDrawable) (context, x, y, width, height, widgetTheme) -> {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            for (int i = 0; i < 16 * chunkSize; i++) {
                for (int j = 16 * chunkSize - 1; j >= 0; j--) {
                    if (blockColorStorage[i][j] == col(MT.White)) continue;
                    GL11.glShadeModel(GL11.GL_SMOOTH);
                    int color = blockColorStorage[i][j];
                    Tessellator.instance.startDrawingQuads();
                    bufferbuilder.pos(i, j, 0.0f)
                        .color(Color.getRed(color),
                            Color.getGreen(color),
                            Color.getBlue(color),
                            Color.getAlpha(color))
                        .endVertex();
                    bufferbuilder.pos(i, j + 1, 0.0f)
                        .color(Color.getRed(color),
                            Color.getGreen(color),
                            Color.getBlue(color),
                            Color.getAlpha(color))
                        .endVertex();
                    bufferbuilder.pos(i + 1, j + 1, 0.0f)
                        .color(Color.getRed(color),
                            Color.getGreen(color),
                            Color.getBlue(color),
                            Color.getAlpha(color))
                        .endVertex();
                    bufferbuilder.pos(i + 1, j, 0.0f)
                        .color(Color.getRed(color),
                            Color.getGreen(color),
                            Color.getBlue(color),
                            Color.getAlpha(color))
                        .endVertex();
                    Tessellator.instance.draw();
                }
            }
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            // convert gui coordinates to world coordinates
            int mouse_x = mainPanel.getScreen().getContext().getAbsMouseX();
            int mouse_y = mainPanel.getScreen().getContext().getAbsMouseY();
            int world_x = (mouse_x - mainPanel.getArea().x - x + x_origin - 9);
            int world_z = (mouse_y - mainPanel.getArea().y - y + z_origin - 9);
            String corX = "X -> " + world_x;
            String corZ = "Z -> " + world_z;
            String corN = " ";
            int text_color = col(MT.White);
            try {
                short mat = blockMatStorage[(mouse_x - mainPanel.getArea().x - x - 10)][(mouse_y - mainPanel.getArea().y - y - 10)];
                if (mat != 0) {
                    corN = OreDictMaterial.MATERIAL_ARRAY[mat].mNameLocal;
                    text_color = col(OreDictMaterial.MATERIAL_ARRAY[mat]);
                } else corN = "NaN";
            } catch (Exception e) {
                corN = "NaN";
            }
            if (Mouse.isButtonDown(0)) {
                // todo : bounds check
                bookmark(world_x, world_z, text_color);
            }
            if (Mouse.isButtonDown(1)) {
                if (!corN.equalsIgnoreCase("nan")) {
                    UT.Entities.sendchat(player,
                        String.format("x: %d, z: %d, has material %s", world_x, world_z, corN));
                }
            }
            String cor = corX + " , " + corZ + " , " + corN + ". Dim: " + player.worldObj.provider.getDimensionName() + ". Mode:" + mode.name()
                .toLowerCase();
            GuiDraw.drawText(cor, 0, -15, 1f, text_color, true);
        }).asWidget().pos(10, 10).right(10).bottom(10);
        Grid listWidget = new Grid().scrollable().size(150, 150).pos(280 - 125, 0);
        sortedOres.forEach((matID, amount) -> {
            OreDictMaterial mat = OreDictMaterial.MATERIAL_ARRAY[matID];
            IWidget itemWidget = wItem(mat, mode);
            String name = mat.mNameLocal;
            int color = UT.Code.getRGBaInt((mat.fRGBaSolid));
            if (mat == MT.MethaneIce) {
                color = UT.Code.getRGBaInt(MT.Milk.fRGBaSolid);
                name = "Natural Gas";
            }
            IWidget nameWidget = new TextWidget(IKey.str(name + ": " + amount)).color(color).shadow(true);
            listWidget.row(itemWidget, nameWidget).nextRow();
        });
        mainPanel.child(mapWidget);
        mainPanel.child(listWidget);
        return IGuiHolder.super.createScreen(data, mainPanel);
    }

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
        return ModularPanel.defaultPanel("Scanner");
    }

    private boolean bookmark(int world_x, int world_z, int color) {
        // bookmark on a WayPoint manager
        //todo antique atlas
        if (Loader.isModLoaded("journeymap")) {
            Waypoint waypoint = Waypoint.at(
                world_x,
                Minecraft.getMinecraft().thePlayer.serverPosY,
                world_z,
                Waypoint.Type.Normal,
                Minecraft.getMinecraft().thePlayer.dimension);
            if (color < 0) {
                waypoint.setRandomColor();
            } else {
                waypoint.setColor(color);
            }
            return true;
        } else if (Loader.isModLoaded("xaero")) {
            //todo Xaer
        }
        return false;
    }

    private boolean bookmark(int world_x, int world_z) {return bookmark(world_x, world_z, -1);}
}
