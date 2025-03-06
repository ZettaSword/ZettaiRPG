package zettasword.zettai_rpg.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import zettasword.zettai_rpg.cap.RPGData;
import zettasword.zettai_rpg.network.PacketPlayerSync;
import zettasword.zettai_rpg.variables.StatsKeyHandler;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
  }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        StatsKeyHandler.registerKeyBindings();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    @Override
    public void handlePlayerSyncPacket(PacketPlayerSync.Message message){
        RPGData data = RPGData.get(Minecraft.getMinecraft().player);

        if(data != null){
            message.spellData.forEach(data::setVariable);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getMinecraft().player != null) {
            RPGData playerStats = RPGData.get(Minecraft.getMinecraft().player); // Replace with your logic to fetch stats
            if (playerStats != null) {
                StatsKeyHandler.handleKeyPress(playerStats);
            }
        }
    }
}