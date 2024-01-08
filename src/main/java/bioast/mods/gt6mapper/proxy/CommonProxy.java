package bioast.mods.gt6mapper.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import gregapi.api.Abstract_Proxy;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy extends Abstract_Proxy {

	public CommonProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	// preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
	// GameRegistry." (Remove if not needed)
	public void preInit(FMLPreInitializationEvent event) {
	}

	// load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
	public void init(FMLInitializationEvent event) {
	}

	// postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
	public void postInit(FMLPostInitializationEvent event) {
	}

	// register server commands in this event handler (Remove if not needed)
	public void serverStarting(FMLServerStartingEvent event) {
	}

	public World getClientWorld() {
		return null;
	}

//    @SubscribeEvent
//    public void onPlayerUseEvent(PlayerInteractEvent aEvent) {
//        if (aEvent.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
//        }
//    }
}
