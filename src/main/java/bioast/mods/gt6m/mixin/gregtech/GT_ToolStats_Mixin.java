package bioast.mods.gt6m.mixin.gregtech;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bioast.mods.gt6m.Config;
import gregapi.item.multiitem.tools.ToolStats;

@Mixin(value = ToolStats.class, remap = false)
public class GT_ToolStats_Mixin {

    @Inject(
        method = "getBrokenItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
        at = @At("RETURN"),
        cancellable = true)
    public void getBrokenItem(ItemStack aStack, CallbackInfoReturnable<ItemStack> cir) {
        if (Config.SCRAP_NO.getBoolean()) cir.setReturnValue(null);
    }

    @Inject(
        method = "afterBreaking(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)V",
        at = @At("HEAD"),
        cancellable = true)
    public void afterBreaking(ItemStack aStack, EntityPlayer aPlayer, CallbackInfo ci) {
        // Greg: "If you work so hard that your Tool breaks, you should probably take a break yourself. :P"
        // Bio: No :3
        if (Config.PENALTY_NO.getBoolean()) ci.cancel();
    }
}
