package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.network.OreData;
import bioast.mods.gt6scan.network.OreDataSyncHandler;
import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.manager.GuiInfo;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.data.*;
import gregapi.item.multiitem.MultiItem;
import gregapi.item.multiitem.behaviors.IBehavior;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.OM;
import gregapi.util.UT;
import gregtech.blocks.BlockDiggable;
import gregtech.blocks.stone.BlockCrystalOres;
import gregtech.blocks.stone.BlockRockOres;
import gregtech.blocks.stone.BlockVanillaOresA;
import gregtech.tileentity.misc.MultiTileEntityFluidSpring;
import gregtech.tileentity.placeables.MultiTileEntityRock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static bioast.mods.gt6scan.ScannerMod.config;
import static bioast.mods.gt6scan.ScannerMod.debug;
import static bioast.mods.gt6scan.utils.HLPs.col;
import static bioast.mods.gt6scan.utils.HLPs.prefixBlock;
import static com.cleanroommc.modularui.drawable.BufferBuilder.bufferbuilder;

public class ScannerBehavior extends IBehavior.AbstractBehaviorDefault implements IItemGuiHolder {
	//Mode mode = Mode.LARGE;
	List<OreData> scannedOres = new ArrayList<>();
	Map<Short, Integer> sortedOres = new HashMap<>();
	int chunkSize = 9;
	int oresFound = 0;
	int x_origin;
	int z_origin;
	boolean usePower = false;

	/* CLIENT ONLY*/ int[][] blockColorStorage = new int[16 * chunkSize][16 * chunkSize]; // store color
	/* CLIENT ONLY*/ short[][] blockMatStorage = new short[16 * chunkSize][16 * chunkSize]; // like blocks but stores materialID

	public ScannerBehavior(int sizeIn) {
		chunkSize = sizeIn;
	}

	public ScannerBehavior() {
		int def_size = config.get("core", "sizeInChunksOddNumber", 9);
		if (def_size % 2 == 0) def_size++;
		new ScannerBehavior(def_size);
	}

	private static Chunk[][] getChunksAroundPlayer(World aWorld, EntityPlayer aPlayer, int chunkSize) {
		Chunk[][] chunks = new Chunk[chunkSize][chunkSize];
		final int PLAYER_CHUNK_INDEX = (chunkSize - 1) / 2;
		chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX] = aWorld.getChunkFromBlockCoords((int) aPlayer.posX, (int) aPlayer.posZ);
		final Chunk PLAYER_CHUNK = chunks[PLAYER_CHUNK_INDEX][PLAYER_CHUNK_INDEX];
		chunks[0][0] = aWorld.getChunkFromChunkCoords(PLAYER_CHUNK.xPosition - ((chunkSize - 1) / 2), PLAYER_CHUNK.zPosition - ((chunkSize - 1) / 2));
		for (int i = 0; i < chunkSize; i++) {
			for (int j = 0; j < chunkSize; j++) {
				if (i == 0 && j == 0) continue;
				if (i == PLAYER_CHUNK_INDEX && j == PLAYER_CHUNK_INDEX) continue;
				chunks[i][j] = aWorld.getChunkFromChunkCoords(chunks[0][0].xPosition + i, chunks[0][0].zPosition + j);
			}
		}
		return chunks;
	}

	private IWidget wItem(OreDictMaterial mat, ScanMode mode) {
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

	public ItemStack changeMode(EntityPlayer aPlayer, ItemStack aStack, ScanMode currentMode) {
		//Switch mode
		int nextMode = currentMode.ordinal() + 1;
		if (nextMode >= ScanMode.values().length) nextMode = 0;
		try {
			currentMode = ScanMode.values()[nextMode];
		} catch (ArrayIndexOutOfBoundsException e) {
			currentMode = ScanMode.NONE;
		}
		UT.NBT.makeInt(UT.NBT.getNBT(aStack),"mode", currentMode.ordinal());
		UT.Entities.sendchat(aPlayer, "Mode: " + currentMode.name());
		return aStack;
	}

	public ScanMode getMode(ItemStack aStack) {
		NBTTagCompound tag = UT.NBT.getOrCreate(aStack);
		if (!tag.hasKey("mode")) {
			UT.NBT.makeInt(tag, 0);
			UT.NBT.set(aStack, tag);
			return ScanMode.NONE;
		} else {
			return ScanMode.values()[tag.getInteger("mode")];
		}
	}

	@Override
	public ItemStack onItemRightClick(MultiItem aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
		UT.Sounds.send(CS.SFX.IC_SCANNER, 1, 5.0F, aPlayer);
		if (aStack != null && (aPlayer == null || aPlayer.isSneaking()) && !aWorld.isRemote) {
			changeMode(aPlayer, aStack, getMode(aStack));
			return aStack;
		}
        if(!aWorld.isRemote && getMode(aStack) == ScanMode.NONE){
            return aStack;
        }
		List<String> chat_debug = new ArrayList<>();
		if (!aWorld.isRemote && getMode(aStack) != ScanMode.NONE){
            if (doTroll(aStack, aWorld, aPlayer)) return aStack;
			if (!UT.Entities.isCreative(aPlayer) && usePower) {
				if (aItem.getEnergyStored(TD.Energy.LU, aStack) < CS.V[6]) return aStack;
			}
			if (!UT.Entities.isCreative(aPlayer) && usePower)
				aItem.useEnergy(TD.Energy.LU, aStack, sortedOres.keySet().size() * CS.V[6] * Math.min(oresFound / 100, 30), aPlayer, aPlayer.inventory, aWorld, (int) aPlayer.posX, (int) aPlayer.posY, (int) aPlayer.posZ, !UT.Entities.isCreative(aPlayer));
			serverLogic(aStack, aWorld, aPlayer, chat_debug);
			debug.debug(oresFound + " ores found.");
		}
		return aStack;
	}

	private static boolean doTroll(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
		if(!aWorld.isRemote && "Bear989Sr".equalsIgnoreCase(aPlayer.getCommandSenderName()) && new Random().nextInt(10) < 2){
			// let's troll bear
			UT.Entities.sendchat(aPlayer,"BEAR DETECTED, Realising the Specman...");
			UT.Sounds.send(CS.SFX.MC_TNT_IGNITE,1,5f, aPlayer);
			if(new Random().nextInt(100)<2){
				// or troll for real
				EntityCreeper creeper = new EntityCreeper(aWorld);
				creeper.setCreeperState(1);
				creeper.setPosition(aPlayer.posX, aPlayer.posY+30, aPlayer.posZ);
				creeper.setCustomNameTag("Bear989Jr");
				creeper.setAbsorptionAmount(20);
				UT.Entities.applyPotion(creeper, Potion.moveSlowdown,5000,2,true);
				UT.Entities.applyPotion(creeper, Potion.regeneration,5000,5,true);
				aWorld.spawnEntityInWorld(creeper);
			}
			return true;
		}
		return false;
	}

	@Override
	public List<String> getAdditionalToolTips(MultiItem aItem, List<String> aList, ItemStack aStack) {
		aList.add("Shift Right Click to Change Mode.");
		aList.add("Right Click to Open GUI.");
		aList.add(getMode(aStack) + " Mode.");
		return aList;
	}

	@Override
	public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player, ItemStack itemStack) {
		guiSyncHandler.syncValue(0, new OreDataSyncHandler(() -> this.scannedOres, value -> this.scannedOres = value));
		guiSyncHandler.syncValue(1, SyncHandlers.intNumber(() -> this.oresFound, val -> this.oresFound = val));
		guiSyncHandler.syncValue(2, SyncHandlers.intNumber(() -> this.chunkSize, val -> this.chunkSize = val));
		guiSyncHandler.syncValue(3, SyncHandlers.intNumber(() -> this.x_origin, val -> this.x_origin = val));
		guiSyncHandler.syncValue(4, SyncHandlers.intNumber(() -> this.z_origin, val -> this.z_origin = val));
		//guiSyncHandler.syncValue(5, SyncHandlers.enumValue(Mode.class, () -> this.mode, val -> this.mode = val));
	}


	private void serverLogic(ItemStack aStack, World aWorld, EntityPlayer aPlayer, List<String> chat) {
		//TODO server logic optimazation
		ScanMode mode = getMode(aStack);
		this.scannedOres.clear();
		this.oresFound = 0;
		Chunk[][] chunks = getChunksAroundPlayer(aWorld, aPlayer, chunkSize);
		x_origin = (chunks[0][0].xPosition << 4);
		z_origin = (chunks[0][0].zPosition << 4);
		for (int i = 0; i < chunkSize; i++) {
			for (int j = 0; j < chunkSize; j++) {
				if (mode.isTE()) findTileEntityBlocks(chunks[i][j], mode);
				else {
					for (int k = 0; k < 16; k++) {
						for (int l = 0; l < 16; l++) {
							//TODO check packet size
							int highest_y = chunks[i][j].getHeightValue(k, l);
							for (int m = highest_y; m >= 0; m--) {
								Block block1 = chunks[i][j].getBlock(k, m, l);
								short matID;
								int x = chunks[i][j].xPosition * 16 + k;
								int z = chunks[i][j].zPosition * 16 + l;
								int y = m;
								if (mode == ScanMode.DENSE_AND_NORMAL) {
									if (block1 instanceof BlockRockOres) {
										matID = BlockRockOres.ORE_MATERIALS[chunks[i][j].getBlockMetadata(k, m, l)].mID;
										oresFound++;
										scannedOres.add(new OreData(x, y, z, matID));
										//TODO
										// this block can easially cause server to crash because of 2097050 bytes Packet Limit
										// so we skip the lower y levels for dense ores
										break;
									} else if (block1 instanceof BlockCrystalOres) {
										matID = BlockCrystalOres.ORE_MATERIALS[chunks[i][j].getBlockMetadata(k, m, l)].mID;
										oresFound++;
										scannedOres.add(new OreData(x, y, z, matID));
										//break;
									} else if (block1 instanceof BlockVanillaOresA) {
										matID = BlockVanillaOresA.ORE_MATERIALS[chunks[i][j].getBlockMetadata(k, m, l)].mID;
										oresFound++;
										scannedOres.add(new OreData(x, y, z, matID));
										//break;
									} else if (block1 instanceof BlockDiggable) {
										int meta = aWorld.getBlockMetadata(x, y, z);
										matID = 0;
										switch (meta) {
											case 0:
												break;
											case 1:
												matID = MT.ClayBrown.mID;
												break;
											case 2:
												matID = MT.Peat.mID;
												break;
											case 3:
												matID = MT.ClayRed.mID;
												break;
											case 4:
												matID = MT.Bentonite.mID;
												break;
											case 5:
												matID = MT.Palygorskite.mID;
												break;
											case 6:
												matID = MT.Kaolinite.mID;
												break;
										}
										if (matID != 0) {
											scannedOres.add(new OreData(x, y, z, matID));
											oresFound++;
										}
									}
								} else if (mode == ScanMode.FLUID) {
									//TODO HOLY SHIT FLUIDS ARE A MESS!!!
									if (block1 == Blocks.lava) {
										matID = MT.Lava.mID;
										if (aWorld.provider.isHellWorld) {
											scannedOres.add(new OreData(x, y, z, matID));
											oresFound++;
											break;
										}
										scannedOres.add(new OreData(x, y, z, matID));
										oresFound++;
									}
									if (block1 instanceof IFluidBlock || block1 == Blocks.lava || block1 == Blocks.water) {
										if (!(block1 instanceof IFluidBlock)) break;
										String fluidName = ((IFluidBlock) block1).getFluid().getName();
										matID = MT.Air.mID;
										if (fluidName.contains("natural")) matID = MT.MethaneIce.mID;
										if (fluidName.contains("water")) matID = MT.Water.mID;
										if (fluidName.contains("oil")) matID = MT.Oil.mID;
										if (fluidName.contains("honey")) matID = MT.Honey.mID;
										if (fluidName.contains("sulfuric")) matID = MT.H2SO4.mID;
										if (fluidName.contains("acid")) matID = MT.H2SO4.mID;
										if (fluidName.contains("poison")) matID = MT.DirtyWater.mID;
										if (fluidName.contains("infused")) matID = MT.InfusedWater.mID;
										if (fluidName.contains("mana")) matID = MT.Magic.mID;
										if (block1 == CS.BlocksGT.WaterGeothermal) matID = MT.DistWater.mID;
										if (block1 == CS.BlocksGT.Swamp) matID = MT.DirtyWater.mID;
										if (block1 == CS.BlocksGT.River) matID = MT.FreshWater.mID;
										if (block1 == CS.BlocksGT.Ocean) {
											/*skip ocean*/
											matID = MT.SeaWater.mID;
											scannedOres.add(new OreData(x, y, z, matID));
											oresFound++;
											break;
										}
										if (matID != MT.Air.mID) {
											scannedOres.add(new OreData(x, y, z, matID));
											oresFound++;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		/*Handles Server to Client Communication*/
		final GuiInfo gui = GuiInfo.builder().clientGui(context -> {
			return createGuiScreen(context.getPlayer(), aStack);
		}).serverGui((context, guiSyncHandler) -> {
			this.buildSyncHandler(guiSyncHandler, context.getPlayer(), aStack);
		}).build();
		gui.open(aPlayer);
	}

	private void findTileEntityBlocks(Chunk chunk, ScanMode mode) {
		var tMap = chunk.chunkTileEntityMap;
		Map<ChunkPosition, TileEntity> tTileMap;
		if (tMap != null) {
			try {
				tTileMap = (Map<ChunkPosition, TileEntity>) (tMap);
				tTileMap.forEach((chunkPos, tile) -> {
					if (tile instanceof PrefixBlockTileEntity pTile && (mode == ScanMode.LARGE || mode == ScanMode.SMALL || mode == ScanMode.BEDROCK)) {
						PrefixBlock pBlock = prefixBlock(pTile);
						boolean isBedrock = false;
						if (mode == ScanMode.BEDROCK) {
							isBedrock = pBlock.mNameInternal.contains("bedrock");
							//ScannerMod.debug.info(pBlock.mNameInternal);
						}
						if (isBedrock || pBlock.mPrefix.mFamiliarPrefixes.contains(mode.PREFIX)) {
							short matID = pTile.mMetaData;
							int x = pTile.getX();
							int y = pTile.getY();
							int z = pTile.getZ();
							this.scannedOres.add(new OreData(x, y, z, matID));
							this.oresFound++;
						}
					}
					if (mode == ScanMode.FLUID_BEDROCK && tile instanceof MultiTileEntityFluidSpring) {
						FluidStack fluidStack = ((MultiTileEntityFluidSpring) tile).mFluid;
						String name = fluidStack.getFluid().getName();
						short matID = 0;
						if (name.contains("oil")) matID = MT.Oil.mID;
						if (name.contains("water")) matID = MT.Water.mID;
						if (name.contains("lava")) matID = MT.Lava.mID;
						if (name.contains("natural")) matID = MT.CH4.mID;
						int x = ((MultiTileEntityFluidSpring) tile).getX();
						int y = ((MultiTileEntityFluidSpring) tile).getY();
						int z = ((MultiTileEntityFluidSpring) tile).getZ();
						if (matID != 0) {
							this.scannedOres.add(new OreData(x, y, z, matID));
							this.oresFound++;
						}
					}
					if (tile instanceof MultiTileEntityRock && mode == ScanMode.ROCK) {
						short matID = OM.anydata_(((MultiTileEntityRock) tile).mRock).mMaterial.mMaterial.mID;
						int x = ((MultiTileEntityRock) tile).getX();
						int y = ((MultiTileEntityRock) tile).getY();
						int z = ((MultiTileEntityRock) tile).getZ();
						this.scannedOres.add(new OreData(x, y, z, matID));
						this.oresFound++;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void clientLogic() {
		//ScannerMod.debug.info(x_origin);
		//ScannerMod.debug.info(z_origin);

		/* CLIENT CODE */
		blockMatStorage = new short[chunkSize * 16][chunkSize * 16];
		int borderColor = col(MT.Gray);
		int backgroundColor = col(MT.White);
		int oreColor = 0;
		sortedOres.clear();
		for (int chunkGridX = 0; chunkGridX < chunkSize; chunkGridX++) {
			for (int chunkGridZ = 0; chunkGridZ < chunkSize; chunkGridZ++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						int blockGridX, blockGridZ;
						boolean isOre = false;
						blockGridX = x + chunkGridX * 16;
						blockGridZ = z + chunkGridZ * 16;
						int blockWorldX = x_origin + blockGridX;
						int blockWorldZ = z_origin + blockGridZ;
						int lastY = 0;
						try {
							if (scannedOres != null && !scannedOres.isEmpty()) for (OreData data : scannedOres) {
								if (data.x == blockWorldX && data.z == blockWorldZ) {
									if (lastY <= data.y) {
										oreColor = col(OreDictMaterial.MATERIAL_ARRAY[data.matID]);
										lastY = data.y;
									}
									if (sortedOres.containsKey(data.matID)) {
										int lastCount = sortedOres.get(data.matID);
										sortedOres.put(data.matID, lastCount + 1);
									} else sortedOres.put(data.matID, 1);
									blockMatStorage[blockGridX][blockGridZ] = data.matID;
									isOre = true;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						blockColorStorage[blockGridX][blockGridZ] = backgroundColor;
						if (isOre) {
							blockColorStorage[blockGridX][blockGridZ] = oreColor;
						}
						if (x == 15 || z == 15 || x == 0 || z == 0) // We Skip 16th block to draw Borders
							blockColorStorage[blockGridX][blockGridZ] = borderColor;
						if (chunkGridZ == (chunkSize - 1) / 2 && chunkGridX == (chunkSize - 1) / 2 && x == 7 && z == 7)
							blockColorStorage[blockGridX][blockGridZ] = col(MT.Red);
					}
				}
			}
		}
	}

	@Override
	public ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack) {
		ScanMode mode = getMode(itemStack);
		clientLogic();
		return ModularScreen.simple("map", guiContext -> {
			ModularPanel panel = ModularPanel.defaultPanel(guiContext);
			panel.flex().align(Alignment.Center).size(300, 166);
			//if (config.get("client", "fullScreen", false))
			//panel.flex().full();
			IWidget mapWidget = ((IDrawable) (context, x, y, width, height) -> {
				// TODO get rid of this for loop it would be a HUGE deal to optimization
				// This block alone gets called every FRAME to draw the gui this for loop
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
				for (int i = 0; i < 16 * chunkSize; i++) {
					for (int j = 16 * chunkSize - 1; j >= 0; j--) {
						if (blockColorStorage[i][j] == col(MT.White)) continue;
//                        GuiDraw.drawRect(i, j, 1, 1, block[i][j]);
						//

						GL11.glShadeModel(GL11.GL_SMOOTH);
						int color = blockColorStorage[i][j];
						Tessellator.instance.startDrawingQuads();
//                        Tessellator.instance.addVertex(i+0.5,j+0.5,0);
//                        Tessellator.instance.setColorOpaque_I(color);
						bufferbuilder.pos(i, j, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
						bufferbuilder.pos(i, j + 1, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
						bufferbuilder.pos(i + 1, j + 1, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
						bufferbuilder.pos(i + 1, j, 0.0f).color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color)).endVertex();
						Tessellator.instance.draw();

						//TODO use verteces to draw grids instead of point to point!
					}
				}
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				String corX = "X -> " + (guiContext.getAbsMouseX() - panel.getArea().x - x + x_origin - 9);
				String corZ = "Z -> " + (guiContext.getAbsMouseY() - panel.getArea().y - y + z_origin - 9);
				String corN = " ";
				int text_color = col(MT.White);
				try {
					short mat = blockMatStorage[(guiContext.getAbsMouseX() - panel.getArea().x - x - 10)][(guiContext.getAbsMouseY() - panel.getArea().y - y - 10)];
					if (mat != 0) {
						corN = OreDictMaterial.MATERIAL_ARRAY[mat].mNameLocal;
						text_color = col(OreDictMaterial.MATERIAL_ARRAY[mat]);
					} else corN = "NaN";
				} catch (Exception e) {
					corN = "NaN";
				}
				String cor = corX + " , " + corZ + " , " + corN + ". Dim: " + player.worldObj.provider.getDimensionName() + ". Mode:" + mode.name().toLowerCase();
				GuiDraw.drawText(cor, 0, -15, 1f, text_color, true);
			}).asWidget().pos(10, 10).right(10).bottom(10);
			Grid listWidget = new Grid().scrollable().size(150, 150).pos(280 - 125, 0);
			sortedOres.forEach((matID, amount) -> {
				OreDictMaterial mat = OreDictMaterial.MATERIAL_ARRAY[matID];
                IWidget itemWidget = wItem(mat, mode);
                String name = mat.mNameLocal;
                int color = UT.Code.getRGBaInt((mat.fRGBaSolid));
                if(mat==MT.MethaneIce){
                    color = UT.Code.getRGBaInt(MT.Milk.fRGBaSolid);
                    name = CS.BlocksGT.GasNatural.getLocalizedName();
                }
				IWidget nameWidget = new TextWidget(IKey.str(name + ": " + amount)).color(color).shadow(true);
				listWidget.row(itemWidget, nameWidget).nextRow();
			});
			panel.child(mapWidget);
			panel.child(listWidget);
			return panel;
		});
	}
}
