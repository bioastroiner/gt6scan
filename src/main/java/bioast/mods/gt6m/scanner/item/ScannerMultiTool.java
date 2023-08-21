package bioast.mods.gt6m.scanner.item;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.scanner.utils.VALs;
import gregapi.item.multiitem.MultiItemTool;

public class ScannerMultiTool extends MultiItemTool {
    public static ScannerMultiTool INSTANCE;

    public ScannerMultiTool() {
        super(GT6M_Mod.MODID, VALs.SCANNER_MULTI_NAME);
        addTool(0, "Scanner", "Open it",new ScannerToolStats(6), "toolScanner");
    }
}
