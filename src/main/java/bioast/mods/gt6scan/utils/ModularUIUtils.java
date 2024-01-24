package bioast.mods.gt6scan.utils;

import bioast.mods.gt6scan.item.ScanMode;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import gregapi.data.FL;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;

public class ModularUIUtils {
	public static IWidget wItem(OreDictMaterial mat, ScanMode mode) {
		OreDictPrefix prefix = OP.oreRaw;
		if (mode == ScanMode.SMALL) prefix = OP.crushed;
		if (mode.PREFIX == OP.bucket) prefix = OP.bucket;
		if (mode == ScanMode.ROCK) prefix = OP.rockGt;
		if (mat.mNameInternal.contains("Peat")) prefix = OP.ingot;
		if (mat.mNameInternal.contains("Clay")) prefix = OP.dust;
		if (mat.mNameInternal.contains("Methan"))
			return new ItemDrawable().setItem(FL.display(FL.Gas_Natural.fluid())).asWidget();
		if (mat.mNameInternal.contains("Water"))
			return new ItemDrawable().setItem(FL.display(FL.Water_Geothermal.fluid())).asWidget();
		if (mat.mNameInternal.contains("Lava"))
			return new ItemDrawable().setItem(FL.display(FL.Lava.fluid())).asWidget();

		return new ItemDrawable().setItem(prefix.dat(mat).getStack(1)).asWidget();
	}
}
