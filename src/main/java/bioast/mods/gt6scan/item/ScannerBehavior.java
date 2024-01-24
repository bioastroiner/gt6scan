package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.network.ScanRequestToServer;
import bioast.mods.gt6scan.proxy.CommonProxy;
import gregapi.item.multiitem.MultiItem;
import gregapi.item.multiitem.behaviors.IBehavior;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ScannerBehavior extends IBehavior.AbstractBehaviorDefault {
    @Override
    public ItemStack onItemRightClick(MultiItem aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        ScanRequestToServer req = new ScanRequestToServer(ScanMode.LARGE, (int) aPlayer.posX, (int) aPlayer.posZ);
        if (aWorld.isRemote)
            CommonProxy.simpleNetworkWrapper.sendToServer(req);

        return super.onItemRightClick(aItem, aStack, aWorld, aPlayer);
    }
}
