package zettasword.zettai_rpg.cap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import zettasword.zettai_rpg.ZettaiRPG;
import zettasword.zettai_rpg.network.PacketPlayerSync;
import zettasword.zettai_rpg.network.ZettaiPacketHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ZettaSword: This is modified version of EBWiz capability system.
 * OBVIOUSLY it's mentioned in the mod description.
 * OBVIOUSLY I could have made my own, and I did in some of my mods, but I believe that Electroblob's implementation is more interesting.
 * There goes default description.
 * Capability-based replacement for the old ExtendedPlayer class from 1.7.10. This has been reworked to leave minimum
 * external changes (for my own sanity, mainly!). Turns out the only major difference between an internal capability and
 * an IEEP is a couple of redundant classes and a different way of registering it.
 * <p></p>
 * Forge seems to have separate classes to hold the Capability<...> instance ('key') and methods for getting the
 * capability, but in my opinion there are already too many classes to deal with, so I'm not adding any more than are
 * necessary, meaning those constants and values are kept here instead.
 * 
 * @since Wizardry 2.1
 * @author Electroblob
 */
// On the plus side, having to rethink this class allowed me to clean it up a lot.
@Mod.EventBusSubscriber
public class RPGData implements INBTSerializable<NBTTagCompound> {

	/** Static instance of what I like to refer to as the capability key. Private because, well, it's internal! */
	// This annotation does some crazy Forge magic behind the scenes and assigns this field a value.
	@CapabilityInject(RPGData.class)
	private static final Capability<RPGData> RPG_DATA_CAPABILITY = null;

	/** Internal storage of registered variable keys. This only contains the stored keys. */
	private static final Set<IStoredVariable> storedVariables = new HashSet<>();

	/** The player this WizardData instance belongs to. */
	private final EntityPlayer player;

	/** Internal storage of custom (spell-specific) data. Note that a {@code Map} cannot specify that its values are of
	 * the same type as the type parameter of its keys, so to ensure this condition always holds, the map must only
	 * be modified via {@link RPGData#setVariable(IVariable, Object)}, which (as a method) is able to enforce it. */
	private final Map<IVariable, Object> spellData;

	public RPGData(){
		this(null); // Nullary constructor for the registration method factory parameter
	}

	public RPGData(EntityPlayer player){
		this.player = player;
		this.spellData = new HashMap<>();
	}

	/** Called from preInit in the main mod class to register the WizardData capability. */
	public static void preInit(){

		// Yes - by the looks of it, having an interface is completely unnecessary in this case.
		CapabilityManager.INSTANCE.register(RPGData.class, new IStorage<RPGData>(){
			// These methods are only called by Capability.writeNBT() or Capability.readNBT(), which in turn are
			// NEVER CALLED. Unless I'm missing some reflective invocation, that means this entire class serves only
			// to allow capabilities to be saved and loaded manually. What that would be useful for I don't know.
			// (If an API forces most users to write redundant code for no reason, it's not user friendly, is it?)
			// ... well, that's my rant for today!
			@Override
			public NBTBase writeNBT(Capability<RPGData> capability, RPGData instance, EnumFacing side){
				return null;
			}

			@Override
			public void readNBT(Capability<RPGData> capability, RPGData instance, EnumFacing side, NBTBase nbt){}

		}, RPGData::new);
	}

	/** Returns the WizardData instance for the specified player. */
	public static RPGData get(EntityPlayer player){
		return player.getCapability(RPG_DATA_CAPABILITY, null);
	}

	// ============================================= Variable Storage =============================================

	// This is my answer to having spells define their own player variables. It's not the prettiest system ever, but
	// I think the ability to add arbitrary data to this class and have it save itself to NBT automatically is pretty
	// powerful. If it doesn't need saving, this can even be done on the fly - no registration necessary.

	// The reason we have interfaces here is to allow custom implementations of the NBT read/write methods, for
	// example, reading/writing multiple keys without having to wrap them in an NBTTagCompound.

	/** Registers the given {@link IStoredVariable} objects as keys that will be stored to NBT for each {@code WizardData}
	 * instance. */
	public static void registerStoredVariables(IStoredVariable<?>... variables){
		storedVariables.addAll(Arrays.asList(variables));
	}

	/** Returns a set containing the registered {@link IStoredVariable} objects for which {@link IVariable#isSynced()}
	 * returns true. Used internally for packet reading. */
	public static Set<IVariable> getSyncedVariables(){
		return storedVariables.stream().filter(IVariable::isSynced).collect(Collectors.toSet());
	}

	/**
	 * Stores the given value under the given key in this {@code WizardData} object.
	 * @param variable The key under which the value is to be stored. See {@link IVariable} for more details.
	 * @param value The value to be stored.
	 * @param <T> The type of the value to be stored. Note that the given variable (key) may be of a supertype of the
	 *           stored value itself; however, when the value is retrieved its type will match that of the key. In
	 *           other words, if an {@code Integer} is stored under a {@code IVariable<Number>}, a {@code Number} will
	 *           be returned when the value is retrieved.
	 */
	// This use of type parameters guarantees that data may only be stored (and therefore may only be accessed)
	// using a compatible key. For instance, the following code will not compile:
	// Number i = 1;
	// setVariable(StoredVariable.ofInt("key", Persistence.ALWAYS), i);
	public <T> void setVariable(IVariable<? super T> variable, T value){
		this.spellData.put(variable, value);
	}

	/**
	 * Returns the value stored under the given key in this {@code WizardData} object, or null if the key was not
	 * stored.
	 * @param variable The key whose associated value is to be returned.
	 * @param <T> The type of the returned value.
	 * @return The value associated with the given key, or null no such key was stored. <i>Beware of auto-unboxing
	 * of primitive types! Directly assigning the result to a primitive type, as in {@code int i = getVariable(...)},
	 * will cause a {@link NullPointerException} if the key was not stored.</i>
	 */
	@SuppressWarnings("unchecked") // The spellData map is fully encapsulated so we can be sure that the cast is safe
	@Nullable
	public <T> T getVariable(IVariable<T> variable){
		return (T)spellData.get(variable);
	}

	public <T> T getOrDefault(IVariable<T> variable, T value){
		return getVariable(variable) != null ? getVariable(variable) : value;
	}
	// ============================================== Data Handling ==============================================

	/** Called each time the associated player is updated. */
	@SuppressWarnings("unchecked") // Again, we know it must be ok
	private void update(){
		this.spellData.forEach((k, v) -> this.spellData.put(k, k.update(player, v)));
		this.spellData.keySet().removeIf(k -> k.canPurge(player, this.spellData.get(k)));
	}

	/**
	 * Called from the event handler each time the associated player entity is cloned, i.e. on respawn or when
	 * travelling to a different dimension. Used to copy over any data that should persist over player death. This
	 * is the inverse of the old onPlayerDeath method, which reset the data that shouldn't persist.
	 * 
	 * @param data The old WizardData whose data is to be copied over.
	 * @param respawn True if the player died and is respawning, false if they are just travelling between dimensions.
	 */
	public void copyFrom(RPGData data, boolean respawn){
		for(IVariable variable : data.spellData.keySet()){
			if(variable.isPersistent(respawn)) this.spellData.put(variable, data.spellData.get(variable));
		}
	}

	/** Sends a packet to this player's client to synchronise necessary information. Only called server side. */
	public void sync(){
		if(this.player instanceof EntityPlayerMP){
			IMessage msg = new PacketPlayerSync.Message(this.spellData);
			ZettaiPacketHandler.net.sendTo(msg, (EntityPlayerMP)this.player);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public NBTTagCompound serializeNBT(){
		NBTTagCompound properties = new NBTTagCompound();
		storedVariables.forEach(k -> k.write(properties, this.spellData.get(k)));

		return properties;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt){

		if(nbt != null){
			try{
				storedVariables.forEach(k -> this.spellData.put(k, k.read(nbt)));
			}catch(ClassCastException e){
				// Should only happen if someone manually edits the save file
				ZettaiRPG.log.warn("Wizard data NBT tag was not of expected type!", e);
			}
		}
	}

	// ============================================== Event Handlers ==============================================

	@SubscribeEvent
	// The type parameter here has to be Entity, not EntityPlayer, or the event won't get fired.
	public static void onCapabilityLoad(AttachCapabilitiesEvent<Entity> event){

		if(event.getObject() instanceof EntityPlayer)
			event.addCapability(new ResourceLocation(ZettaiRPG.MODID, "RPGData"),
					new RPGData.Provider((EntityPlayer)event.getObject()));
	}

	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event){

		RPGData newData = RPGData.get(event.getEntityPlayer());
		RPGData oldData = RPGData.get(event.getOriginal());

		newData.copyFrom(oldData, event.isWasDeath());

		newData.sync(); // In theory this should fix client/server discrepancies (see #69)
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event){
		if(!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayerMP){
			// Synchronises wizard data after loading.
			RPGData data = RPGData.get((EntityPlayer)event.getEntity());
			if(data != null) data.sync();
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		if(event.getEntityLiving() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getEntityLiving();

			if(RPGData.get(player) != null){
				RPGData.get(player).update();
			}
		}
	}

	// ========================================== Capability Boilerplate ==========================================

	/**
	 * This is a nested class for a few reasons: firstly, it makes sense because instances of this and WizardData go
	 * hand-in-hand; secondly, it's too short to be worth a separate file; and thirdly (and most importantly) it allows
	 * me to access WIZARD_DATA_CAPABILITY while keeping it private.
	 */
	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		private final RPGData data;

		public Provider(EntityPlayer player){
			data = new RPGData(player);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing){
			return capability == RPG_DATA_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing){

			if(capability == RPG_DATA_CAPABILITY){
				return RPG_DATA_CAPABILITY.cast(data);
			}

			return null;
		}

		@Override
		public NBTTagCompound serializeNBT(){
			return data.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt){
			data.deserializeNBT(nbt);
		}

	}

}
