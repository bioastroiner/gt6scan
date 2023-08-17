package bioast.mods.gt6m.mixin.gregtech;

import bioast.mods.gt6m.Config;
import bioast.mods.gt6m.GT6M_Mod;
import gregapi.item.multiitem.tools.ToolStats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ToolStats.class,remap = false)
public class GT_ToolStats_Mixin {

    //    @Shadow()
//    private long mMaterialAmount;
    @Inject(method = "getBrokenItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    public void getBrokenItem(ItemStack aStack, CallbackInfoReturnable<ItemStack> cir) {
        //ItemStack stack = mMaterialAmount < U4 ? null : OP.scrapGt.mat(MultiItemTool.getPrimaryMaterial(aStack), 1 + RNGSUS.nextInt(1 + (int) (4 * mMaterialAmount / U)));
        GT6M_Mod.LOG.debug("No Scraps Added!");
        if(Config.PENALTY_NO.getBool())  cir.setReturnValue(null);
    }

    @Inject(method = "afterBreaking(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)V", at = @At("HEAD"), cancellable = true)
    public void afterBreaking(ItemStack aStack, EntityPlayer aPlayer, CallbackInfo ci) {
        // Greg: "If you work so hard that your Tool breaks, you should probably take a break yourself. :P"
        // Bio: No :3
        GT6M_Mod.LOG.debug("No More Nasty Potion Effects");
        if(Config.SCRAP_NO.getBool())
            ci.cancel();

    }
}
