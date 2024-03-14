package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.ScannerMod;
import gregapi.data.CS;
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
        for (int i = 2; i < 6; i++) {
            addItem(i,
                String.format("%s Scanner (%s)", CS.VOLTAGE_NAMES[i], CS.VN[i]),
                "Scan For Ores, Fluids and Rocks",
                new ScannerBehavior(),
                EnergyStat.makeTool(TD.Energy.EU, V[i] * 8000, V[i], 1, ST.make(this, 1, i)));
        }
    }
}

