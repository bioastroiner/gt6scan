package bioast.mods.gt6scan.proxy;

import bioast.mods.gt6scan.network.scanmessage.HandlerDummy;
import bioast.mods.gt6scan.network.scanmessage.HandlerServer;
import bioast.mods.gt6scan.network.scanmessage.ScanRequest;
import bioast.mods.gt6scan.network.scanmessage.ScanResponse;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import gregapi.api.Abstract_Proxy;
import gregapi.data.LH;
import gregapi.util.UT;
import net.minecraft.world.World;

public class CommonProxy extends Abstract_Proxy {
    public static SimpleNetworkWrapper simpleNetworkWrapper;

    public CommonProxy() {
        //MinecraftForge.EVENT_BUS.register(this);


    }

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("Scanning_channel");
        simpleNetworkWrapper.registerMessage(HandlerServer.class, ScanRequest.class, 1, Side.SERVER);
        simpleNetworkWrapper.registerMessage(HandlerDummy.class, ScanResponse.class,
            2, Side.SERVER);
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

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        UT.Entities.sendchat(event.player, String.format(
            LH.Chat.PURPLE + "To Use the ScannerMod For GT6, You Must have ModularUI2 version 2.0.8 on your client.\n" + LH.Chat.GRAY
            //+     "in addition to scanner tool, you can also use command /scan [MODE] to use the GUI (experimental)"
        ));
    }
}
