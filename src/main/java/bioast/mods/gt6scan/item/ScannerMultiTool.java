package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.ScannerMod;
import gregapi.data.TD;
import gregapi.item.multiitem.MultiItemRandom;
import gregapi.item.multiitem.energy.EnergyStat;
import gregapi.util.ST;

import static gregapi.data.CS.V;

public class ScannerMultiTool extends MultiItemRandom {
    public ScannerMultiTool() {
        super(ScannerMod.MODID, "scannertool");
    }

    @Override
    public void addItems() {
        addItem(1,"Advanced Scanner (LuV)","Scan For Ores, Fluids and Rocks",new ScannerBehavior(9),
            EnergyStat.makeTool(TD.Energy.LU,V[6]*8000,V[6],64, ST.make(this,1,1)));
    }
}

