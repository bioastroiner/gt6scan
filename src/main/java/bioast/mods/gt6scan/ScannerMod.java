package bioast.mods.gt6scan;

import bioast.mods.gt6scan.item.ScannerBehavior;
import bioast.mods.gt6scan.item.ScannerMultiTool;
import bioast.mods.gt6scan.proxy.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.config.Config;
import gregapi.data.CS;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twilightforest.TwilightForestMod;
import twilightforest.item.ItemTFMagicMap;

import static bioast.mods.gt6scan.ScannerMod.*;

@Mod(modid = MODID, version = VERSION, name = MODNAME,dependencies = DEPENDENCIES)
public class ScannerMod extends Abstract_Mod {
    public static final String DEPENDENCIES = "required-after:modularui@[2.0.6,);after:gregapi";
    public static final String MODID = "GRADLETOKEN_MODID";
    public static final String MODNAME = "GRADLETOKEN_MODNAME";
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final String GROUPNAME = "GRADLETOKEN_GROUPNAME";

    public static final Logger debug = LogManager.getLogger(MODID);
    public static gregapi.code.ModData MOD_DATA = new gregapi.code.ModData(MODID, MODNAME);
    public static ScannerMod instance;
    public static Config config;
    @SidedProxy(clientSide = "bioast.mods.gt6scan.proxy.ClientProxy", serverSide = "bioast.mods.gt6scan.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Override
    public String getModID() {
        return MODID;
    }

    @Override
    public String getModName() {
        return MODNAME;
    }

    @Override
    public String getModNameForLog() {
        return MODNAME;
    }

    @Override
    public Abstract_Proxy getProxy() {
        return proxy;
    }

    // Do not change these 7 Functions. Just keep them this way.
    @cpw.mods.fml.common.Mod.EventHandler
    public final void onPreLoad(cpw.mods.fml.common.event.FMLPreInitializationEvent aEvent) {
        onModPreInit(aEvent);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public final void onLoad(cpw.mods.fml.common.event.FMLInitializationEvent aEvent) {
        onModInit(aEvent);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public final void onPostLoad(cpw.mods.fml.common.event.FMLPostInitializationEvent aEvent) {
        onModPostInit(aEvent);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public final void onServerStarting(cpw.mods.fml.common.event.FMLServerStartingEvent aEvent) {
        onModServerStarting(aEvent);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public final void onServerStarted(cpw.mods.fml.common.event.FMLServerStartedEvent aEvent) {
        onModServerStarted(aEvent);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public final void onServerStopping(cpw.mods.fml.common.event.FMLServerStoppingEvent aEvent) {
        onModServerStopping(aEvent);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public final void onServerStopped(cpw.mods.fml.common.event.FMLServerStoppedEvent aEvent) {
        onModServerStopped(aEvent);
    }

    @Override
    public void onModPreInit2(FMLPreInitializationEvent aEvent) {
        instance = this;
        proxy.preInit(aEvent);
        config = new Config(CS.DirectoriesGT.CONFIG_GT,"scanner.cfg");
        new ScannerMultiTool();
        Item item = new ItemProspectMap().setUnlocalizedName("prospectingMap").setMaxStackSize(1);
        GameRegistry.registerItem(item, item.getUnlocalizedName(), MODID);
    }

    @Override
    public void onModInit2(FMLInitializationEvent aEvent) {
        MinecraftForge.EVENT_BUS.register(this);
//        FMLCommonHandler.instance().bus().register(eventListener); // we're getting events off this bus too

        proxy.init(aEvent);
        NetworkRegistry.INSTANCE.newEventDrivenChannel(ItemProspectMap.STR_ID).register( new MapPacketHandler());

        if(config.get("core","useCheatTool",true)){
            CS.ItemsGT.TOOLS.addItemBehavior(9001,new ScannerBehavior());
        }
    }

    @Override
    public void onModPostInit2(FMLPostInitializationEvent aEvent) {
        proxy.postInit(aEvent);
    }

    @Override
    public void onModServerStarting2(FMLServerStartingEvent aEvent) {
        proxy.serverStarting(aEvent);
    }

    @Override
    public void onModServerStarted2(FMLServerStartedEvent aEvent) {

    }

    @Override
    public void onModServerStopping2(FMLServerStoppingEvent aEvent) {

    }

    @Override
    public void onModServerStopped2(FMLServerStoppedEvent aEvent) {

    }
}
