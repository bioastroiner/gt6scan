package bioast.mods.gt6mapper.block;

import bioast.mods.gt6mapper.MapperMod;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.block.multitileentity.IMultiTileEntity;
import gregapi.block.multitileentity.MultiTileEntityContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import gregapi.render.ITexture;
import gregapi.render.RenderHelper;
import gregapi.tileentity.base.TileEntityBase07Paintable;
import gregapi.util.UT;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import static gregapi.data.CS.*;
import static gregapi.data.CS.NBT_TEXTURE;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 */
public class MultiTileEntityCartographyTable extends TileEntityBase07Paintable implements IMultiTileEntity.IMTE_OnRegistrationFirstClient, IMultiTileEntity.IMTE_OnRegistrationClient, IMultiTileEntity.IMTE_OnPlaced {

    // via NBT
    public String mTextureName = "";
    protected OreDictMaterial mMaterial = MT.NULL;

    @Override
    public boolean onBlockActivated3(EntityPlayer aPlayer, byte aSide, float aHitX, float aHitY, float aHitZ) {
        if(isServerSide()){
            ItemStack aStack = aPlayer.getCurrentEquippedItem();
            if(SIDES_TOP[aSide]){
                // only accept work from top easy
                UT.Entities.sendchat(aPlayer,"TO BE CONTUNTED!!!");
            }
        }
        return T;
    }

    @Override
    public ItemStack[] getDefaultInventory(NBTTagCompound aNBT) {
        return new ItemStack[2]; // 2 input 1 output
    }

    @Override
    public boolean canInsertItem2(int aSlot, ItemStack aStack, byte aSide) {
        return F;
    }

    @Override
    public boolean canExtractItem2(int aSlot, ItemStack aStack, byte aSide) {
        return F;
    }

    @Override
    public void readFromNBT2(NBTTagCompound aNBT) {
        super.readFromNBT2(aNBT);
        if (aNBT.hasKey(NBT_TEXTURE)) mTextureName = aNBT.getString(NBT_TEXTURE);
        if (aNBT.hasKey(NBT_MATERIAL)) mMaterial = OreDictMaterial.get(aNBT.getString(NBT_MATERIAL));
    }

    @Override
    public void writeToNBT2(NBTTagCompound aNBT) {
        super.writeToNBT2(aNBT);

    }

    @Override
    public boolean canDrop(int aSlot) {
        return T;
    }

    @Override
    public ITexture getTexture2(Block aBlock, int aRenderPass, byte aSide, boolean[] aShouldSideBeRendered) {
        return null;
    }

    @Override
    public String getTileEntityName() {
        return "scanner.multitileentity.cartography";
    }

    @SideOnly(Side.CLIENT)
    private static MultiTileEntityCartographyTable.MultiTileEntityRendererCartographyTable RENDERER;

    @Override
    public void onRegistrationFirstClient(MultiTileEntityRegistry aRegistry, short aID) {
        ClientRegistry.bindTileEntitySpecialRenderer(getClass(), RENDERER = new MultiTileEntityRendererCartographyTable());
    }

    @Override
    public void onRegistrationClient(MultiTileEntityRegistry aRegistry, short aID) {
        //RENDERER.mResources.put(mTextureName, new ResourceLocation[] {new ResourceLocation(MD.GT.mID, TEX_DIR_MODEL + aRegistry.mNameInternal + "/" + mTextureName + ".colored.png"), new ResourceLocation(MD.GT.mID, TEX_DIR_MODEL + aRegistry.mNameInternal + "/" + mTextureName + ".plain.png")});
    }

    @Override
    public boolean onPlaced(ItemStack aStack, EntityPlayer aPlayer, MultiTileEntityContainer aMTEContainer, World aWorld, int aX, int aY, int aZ, byte aSide, float aHitX, float aHitY, float aHitZ) {
        slot(0,new ItemStack(Items.apple));
        return T;
    }

    @SideOnly(Side.CLIENT)
    public static class MultiTileEntityRendererCartographyTable extends TileEntitySpecialRenderer{
        public static final IModelCustom sModel = AdvancedModelLoader.loadModel(new ResourceLocation(MapperMod.MODID,"models/worktable.obj"));
        public static final ResourceLocation sTexture = new ResourceLocation(MapperMod.MODID,"models/worktable.png");
        @Override
        public void renderTileEntityAt(TileEntity aTileEntity, double aX, double aY, double aZ, float aPartialTick){
            MultiTileEntityCartographyTable aTile = (MultiTileEntityCartographyTable) aTileEntity;
            bindTexture(sTexture);
            // FIXME textures still broken? but one got rendered just fine
            glPushMatrix();
            glTranslated(aX + 0.5f, aY + 0.5f , aZ + 0.5f );
            glPushMatrix();
            //GL11.glRotatef(180, 0F, 1F, 0.5F);
            sModel.renderAll();
            glPopMatrix();
            glPopMatrix();

            // Render Item Stack
            if(aTile.slotHas(0)){
                //FIXME item renders but it vanishes after some ticks
                int tTexIndex = glGetInteger(GL_TEXTURE_BINDING_2D);
                glDisable(GL_BLEND);
                glDisable(GL_LIGHTING);

                glPushMatrix();
                glTranslated(aX+0.5+OFFX[0]*0.502-OFFZ[0]*0.25, aY+0.625, aZ+0.5+OFFZ[0]*0.502+OFFX[0]*0.25);

                glRotatef(90, 0, 1, 0);
                //glRotatef(COMPASS_FROM_SIDE[0] * 90, 0, 1, 0);
                glScalef(1/256F, 1/256F, -0.0001F);
                glScalef(8.0F, 8.0F, 1.0F);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

                try {
                    if (!ForgeHooksClient.renderInventoryItem(RenderHelper.mRenderBlocks, Minecraft.getMinecraft().renderEngine, aTile.slot(0), T, 0, 0, 0)) {
                        RenderHelper.renderItemIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().renderEngine, aTile.slot(0), 0, 0, F);
                    }
                } catch(Throwable e) {e.printStackTrace(ERR);}
                glPopMatrix();
                glColor4f(1, 1, 1, 1);
                glAlphaFunc(GL_GREATER, 0.1F);
                OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                glDisable(GL_BLEND);
                glEnable(GL_LIGHTING);
                glEnable(GL_ALPHA_TEST);
                glBindTexture(GL_TEXTURE_2D, tTexIndex);
            }
        }
    }

//    @SideOnly(Side.CLIENT)
//    public static class MultiTileEntityModelCartographyTable extends ModelBase {
//        private final ModelRenderer mLid, mBottom, mKnob;
//
//        public MultiTileEntityModelCartographyTable() {
//            mLid = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
//            mLid.addBox(0, -5, -14, 14, 5, 14, 0);
//            mLid.rotationPointX =  1;
//            mLid.rotationPointY =  7;
//            mLid.rotationPointZ = 15;
//            mKnob = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
//            mKnob.addBox(-1, -2, -15, 2, 4, 1, 0);
//            mKnob.rotationPointX =  8;
//            mKnob.rotationPointY =  7;
//            mKnob.rotationPointZ = 15;
//            mBottom = (new ModelRenderer(this, 0, 19)).setTextureSize(64, 64);
//            mBottom.addBox(0, 0, 0, 14, 10, 14, 0);
//            mBottom.rotationPointX = 1;
//            mBottom.rotationPointY = 6;
//            mBottom.rotationPointZ = 1;
//        }
//
//        public void render(double aLidAngle) {
//            mKnob.rotateAngleX = mLid.rotateAngleX = (float)aLidAngle;
//            mLid.render(0.0625F);
//            mKnob.render(0.0625F);
//            mBottom.render(0.0625F);
//        }
//    }
}
