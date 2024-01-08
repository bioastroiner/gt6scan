package bioast.mods.gt6mapper.block;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

import static bioast.mods.gt6mapper.MapperMod.MODID;

public class CartographyTableRenderer extends TileEntitySpecialRenderer {

    ResourceLocation texture;
    ResourceLocation objModelLocation;
    IModelCustom model;
    public CartographyTableRenderer() {
        texture = new ResourceLocation(MODID, "models/worktable.png");
        objModelLocation = new ResourceLocation(MODID, "models/worktable.obj");
        model = AdvancedModelLoader.loadModel(objModelLocation);
    }
    @Override
    public void renderTileEntityAt(TileEntity te, double posX, double posY, double posZ, float timeSinceLastTick) {
        bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glTranslated(posX + 0.5, posY + 0.5, posZ + 0.5);
        GL11.glPushMatrix();
        model.renderAll();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
