package bioast.mods.gt6mapper.item;

import bioast.mods.gt6mapper.MapperMod;
import bioast.mods.gt6mapper.world.ProspectMapData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.util.UT;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

// can have a meta data ranged from 0 to 4 corresponding to the size it will create
public class ItemEmptyProspectMap extends ItemMapBase {
	public static Pair<Short,ProspectMapData> existsMap(World par2World, EntityPlayer par3EntityPlayer, byte scale){
		Object l = ((Map)UT.Reflection.getFieldContent(par2World.mapStorage,"idCounts",true,true)).getOrDefault( "prospectmap",-1);
		int lastID;
		if(l instanceof Short) lastID = (short) l;
		if(l instanceof Integer) lastID = (int) l;
		else return null;
		if(lastID==-1) return null;
		for (int j = 0; j <= lastID; j++) {
			ProspectMapData data = (ProspectMapData) par2World.perWorldStorage.loadData(ProspectMapData.class, "prospectmap" + "_" + j);
			if(data==null) continue;
			int i = 128 * (1 << data.scale);
			int x=(int) (Math.round(par3EntityPlayer.posX / (double) i) * (long) i);
			int z=(int) (Math.round(par3EntityPlayer.posZ / (double) i) * (long) i);
			if(x==data.xCenter&&z==data.zCenter&&scale==data.scale)return Pair.of((short) j,data);
		}
		return null;
	}
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		byte scale = 0;
		int meta = 0;
		int i = 128 * (1 << scale);
		int x=(int) (Math.round(par3EntityPlayer.posX / (double) i) * (long) i);
		int z=(int) (Math.round(par3EntityPlayer.posZ / (double) i) * (long) i);
		if(!(par1ItemStack.getItemDamage()<1&&par1ItemStack.getItemDamage()>4))  scale = (byte) par1ItemStack.getItemDamage();
		Pair<Short,ProspectMapData> entry= existsMap(par2World,par3EntityPlayer,scale);
		ProspectMapData mapData = null;
		if(entry!=null) mapData=entry.getValue();
		ItemStack mapItem = null;
		if(mapData!=null){
			meta = entry.getKey();
			mapItem = new ItemStack(MapperMod.mapWritten, 1, meta);
		}
		else {
			meta = par2World.getUniqueDataId(ItemProspectMap.STR_ID);
			mapItem = new ItemStack(MapperMod.mapWritten, 1, meta);
			String var5 = "prospectmap_" + mapItem.getItemDamage();
			mapData = new ProspectMapData(var5);
			mapData.scale = scale;
			mapData.xCenter = x;
			mapData.zCenter = z;
			mapData.dimension = par2World.provider.dimensionId;
			mapData.markDirty();
			par2World.setItemData(var5, mapData);
		}
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
		this.itemIcon = par1IconRegister.registerIcon(MapperMod.MODID + ":" + "emptyProspectingMap");
	}
}
