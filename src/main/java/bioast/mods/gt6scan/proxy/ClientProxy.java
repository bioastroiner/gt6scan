package bioast.mods.gt6scan.proxy;

import bioast.mods.gt6scan.ItemProspectMap;
import bioast.mods.gt6scan.ScannerMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import org.lwjgl.opengl.GL11;

public class ClientProxy extends CommonProxy {
	@Override
	public World getClientWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event) {
		if (event.item.getItem() == ScannerMod.mapWritten) {
			event.renderer.renderManager.renderEngine.bindTexture(new ResourceLocation("textures/map/map_background.png"));
			Tessellator tessellator = Tessellator.instance;
			GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
			float f = 0.0078125F;
			GL11.glScalef(f, f, f);

			switch (event.entityItemFrame.getRotation()) {
				case 0:
					GL11.glTranslatef(-64.0F, -87.0F, -1.5F);
					break;
				case 1:
					GL11.glTranslatef(-66.5F, -84.5F, -1.5F);
					break;
				case 2:
					GL11.glTranslatef(-64.0F, -82.0F, -1.5F);
					break;
				case 3:
					GL11.glTranslatef(-61.5F, -84.5F, -1.5F);
			}

			GL11.glNormal3f(0.0F, 0.0F, -1.0F);
			MapData mapdata = ((ItemProspectMap) event.item.getItem()).getMapData(event.item, event.entityItemFrame.worldObj);
			GL11.glTranslatef(0.0F, 0.0F, -1.0F);

			if (mapdata != null) {
				Minecraft.getMinecraft().entityRenderer.getMapItemRenderer().func_148250_a(mapdata, true);
			}
		}
	}

}
