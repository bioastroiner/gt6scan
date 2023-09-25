package bioast.mods.gt6scan;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.OP;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.OM;
import gregapi.util.ST;
import gregapi.util.UT;
import net.minecraft.block.material.MapColor;
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

public class ItemProspectMap extends ItemMap {
	public static final String STR_ID = "prospectmap";

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

	private static int searchForTopBlockInVerticalColumn(Chunk chunk, int x, int z, boolean inColor) {
		int y;
		int color = 0;
		boolean oreFound = false;
		for (int i = chunk.worldObj.getHeightValue(x, z); i >= 0; i--) {
			y = i;
			try {
				if (chunk.getTileEntityUnsafe(x & 15, y, z & 15) instanceof PrefixBlockTileEntity prefixBlockTileEntity) {
					PrefixBlock prefixBlock = (PrefixBlock) prefixBlockTileEntity.getBlock(x, y, z);
					OreDictPrefix prefix = prefixBlock.mPrefix;
					OreDictMaterial material = OreDictMaterial.MATERIAL_ARRAY[prefixBlockTileEntity.mMetaData];
					if (prefix.mFamiliarPrefixes.contains(OP.ore)) {
						oreFound = true;
						color = UT.Code.getRGBInt(material.mRGBaSolid);
						break;
					}
				}
				OreDictItemData data = OM.anyassociation(ST.make(chunk.getBlock(x & 15, y, z & 15), 1, chunk.getBlockMetadata(x & 15, y, z & 15)));
				if (data != null && (data.mPrefix.mFamiliarPrefixes.contains(OP.oreDense) || data.mPrefix.mFamiliarPrefixes.contains(OP.ore) || data.mPrefix.mFamiliarPrefixes.contains(OP.glowstone))) {
					oreFound = true;
					color = UT.Code.getRGBaInt(data.mMaterial.mMaterial.mRGBaSolid);
					break;
				}
			} catch (Exception ignored) {
			}
		}
		if (!inColor && oreFound) return MapColor.blackColor.colorIndex;
		// color was found now we try to find the most similar map color to it and return its index
		return ColorMap.asMinecraftMapColor(color).colorIndex;
	}

	@Override
	public ProspectMapData getMapData(ItemStack par1ItemStack, World par2World) {
		String mapName = STR_ID + "_" + par1ItemStack.getItemDamage();
		ProspectMapData mapData = (ProspectMapData) par2World.loadItemData(ProspectMapData.class, mapName);

		if (mapData == null && !par2World.isRemote) {
			par1ItemStack.setItemDamage(par2World.getUniqueDataId(STR_ID));
			mapName = STR_ID + "_" + par1ItemStack.getItemDamage();
			mapData = new ProspectMapData(mapName);
			mapData.scale = 0;
			int i = 128 * (1 << mapData.scale);
			mapData.xCenter = Math.round((float) par2World.getWorldInfo().getSpawnX() / (float) i) * i;
			mapData.zCenter = Math.round((float) (par2World.getWorldInfo().getSpawnZ() / i)) * i;
			mapData.dimension = par2World.provider.dimensionId;
			mapData.markDirty();
			par2World.setItemData(mapName, mapData);
		}

		return mapData;
	}

	public void updateMapData(World worldIn, Entity entityIn, ProspectMapData mapDataIn) {
		if (worldIn.provider.dimensionId == mapDataIn.dimension && entityIn instanceof EntityPlayer) {
			int scaleFactor = (1 << mapDataIn.scale);
			int xCenter = mapDataIn.xCenter;
			int zCenter = mapDataIn.zCenter;
			int xDraw = MathHelper.floor_double(entityIn.posX - (double) xCenter) / scaleFactor + 64;
			int zDraw = MathHelper.floor_double(entityIn.posZ - (double) zCenter) / scaleFactor + 64;
			int drawSize = 128 / scaleFactor;
			MapData.MapInfo mapInfo = mapDataIn.func_82568_a((EntityPlayer) entityIn);
			++mapInfo.field_82569_d;
			for (int xStep = xDraw - drawSize + 1; xStep < xDraw + drawSize; ++xStep)
				if ((xStep & 15) == (mapInfo.field_82569_d & 15)) {
					int topZ = 255;
					int botZ = 0;
					for (int zStep = zDraw - drawSize - 1; zStep < zDraw + drawSize; ++zStep)
						if (xStep >= 0 && zStep >= -1 && xStep < 128 && zStep < 128) {
							int xOffset = xStep - xDraw;
							int zOffset = zStep - zDraw;
							boolean var20 = xOffset * xOffset + zOffset * zOffset > (drawSize - 2) * (drawSize - 2);
							int xDraw2 = (xCenter / scaleFactor + xStep - 64) * scaleFactor;
							int zDraw2 = (zCenter / scaleFactor + zStep - 64) * scaleFactor;
							int x = xDraw2;
							int z = zDraw2;
							Chunk chunk = worldIn.getChunkFromBlockCoords(xDraw2, zDraw2);
							if (!chunk.isEmpty()) {
								int colorIndex = searchForTopBlockInVerticalColumn(chunk, x, z, mapDataIn.coloredMode);
								// set background so its not transparent
								// in case a mat has background as color change it
								if (colorIndex == 2) colorIndex = (byte) 30;
								if (colorIndex == 0) colorIndex = (byte) 2;
								if (zStep >= 0 && xOffset * xOffset + zOffset * zOffset < drawSize * drawSize && (!var20 || (xStep + zStep & 1) != 0)) {
									mapDataIn.colors[xStep + zStep * 128] = (byte) (colorIndex * 4);
									if (topZ > zStep) topZ = zStep;
									if (botZ < zStep) botZ = zStep;
								}
							}
						}
					if (topZ <= botZ) mapDataIn.setColumnDirty(xStep, topZ, botZ);
				}
		}
	}

	@Override
	public void onUpdate(ItemStack aStack, World worldIn, Entity entityIn, int partialTicks, boolean isActiveItem) {
		if (worldIn.isRemote) return;
		ProspectMapData mapData = this.getMapData(aStack, worldIn);
		if (entityIn instanceof EntityPlayer player) {
			mapData.updateVisiblePlayers(player, aStack);
		}
		if (isActiveItem) this.updateMapData(worldIn, entityIn, mapData);
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
	public Packet func_150911_c(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		//System.out.println("yCenter = " + this.getMapData(par1ItemStack, par2World).yCenter);
		byte[] mapBytes = this.getMapData(par1ItemStack, par2World).getUpdatePacketData(par1ItemStack, par2World, par3EntityPlayer);
		if (mapBytes == null) return null;
		else {
			short uniqueID = (short) par1ItemStack.getItemDamage();
			return MapPacketHandler.makeProspectingMapHandler(ItemProspectMap.STR_ID, uniqueID, mapBytes);
		}
	}

	public String getItemStackDisplayName(ItemStack par1ItemStack) {
		return (StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(par1ItemStack) + ".name") + " #" + par1ItemStack.getItemDamage()).trim();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon(ScannerMod.MODID + ":" + this.getUnlocalizedName().substring(5));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
		if (!player.isSneaking()) toggleSize(itemStackIn, worldIn);
		if (player.isSneaking()) toggleColor(itemStackIn, worldIn);
		return super.onItemRightClick(itemStackIn, worldIn, player);
	}

	public void toggleSize(ItemStack itemStackIn, World worldIn) {
		getMapData(itemStackIn, worldIn).scale += 1;
		if (getMapData(itemStackIn, worldIn).scale > 4) getMapData(itemStackIn, worldIn).scale = 0;
		for (int i = 0; i < getMapData(itemStackIn, worldIn).colors.length; i++) {
			getMapData(itemStackIn, worldIn).colors[i] = (byte) 0;
		}
	}

	public void toggleColor(ItemStack itemStackIn, World worldIn) {
		getMapData(itemStackIn, worldIn).coloredMode = !getMapData(itemStackIn, worldIn).coloredMode;
//		for (int i = 0; i < getMapData(itemStackIn, worldIn).colors.length; i++) {
//			if (getMapData(itemStackIn, worldIn).colors[i] != 0) {
//				getMapData(itemStackIn, worldIn).colors[i] = (byte) (MapColor.obsidianColor.colorIndex * 4);
//			}
//		}
	}
}
