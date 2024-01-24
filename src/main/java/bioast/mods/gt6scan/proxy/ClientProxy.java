package bioast.mods.gt6scan.proxy;

import bioast.mods.gt6scan.network.ScanMessageHandlerOnClient;
import bioast.mods.gt6scan.network.ScanResponceToClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
	@Override
	public World getClientWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);

		CommonProxy.simpleNetworkWrapper.registerMessage(ScanMessageHandlerOnClient.class, ScanResponceToClient.class,
				2, Side.CLIENT);
	}
}
