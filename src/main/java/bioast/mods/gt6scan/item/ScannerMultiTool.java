package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.ScannerMod;
import gregapi.code.TagData;
import gregapi.data.CS;
import gregapi.data.TD;
import gregapi.item.multiitem.MultiItemRandom;
import gregapi.item.multiitem.energy.EnergyStat;
import gregapi.util.ST;
import gregapi.util.UT;

import static gregapi.data.CS.V;

public class ScannerMultiTool extends MultiItemRandom {
    public static int consumptionRate;
    public String mainConsumptionEnergyType = "EU";
    public int storage_multiplier = 8000;

    public ScannerMultiTool() {
        super(ScannerMod.MODID, "scannertool");
        consumptionRate = 50;
    }

    @Override
    public void addItems() {
        for (int i = 2; i < 6; i++) {
            TagData energy = (TagData) UT.Reflection.getFieldContent(TD.Energy.class,
                mainConsumptionEnergyType,
                false,
                false);
            ScannerMod.debug.warn(
                "mainConsumptionEnergyType was not set right, defaulting to NULL (not energy consumption)");
            addItem(i,
                String.format("%s Scanner (%s)", CS.VOLTAGE_NAMES[i], CS.VN[i]),
                "Scan For Ores, Fluids and Rocks (when in Gui you can click on any tile and bookmark it)",
                new ScannerBehavior(energy)
            );
            if (energy != null) {
                setElectricStats(i,
                    EnergyStat.makeTool(energy,
                        V[i] * storage_multiplier,
                        V[i],
                        1,
                        ST.make(this, 1, i)));
            }
        }
    }
}

