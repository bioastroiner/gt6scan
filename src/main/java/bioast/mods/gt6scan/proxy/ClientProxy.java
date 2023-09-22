package bioast.mods.gt6scan.proxy;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
	@Override
	public World getClientWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
	}
}
