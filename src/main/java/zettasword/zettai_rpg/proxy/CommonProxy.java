package zettasword.zettai_rpg.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import zettasword.zettai_rpg.cmd.AddStatCommand;
import zettasword.zettai_rpg.cmd.SetStatCommand;
import zettasword.zettai_rpg.network.PacketPlayerSync;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
    }

    public void init(FMLInitializationEvent event)
    {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new SetStatCommand());
        event.registerServerCommand(new AddStatCommand());
    }

    // SECTION Packet Handlers
    // ===============================================================================================================

    public void handlePlayerSyncPacket(PacketPlayerSync.Message message){}


}
