package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.scanner.item.ScannerToolStats;
import bioast.mods.gt6m.scanner.utils.VALs;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregapi.data.MT;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.util.UT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ScannerMultiTool extends MultiItemTool implements IItemGuiHolder {
    public static ScannerMultiTool INSTANCE;

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
            ModularPanel panel = new ModularPanel(guiContext).relativeToScreen().pos(5, 5).size(470, 200);
            // TODO com.cleanroommc.modularui.drawable.Rectangle for each block is BAD for Performance
            List<List<IWidget>> matrix = new ArrayList<>();
            int chunkSize = 10;
            int[][] chunk = new int[chunkSize][chunkSize];
            Rectangle[][] block = new Rectangle[16 * chunkSize][16 * chunkSize];
            IWidget[][] blockW = new IWidget[16 * chunkSize][16 * chunkSize];
            Grid grid = new Grid().margin(0).minColWidth(0).minRowHeight(0);
            // Draw each Chunk
            for (int cy = 0; cy < chunkSize; cy++) {
                for (int cx = 0; cx < chunkSize; cx++) {
                    int chunkOffsetX = cx * 16;
                    int chunkOffsetY = cy * 16;
                    for (int j = 0; j < 16; j++) { // j -> columns
                        for (int i = 0; i < 16; i++) { // i -> rows // we like to iterate rows first
                            int ic, jc;
                            ic = i + chunkOffsetX;
                            jc = j + chunkOffsetY;
                            // We Skip 16th block to draw Borders
                            block[ic][jc] = new Rectangle().setColor(UT.Code.getRGBaInt(MT.As.mRGBaSolid));
                            if (i == 15 || j == 15)
                                block[ic][jc] = new Rectangle().setColor(UT.Code.getRGBaInt(MT.Rubber.mRGBaSolid));
                            blockW[ic][jc] = block[ic][jc].asWidget().size(1, 1);
                        }
                    }
                }
            }
            for (int i = 0; i < blockW.length; i++) {
                List<IWidget> row = new ArrayList<>();
                for (int j = 0; j < blockW[i].length; j++) {
                    IWidget widget = blockW[j][i];
                    row.add(widget);
                }
                matrix.add(row);
            }
            grid.matrix(matrix);
            panel.child(grid
                .pos(10, 10).right(10).bottom(10));
            return panel;
        });
    }
}
