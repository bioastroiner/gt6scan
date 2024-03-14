package bioast.mods.gt6scan.proxy;

import bioast.mods.gt6scan.network.scanmessage.HandlerClient;
import bioast.mods.gt6scan.network.scanmessage.ScanResponse;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        CommonProxy.simpleNetworkWrapper.registerMessage(HandlerClient.class, ScanResponse.class,
            2, Side.CLIENT);
    }

    @Override
    public World getClientWorld() {
        return FMLClientHandler.instance().getClient().theWorld;
    }
}
