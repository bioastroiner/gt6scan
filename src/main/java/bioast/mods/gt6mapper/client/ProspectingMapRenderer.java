package bioast.mods.gt6mapper.client;

import bioast.mods.gt6mapper.MapperMod;
import bioast.mods.gt6mapper.world.ProspectMapData;
import bioast.mods.gt6mapper.item.ItemProspectMap;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import java.util.Collection;

@Deprecated()
public class ProspectingMapRenderer implements IItemRenderer {
	private static final ResourceLocation vanillaMapIcons = new ResourceLocation("textures/map/map_icons.png");
	//private static final ResourceLocation twilightMapIcons = new ResourceLocation(TwilightForestMod.GUI_DIR + "mapicons.png");
	private static final ResourceLocation mapBackgroundTextures = new ResourceLocation("textures/map/map_background.png");
	private final ResourceLocation textureLoc;
	private int[] intArray = new int[16384];
	private DynamicTexture bufferedImage;

	public ProspectingMapRenderer(GameSettings par1GameSettings, TextureManager par2TextureManager) {
		this.bufferedImage = new DynamicTexture(128, 128);
		this.textureLoc = par2TextureManager.getDynamicTextureLocation("map", this.bufferedImage);
		this.intArray = this.bufferedImage.getTextureData();

		for (int i = 0; i < this.intArray.length; ++i) {
			this.intArray[i] = 0;
		}
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return (type == ItemRenderType.FIRST_PERSON_MAP || (RenderItem.renderInFrame && type == ItemRenderType.ENTITY));
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (type == ItemRenderType.FIRST_PERSON_MAP) {
			// get our stuff out of that object array
			EntityPlayer player = (EntityPlayer) data[0];
			TextureManager renderEngine = (TextureManager) data[1];
			MapData mapData = (MapData) data[2];
			// if we have data, render it
			if (mapData != null && mapData instanceof ProspectMapData) {
				renderMap(player, renderEngine, (ProspectMapData) mapData);
			}
		} else if (RenderItem.renderInFrame) {
			EntityItem entity = (EntityItem) data[1];

			ProspectMapData mapData = ((ItemProspectMap) MapperMod.mapWritten).getMapData(item, entity.worldObj);


			// if we have data, render it
			if (mapData != null) {
				renderMapInFrame(item, RenderManager.instance, mapData);
			}
		}
	}

	public void renderMap(EntityPlayer par1EntityPlayer, TextureManager par2TextureManager, ProspectMapData par3MapData) {
		for (int i = 0; i < 16384; ++i) {
			int colorByte = par3MapData.colors[i] & 0xFF;

			if (colorByte == 0) {
				this.intArray[i] = (i + i / 128 & 1) * 8 + 16 << 24;
			} else {
				int biomeID = colorByte - 1;
				BiomeGenBase biome = BiomeGenBase.getBiomeGenArray()[biomeID];
				if (biome != null) {
					this.intArray[i] = -16777216 | biome.color;
				}
			}
		}

		this.bufferedImage.updateDynamicTexture();
		byte var15 = 0;
		byte var16 = 0;
		Tessellator tesselator = Tessellator.instance;
		float var18 = 0.0F;
		par2TextureManager.bindTexture(this.textureLoc);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		tesselator.startDrawingQuads();
		tesselator.addVertexWithUV((double) ((float) (var15 + 0) + var18), (double) ((float) (var16 + 128) - var18), -0.009999999776482582D, 0.0D, 1.0D);
		tesselator.addVertexWithUV((double) ((float) (var15 + 128) - var18), (double) ((float) (var16 + 128) - var18), -0.009999999776482582D, 1.0D, 1.0D);
		tesselator.addVertexWithUV((double) ((float) (var15 + 128) - var18), (double) ((float) (var16 + 0) + var18), -0.009999999776482582D, 1.0D, 0.0D);
		tesselator.addVertexWithUV((double) ((float) (var15 + 0) + var18), (double) ((float) (var16 + 0) + var18), -0.009999999776482582D, 0.0D, 0.0D);
		tesselator.draw();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		par2TextureManager.bindTexture(vanillaMapIcons);
		for (MapData.MapCoord mapCoord : (Collection<MapData.MapCoord>) par3MapData.playersVisibleOnMap.values()) {
			GL11.glPushMatrix();
			GL11.glTranslatef((float) var15 + (float) mapCoord.centerX / 2.0F + 64.0F, (float) var16 + (float) mapCoord.centerZ / 2.0F + 64.0F, -0.04F);
			GL11.glRotatef((float) (mapCoord.iconRotation * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
			GL11.glScalef(4.0F, 4.0F, 3.0F);
			GL11.glTranslatef(-0.125F, 0.125F, 0.0F);
			float var21 = (float) (mapCoord.iconSize % 4 + 0) / 4.0F;
			float var23 = (float) (mapCoord.iconSize / 4 + 0) / 4.0F;
			float var22 = (float) (mapCoord.iconSize % 4 + 1) / 4.0F;
			float var24 = (float) (mapCoord.iconSize / 4 + 1) / 4.0F;
			tesselator.startDrawingQuads();
			tesselator.addVertexWithUV(-1.0D, 1.0D, 0.0D, (double) var21, (double) var23);
			tesselator.addVertexWithUV(1.0D, 1.0D, 0.0D, (double) var22, (double) var23);
			tesselator.addVertexWithUV(1.0D, -1.0D, 0.0D, (double) var22, (double) var24);
			tesselator.addVertexWithUV(-1.0D, -1.0D, 0.0D, (double) var21, (double) var24);
			tesselator.draw();
			GL11.glPopMatrix();
		}

//		par2TextureManager.bindTexture(twilightMapIcons);
//
//		for(MapData.MapCoord mapCoord : (List<MapData.MapCoord>) par3MapData.featuresVisibleOnMap)
//		{
//			GL11.glPushMatrix();
//			GL11.glTranslatef((float)var15 + (float)mapCoord.centerX / 2.0F + 64.0F, (float)var16 + (float)mapCoord.centerZ / 2.0F + 64.0F, -0.02F);
//			GL11.glRotatef((float)(mapCoord.iconRotation * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
//			GL11.glScalef(4.0F, 4.0F, 3.0F);
//			GL11.glTranslatef(-0.125F, 0.125F, 0.0F);
//			float var21 = (float)(mapCoord.iconSize % 8 + 0) / 8.0F;
//			float var23 = (float)(mapCoord.iconSize / 8 + 0) / 8.0F;
//			float var22 = (float)(mapCoord.iconSize % 8 + 1) / 8.0F;
//			float var24 = (float)(mapCoord.iconSize / 8 + 1) / 8.0F;
//			tesselator.startDrawingQuads();
//			tesselator.addVertexWithUV(-1.0D, 1.0D, 0.0D, (double)var21, (double)var23);
//			tesselator.addVertexWithUV(1.0D, 1.0D, 0.0D, (double)var22, (double)var23);
//			tesselator.addVertexWithUV(1.0D, -1.0D, 0.0D, (double)var22, (double)var24);
//			tesselator.addVertexWithUV(-1.0D, -1.0D, 0.0D, (double)var21, (double)var24);
//			tesselator.draw();
//			GL11.glPopMatrix();
//		}

//        GL11.glPushMatrix();
//        GL11.glTranslatef(0.0F, 0.0F, -0.04F);
//        GL11.glScalef(1.0F, 1.0F, 1.0F);
//        this.fontRenderer.drawString(par3MapData.mapName, var15, var16, -16777216);
//        GL11.glPopMatrix();
	}

	private void renderMapInFrame(ItemStack item, RenderManager renderManager, ProspectMapData mapData) {

		// do some rotations so that we get vaguely in the right place
		GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(0.00781250F, 0.00781250F, 0.00781250F);
		GL11.glTranslatef(-65.0F, -111.0F, -3.0F);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F);

		// draw background
		renderManager.renderEngine.bindTexture(mapBackgroundTextures);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		byte b0 = 7;
		tessellator.addVertexWithUV((double) (0 - b0), (double) (128 + b0), 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double) (128 + b0), (double) (128 + b0), 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double) (128 + b0), (double) (0 - b0), 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV((double) (0 - b0), (double) (0 - b0), 0.0D, 0.0D, 0.0D);
		tessellator.draw();

		// push map texture slightly off background
		GL11.glTranslatef(0.0F, 0.0F, -1.0F);

		// draw map
		renderMap(null, renderManager.renderEngine, mapData);
	}

}
