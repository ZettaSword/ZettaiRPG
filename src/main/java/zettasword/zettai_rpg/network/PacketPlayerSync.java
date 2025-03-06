package zettasword.zettai_rpg.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import zettasword.zettai_rpg.ZettaiRPG;
import zettasword.zettai_rpg.cap.IVariable;
import zettasword.zettai_rpg.cap.RPGData;

import java.util.*;

/**
 * <b>[Server -> Client]</b> This packet is sent to synchronise any fields that need synchronising in
 * {@link RPGData WizardData}. This packet is not sent often enough and is too small to warrant
 * having separate packets for each field that needs synchronising.
 */
public class PacketPlayerSync implements IMessageHandler<PacketPlayerSync.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> ZettaiRPG.proxy.handlePlayerSyncPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {
		public Map<IVariable, Object> spellData;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Map<IVariable, Object> spellData){
			this.spellData = spellData;
		}

		@Override
		public void fromBytes(ByteBuf buf){

			this.spellData = new HashMap<>();
			RPGData.getSyncedVariables().forEach(v -> spellData.put(v, v.read(buf)));
			// Have to send empty tags to guarantee correct ByteBuf size/order, but no point keeping the resulting nulls
			spellData.values().removeIf(Objects::isNull);
		}

		@Override
		@SuppressWarnings("unchecked") // We know it's ok
		public void toBytes(ByteBuf buf){
			RPGData.getSyncedVariables().forEach(v -> v.write(buf, spellData.get(v)));
		}
	}
}
