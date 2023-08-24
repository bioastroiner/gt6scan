package bioast.mods.gt6m.proxy;

import bioast.mods.gt6m.Config;
import bioast.mods.gt6m.GT6M_Mod;
import bioast.mods.gt6m.network.PacketScannerData;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.api.Abstract_Proxy;
import gregapi.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CommonProxy extends Abstract_Proxy {

    public static gregapi.network.NetworkHandler NetwrokHandler = new NetworkHandler(GT6M_Mod.MODID, "GTM", new PacketScannerData(0));

    CommonProxy() {
        MinecraftForge.EVENT_BUS.register(this);

    }

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
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

    @SubscribeEvent
    public void onPlayerUseEvent(PlayerInteractEvent aEvent) {
        if (aEvent.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
        }
    }
}
