package bioast.mods.gt6scan;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.UT;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import twilightforest.TFMazeMapData;

public class ItemProspectMap extends ItemMap {
	public static final String STR_ID = "prospectmap";
	private static final int YSEARCH = 50;
	protected boolean mapOres = true;

	@SideOnly(Side.CLIENT)
	public static ProspectMapData getMPMapData(int par0, World par1World) {
		String mapName = STR_ID + "_" + par0;

		ProspectMapData mapData = (ProspectMapData) par1World.loadItemData(ProspectMapData.class, mapName);

		if (mapData == null) {
			mapData = new ProspectMapData(mapName);
			par1World.setItemData(mapName, mapData);
		}

		return mapData;
	}

	@Override
	public ProspectMapData getMapData(ItemStack par1ItemStack, World par2World) {
		String mapName = STR_ID + "_" + par1ItemStack.getItemDamage();
		ProspectMapData mapData = (ProspectMapData) par2World.loadItemData(ProspectMapData.class, mapName);

		if (mapData == null && !par2World.isRemote) {
			par1ItemStack.setItemDamage(par2World.getUniqueDataId(STR_ID));
			mapName = STR_ID + "_" + par1ItemStack.getItemDamage();
			mapData = new ProspectMapData(mapName);
			mapData.xCenter = par2World.getWorldInfo().getSpawnX();
			mapData.zCenter = par2World.getWorldInfo().getSpawnZ();
			mapData.scale = 0;
			mapData.dimension = par2World.provider.dimensionId;
			mapData.markDirty();
			par2World.setItemData(mapName, mapData);
		}

		return mapData;
	}

	public void updateMapData(World par1World, Entity par2Entity, ProspectMapData par3MapData) {
//		short xSize = 128;
//		short zSize = 128;
//		int xWorld = par3MapData.xCenter;
//		int zWorld = par3MapData.zCenter;
//
//		MapData.MapInfo mapInfo = par3MapData.func_82568_a((EntityPlayer) par2Entity);
//		++mapInfo.field_82569_d;

		//int yDraw = MathHelper.floor_double(par2Entity.posY - (double) par3MapData.yCenter);

		if (par1World.provider.dimensionId == par3MapData.dimension) {
			short xSize = 128;
			short zSize = 128;
			int xCenter = par3MapData.xCenter;
			int zCenter = par3MapData.zCenter;
			int xDraw = MathHelper.floor_double(par2Entity.posX - (double) xCenter) + xSize / 2;
			int zDraw = MathHelper.floor_double(par2Entity.posZ - (double) zCenter) + zSize / 2;
			int drawSize = 16;

			MapData.MapInfo mapInfo = par3MapData.func_82568_a((EntityPlayer) par2Entity);
			++mapInfo.field_82569_d;

			// Draw a circle of radius drawSize arpund player's position
			for (int xStep = xDraw - drawSize + 1; xStep < xDraw + drawSize; ++xStep)
				if ((xStep & 15) == (mapInfo.field_82569_d & 15)) {
					int highNumber = 255;
					int lowNumber = 0;
					for (int zStep = zDraw - drawSize - 1; zStep < zDraw + drawSize; ++zStep)
						if (xStep >= 0 && zStep >= -1 && xStep < xSize && zStep < zSize) {
							int xOffset = xStep - xDraw;
							int zOffset = zStep - zDraw;
							boolean var20 = xOffset * xOffset + zOffset * zOffset > (drawSize - 2) * (drawSize - 2);
							int xDraw2 = (xCenter + xStep - xSize / 2);
							int zDraw2 = (zCenter + zStep - zSize / 2);
							int x = xDraw2;
							int z = zDraw2;
							Chunk chunk = par1World.getChunkFromBlockCoords(xDraw2, zDraw2);
							int y = 0;
							int colorindex = 0;
							for (int i = 200; i >= 0; i--) {
								y = i;
								try {
									if (chunk.getTileEntityUnsafe(x&15, y, z&15) instanceof PrefixBlockTileEntity prefixBlockTileEntity) {
										PrefixBlock prefixBlock = (PrefixBlock) prefixBlockTileEntity.getBlock(x, y, z);
										OreDictPrefix prefix = prefixBlock.mPrefix;
										OreDictMaterial material = OreDictMaterial.MATERIAL_ARRAY[prefixBlockTileEntity.mMetaData];
										if (prefix.mFamiliarPrefixes.contains(OP.ore)) {
											colorindex = UT.Code.getRGBaInt(material.mRGBaSolid);
											break;
										}
									}
								} catch (Exception e) {
									continue;
								}
							}
							if (zStep >= 0 && xOffset * xOffset + zOffset * zOffset < drawSize * drawSize && (!var20 || (xStep + zStep & 1) != 0)) {
								par3MapData.colors[xStep + zStep * xSize] = (byte) colorindex;
								if (highNumber > zStep) {
									highNumber = zStep;
								}
								if (lowNumber < zStep) {
									lowNumber = zStep;
								}
							}
						}
					if (highNumber <= lowNumber) {
						par3MapData.setColumnDirty(xStep, highNumber, lowNumber);
					}
				}
		}
	}

	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean isActiveItem) {
		if (!par2World.isRemote) {
			ProspectMapData mapData = this.getMapData(par1ItemStack, par2World);
			if (par3Entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) par3Entity;
				mapData.updateVisiblePlayers(player, par1ItemStack);
				// fix player icon so that it's a dot
				MapData.MapCoord mapCoord = (MapData.MapCoord) mapData.playersVisibleOnMap.get(player.getCommandSenderName());
				if (mapCoord != null) {
					mapCoord.iconSize = 6;
				}
			}
			if (isActiveItem) {
				this.updateMapData(par2World, par3Entity, mapData);
			}
		}
	}

	@Override
	public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		par1ItemStack.setItemDamage(par2World.getUniqueDataId(STR_ID));
		String mapName = STR_ID + "_" + par1ItemStack.getItemDamage();
		ProspectMapData mapData = new ProspectMapData(mapName);
		par2World.setItemData(mapName, mapData);
		mapData.xCenter = MathHelper.floor_double(par3EntityPlayer.posX);
		mapData.yCenter = MathHelper.floor_double(par3EntityPlayer.posY);
		mapData.zCenter = MathHelper.floor_double(par3EntityPlayer.posZ);
		mapData.scale = 0;
		mapData.dimension = par2World.provider.dimensionId;
		mapData.markDirty();
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
		itemStackIn.setItemDamage(worldIn.getUniqueDataId(STR_ID));
		String mapName = STR_ID + "_" + itemStackIn.getItemDamage();
		ProspectMapData mapData = new ProspectMapData(mapName);
		worldIn.setItemData(mapName, mapData);
		mapData.xCenter = MathHelper.floor_double(player.posX);
		mapData.yCenter = MathHelper.floor_double(player.posY);
		mapData.zCenter = MathHelper.floor_double(player.posZ);
		mapData.scale = 0;
		mapData.dimension = worldIn.provider.dimensionId;
		mapData.markDirty();
		return itemStackIn;
	}

	/**
	 * returns null if no update is to be sent
	 * <p>
	 * We have re-written this to provide a Packet250CustomPayload to be sent, since the map data packet is only for the actual map map.
	 */
	@Override
	public Packet func_150911_c(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		//System.out.println("yCenter = " + this.getMapData(par1ItemStack, par2World).yCenter);
		byte[] mapBytes = this.getMapData(par1ItemStack, par2World).getUpdatePacketData(par1ItemStack, par2World, par3EntityPlayer);

		if (mapBytes == null) {
			return null;
		} else {
			short uniqueID = (short) par1ItemStack.getItemDamage();
			return MapPacketHandler.makeProspectingMapHandler(ItemProspectMap.STR_ID, uniqueID, mapBytes);
		}
	}

	/**
	 * Add the map number to the tooltip
	 */
	public String getItemStackDisplayName(ItemStack par1ItemStack) {
		return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(par1ItemStack) + ".name") + " #" + par1ItemStack.getItemDamage()).trim();
	}

	/**
	 * Properly register icon source
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon(ScannerMod.MODID + ":" + this.getUnlocalizedName().substring(5));
	}
}
