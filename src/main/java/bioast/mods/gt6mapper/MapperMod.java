package bioast.mods.gt6mapper;

import bioast.mods.gt6mapper.item.ItemEmptyProspectMap;
import bioast.mods.gt6mapper.item.ItemProspectMap;
import bioast.mods.gt6mapper.network.MapPacketHandler;
import bioast.mods.gt6mapper.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.data.*;
import gregapi.recipes.maps.RecipeMapPrinter;
import gregapi.util.CR;
import gregapi.util.ST;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static bioast.mods.gt6mapper.MapperMod.*;
import static gregapi.data.CS.*;

@Mod(modid = MODID, version = VERSION, name = MODNAME, dependencies = DEPENDENCIES)
public class MapperMod extends Abstract_Mod {
    public static final String DEPENDENCIES = "";//"required-after:gregapi";
    public static final String MODID = "GRADLETOKEN_MODID";
    public static final String MODNAME = "GRADLETOKEN_MODNAME";
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final String GROUPNAME = "GRADLETOKEN_GROUPNAME";

    public static final Logger debug = LogManager.getLogger(MODID);
    public static gregapi.code.ModData MOD_DATA = new gregapi.code.ModData(MODID, MODNAME);
    public static MapperMod instance;
    @SidedProxy(clientSide = "bioast.mods.gt6mapper.proxy.ClientProxy", serverSide = "bioast.mods.gt6mapper.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static ItemProspectMap mapWritten;
    public static Item mapEmpty;

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
        mapWritten = (ItemProspectMap) new ItemProspectMap().setUnlocalizedName("prospectingMap").setMaxStackSize(1);
        mapEmpty = new ItemEmptyProspectMap().setUnlocalizedName("emptyProspectingMap").setMaxStackSize(1);
        GameRegistry.registerItem(mapWritten, mapWritten.getUnlocalizedName(), MODID);
        GameRegistry.registerItem(mapEmpty, mapEmpty.getUnlocalizedName(), MODID);
        LH.add(mapWritten.getUnlocalizedName(), "Geographical Prospecting Map");
        LH.add(mapEmpty.getUnlocalizedName(), "Empty Geographical Prospecting Map");
        CR.shaped(ST.make(mapEmpty, 1, W), CR.DEF, "XXX", "XBX", "XXX", 'X', OreDictionary.getOres("paper"), 'B', OD.itemRock);
        //RM.Printer.addFakeRecipe(F, ST.array(ST.make(mapEmpty, 1, W), IL.USB_Stick_1.getWithName(0, "Containing scanned Prospecting Map")), ST.array(ST.make(mapWritten, 1, W)), null, null, FL.array(FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Yellow], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Magenta], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Cyan], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Black], 1, 9, T)), ZL_FS, 64, 16, 0);
        //RM.ScannerVisuals.addFakeRecipe(F, ST.array(ST.make(mapWritten, 1, W), IL.USB_Stick_1.get(1)), ST.array(IL.USB_Stick_1.getWithName(1, "Containing scanned Prospecting Map"), ST.make(mapWritten, 1, W)), null, null, ZL_FS, ZL_FS, 64, 16, 0);
        // inject to gregapi.recipes.maps.RecipeMapPrinter.findRecipe some code to make recipes actually generate

        /*
        //GameRegistry.registerTileEntity(CartographyTableTE.class,"tileCartographyTable");
        new MultiTileEntityRegistry("scanner.multitileentity");
*/
    }

    @Override
    public void onModInit2(FMLInitializationEvent aEvent) {
        MinecraftForge.EVENT_BUS.register(this);
//        FMLCommonHandler.instance().bus().register(eventListener); // we're getting events off this bus too

        proxy.init(aEvent);
        NetworkRegistry.INSTANCE.newEventDrivenChannel(ItemProspectMap.STR_ID).register(new MapPacketHandler());
        /*
        MultiTileEntityBlock aMachine = MultiTileEntityBlock.getOrCreate(MD.GT.mID, "machine"      , MaterialMachines.instance , Block.soundTypeMetal, TOOL_wrench , 0, 0, 15, F, F
        );
        MultiTileEntityRegistry.getRegistry("scanner.multitileentity")
            .add("Cartography Table","Machines",0,0, MultiTileEntityCartographyTable.class,0,16,aMachine,
                UT.NBT.make(CS.NBT_MATERIAL, MT.Steel));
                */
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
