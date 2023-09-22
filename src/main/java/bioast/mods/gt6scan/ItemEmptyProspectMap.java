package bioast.mods.gt6scan;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemEmptyProspectMap extends ItemMapBase {
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		ItemStack mapItem = new ItemStack(ScannerMod.mapWritten, 1, par2World.getUniqueDataId(ItemProspectMap.STR_ID));
		String var5 = "prospectmap_" + mapItem.getItemDamage();
		ProspectMapData mapData = new ProspectMapData(var5);
		par2World.setItemData(var5, mapData);
		mapData.scale = 0;
		int step = 128 * (1 << mapData.scale);
		// need to fix center for feature offset
//		mapData.xCenter = (int) (Math.round(par3EntityPlayer.posX / step) * step);
//		mapData.zCenter = (int) (Math.round(par3EntityPlayer.posZ / step) * step);
//		mapData.yCenter = MathHelper.floor_double(par3EntityPlayer.posY);
		int i = 128 * (1 << mapData.scale);
		mapData.xCenter = (int)(Math.round(par3EntityPlayer.posX / (double)i) * (long)i);
		mapData.zCenter = (int)(Math.round(par3EntityPlayer.posZ / (double)i) * (long)i);
//		mapData.xCenter = MathHelper.floor_double(par3EntityPlayer.posX);
//		mapData.zCenter = MathHelper.floor_double(par3EntityPlayer.posZ);
		mapData.yCenter = MathHelper.floor_double(par3EntityPlayer.posY);
		mapData.dimension = par2World.provider.dimensionId;
		mapData.markDirty();
		--par1ItemStack.stackSize;
		if (par1ItemStack.stackSize <= 0) {
			return mapItem;
		} else {
			if (!par3EntityPlayer.inventory.addItemStackToInventory(mapItem.copy())) {
				par3EntityPlayer.dropPlayerItemWithRandomChoice(mapItem, false);
			}
			return par1ItemStack;
		}
	}

	/**
	 * Properly register icon source
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon(ScannerMod.MODID + ":" + "emptyProspectingMap");
	}
}
