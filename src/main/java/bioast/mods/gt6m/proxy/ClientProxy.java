package bioast.mods.gt6m.proxy;

import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.proxy.CommonProxy;
import bioast.mods.gt6m.scanner.utils.VALs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy {


    @Override
    public void openProspectorGUI() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        player.openGui(
                GT6M_Mod.instance,
                VALs.GUI_ID,
                player.worldObj,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ);
    }

}
