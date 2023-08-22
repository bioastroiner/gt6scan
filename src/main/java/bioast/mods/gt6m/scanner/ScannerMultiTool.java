package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.scanner.item.ScannerToolStats;
import bioast.mods.gt6m.scanner.utils.VALs;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregapi.data.MT;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.util.UT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.cleanroommc.modularui.utils.Color.*;

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
//        IItemHandlerModifiable itemHandler = new ItemStackItemHandler(itemStack, 4);
//        guiSyncHandler.registerSlotGroup("scan_items", 2);
//        guiSyncHandler.registerSlotGroup("scan_fluids", 2);
//        for (int i = 0; i < 4; i++) {
//            guiSyncHandler.syncValue("scan_items", i, SyncHandlers.itemSlot(itemHandler, i).slotGroup("scan_items"));
//            guiSyncHandler.syncValue("scan_fluids", i, SyncHandlers.itemSlot(itemHandler, i).slotGroup("scan_fluids"));
//
//        }

    }

    @Override
    public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
        return ModularScreen.simple("ores_screen", guiContext -> {
            ModularPanel panel = new ModularPanel(guiContext).relativeToScreen().pos(5,5).size(470,200);

            List<List<IWidget>> matrix = new ArrayList<>();
            int chunkSize = 10;
            //int[][] chunk = new int[chunkSize][chunkSize];
            Rectangle[][] block = new Rectangle[16*chunkSize][16*chunkSize];
            IWidget[][] blockW = new IWidget[16*chunkSize][16*chunkSize];
            Grid grid = new Grid().margin(0).minColWidth(0).minRowHeight(0);
            // Draw each Chunk
            for (int c = 0; c < chunkSize; c++) {
                int chunkOffset = c * 16;
                for (int j = 0; j < 16; j++) { // j -> columns
                    List<IWidget> row = new ArrayList<>();
                    for (int i = 0; i < 16; i++) { // i -> rows // we like to iterate rows first
                        int ic,jc; ic = i + chunkOffset; jc = j + chunkOffset;
                        // We Skip 16th block to draw Borders
                        block[ic][jc] = new Rectangle().setColor(UT.Code.getRGBaInt(MT.As.mRGBaSolid));
                        if(i==15||j==15) block[ic][jc] = new Rectangle().setColor(UT.Code.getRGBaInt(MT.Rubber.mRGBaSolid));
                        blockW[ic][jc] = block[ic][jc].asWidget().size(1,1);
                        row.add(blockW[ic][jc]);
                    }
                    matrix.add(row);
                }
            }
            grid.matrix(matrix);
//            for (int i = 0; i < 400; i++) {
//                int r = i / 20;
//                int c = i % 20;
//                List<IWidget> row;
//                if (matrix.size() <= r) {
//                    row = new ArrayList<>();
//                    matrix.add(row);
//                } else {
//                    row = matrix.get(r);
//                }
//                row.add(new Rectangle().setColor(UT.Code.getRGBaInt(MT.Cu.mRGBaSolid)).asWidget().size(1,1));
//            }
            panel.child(grid
                .pos(10, 10).right(10).bottom(10));
            return panel;
//
//        panel.child(SlotGroupWidget.playerInventory());
            //panel.child(new TextFieldWidget());
            //panel.child(ModularScreen.simple("screen",ModularPanel::new).getMainPanel().background(new ItemDrawable(OM.dust(MT.C))));
            //panel.background(new ItemDrawable(OM.dust(MT.C)));

            // a List of Strings (Ores)

            //panel.child()
//        ListWidget list = new ListWidget<>().widthRel(1).top(50).bottom(2)
//                .child(new Rectangle().setColor(0xFF606060).asWidget().top(1).left(32).size(1,40));
//        Column column = new Column().widthRel(1).top(25).bottom(10);
//        list.child(new TextWidget("Hello"));
//        column.child(new TextWidget("Hi").top(1));
//        panel.child(list);


        });
    }
}
