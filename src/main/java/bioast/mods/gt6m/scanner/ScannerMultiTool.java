package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.GT6M_Mod;
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
import com.cleanroommc.modularui.widget.Widget;
import gregapi.data.MT;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.util.UT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
        GT6M_Mod.LOG.info(getUnlocalizedName());
        return super.onItemRightClick(aStack, aWorld, aPlayer);
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack itemStack) {
    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        return ModularScreen.simple("ores_screen", guiContext -> {
            ModularPanel panel = ModularPanel.defaultPanel(guiContext);
            int chunkSize = 9;
            int[][] block = new int[16 * chunkSize][16 * chunkSize]; // store color
            for (int cy = 0; cy < chunkSize; cy++) {
                for (int cx = 0; cx < chunkSize; cx++) {
                    int chunkOffsetX = cx * 16;
                    int chunkOffsetY = cy * 16;
                    for (int y = 0; y < 16; y++) { // j -> columns
                        for (int x = 0; x < 16; x++) { // i -> rows // we like to iterate rows first
                            int xc, yc;
                            xc = x + chunkOffsetX;
                            yc = y + chunkOffsetY;
                            // We Skip 16th block to draw Borders
                            block[xc][yc] = UT.Code.getRGBaInt(MT.As.mRGBaSolid);
                            if (x == 15 || y == 15)
                                block[xc][yc] = UT.Code.getRGBaInt(MT.Rubber.mRGBaSolid);
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
}
