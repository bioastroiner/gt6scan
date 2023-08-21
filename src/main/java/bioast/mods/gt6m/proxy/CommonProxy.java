package bioast.mods.gt6m.proxy;

import bioast.mods.gt6m.Config;
import bioast.mods.gt6m.scanner.NetwrokHandler;
import bioast.mods.gt6m.scanner.item.gui.ScannerGUI;
import bioast.mods.gt6m.scanner.utils.VALs;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import gregapi.api.Abstract_Proxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CommonProxy extends Abstract_Proxy implements IGuiHandler {

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
//        FMLCommonHandler.instance().bus().register(this);
        new NetwrokHandler();
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
//            World aWorld = aEvent.world;
//            Block tBlock = aEvent.world.getBlock(aEvent.x, aEvent.y, aEvent.z);
//            short tMeta = (short) aWorld.getBlockMetadata(aEvent.x, aEvent.y, aEvent.z);
//            UT.Entities.sendchat(aEvent.entityPlayer,"ORE: "+HLPs.ore(tBlock,tMeta));
//            UT.Entities.sendchat(aEvent.entityPlayer,"MAT: "+HLPs.oreId(tBlock,aWorld.getTileEntity(aEvent.x, aEvent.y, aEvent.z)));
//
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == VALs.GUI_ID) return null;
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == VALs.GUI_ID) return new ScannerGUI();
        return null;
    }

    public void openProspectorGUI() {

    }
}
