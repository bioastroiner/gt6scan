package bioast.mods.gt6scan;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapData;

public class ProspectMapData extends MapData {
	public int yCenter;
	public boolean coloredMode;

	public ProspectMapData(String p_i2140_1_) {
		super(p_i2140_1_);
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);

		this.yCenter = par1NBTTagCompound.getInteger("yCenter");
		this.coloredMode = par1NBTTagCompound.getBoolean("colorMode");
	}

	/**
	 * write data to NBTTagCompound from this MapDataBase, similar to Entities and TileEntities
	 */
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setInteger("yCenter", this.yCenter);
		par1NBTTagCompound.setBoolean("colorMode", this.coloredMode);
	}
}
