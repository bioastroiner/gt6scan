package bioast.mods.gt6scan;

import bioast.mods.gt6scan.item.ScannerMultiTool;
import bioast.mods.gt6scan.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.config.Config;
import gregapi.data.CS;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.CR;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static bioast.mods.gt6scan.ScannerMod.*;

@Mod(modid = MODID, version = VERSION, name = MODNAME, dependencies = DEPENDENCIES)
public class ScannerMod extends Abstract_Mod {
    public static final String DEPENDENCIES = "after:modularui@[2.0.9-1.7.10);required-after:gregapi";
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
    public static ScannerMultiTool tool;

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

    @Override
    public void onModPreInit2(FMLPreInitializationEvent aEvent) {
        instance = this;
        proxy.preInit(aEvent);
        config = new Config(CS.DirectoriesGT.CONFIG_GT, "scanner.cfg");
        String eu = config.get("core",
            "ToolConsumptionEnergy Def:EU, Poss: STEAM, MJ, RF, AU, QU, MU, LU, HU, CU, KU, RU, EU, NU, TU, (put any invalid item to make it consume no energy)",
            "EU");
        tool = new ScannerMultiTool();
        tool.mainConsumptionEnergyType = eu;
        tool.storage_multiplier = config.get("core",
            "storage_multiplier (this gets multiplied by the voltage of each tier)",
            8000);
        ScannerMultiTool.consumptionRate = config.get("core",
            "consumptionRate (def:50) 50 * voltage for each usage of the scanner",
            50);
    }

    @Override
    public void onModInit2(FMLInitializationEvent aEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        proxy.init(aEvent);
    }

    @Override
    public void onModPostInit2(FMLPostInitializationEvent aEvent) {
        proxy.postInit(aEvent);
        OreDictMaterial[] Metals = {
            null,
            null,
            MT.SteelGalvanized,
            MT.Aluminium,
            MT.Titanium,
            MT.TungstenSteel
        };
        String[] OD_USB_STICKS = {"gt:usbstick0", "gt:usbstick1", "gt:usbstick2", "gt:usbstick3", "gt:usbstick4", "gt:usbstick4"};
        for (int i = 2; i < 6; i++) {
            CR.shaped(tool.make(i), new Object[]{
                "FCs",
                "MPM",
                "MDM",
                'M', OP.plate.mat(Metals[i], 1),
                'C', CS.OD_CIRCUITS[i],
                'P', OP.foil.mat(MT.Plastic, 1),
                'D', OD_USB_STICKS[i],
                'F', IL.SENSORS[i].get(1),
//                'B', IL.Batter // todo batteries
            });
        }
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
}
