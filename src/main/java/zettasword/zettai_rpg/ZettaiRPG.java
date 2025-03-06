package zettasword.zettai_rpg;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;
import zettasword.zettai_rpg.cap.RPGData;
import zettasword.zettai_rpg.network.ZettaiPacketHandler;
import zettasword.zettai_rpg.proxy.CommonProxy;
import zettasword.zettai_rpg.variables.Stats;

@Mod(modid = ZettaiRPG.MODID, dependencies = "")
public class ZettaiRPG
{
    public static final String MODID = "zettai_rpg";

    public static Logger log;

    @SidedProxy(clientSide = "zettasword.zettai_rpg.proxy.ClientProxy", serverSide = "zettasword.zettai_rpg.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        log = event.getModLog();
        proxy.preInit(event);
        ModConfig.preInit(event);
        RPGData.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        ZettaiPacketHandler.initPackets();
        Stats.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);

    }
}
