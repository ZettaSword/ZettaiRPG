package zettasword.zettai_rpg.events;

import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zettasword.zettai_rpg.ModConfig;
import zettasword.zettai_rpg.cap.RPGData;
import zettasword.zettai_rpg.variables.Stats;

import java.util.UUID;

@Mod.EventBusSubscriber
public class EventsHandler {
    public static final UUID uuid = UUID.fromString("5df9992e-2d53-4593-b00e-72524e9ec245");

    /**
     * The attribute modifier value is added onto the total value
     */
    public static final int ADD = 0;

    /**
     * The attribute modifier value is multiplied by the original base value then added onto the total value
     */
    public static final int ADD_MULTIPLE = 1;

    /**
     * The total value is multiplied by 1 + the attribute modifier value
     */
    public static final int MULTIPLY = 2;


    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event){
        if (event.phase == TickEvent.Phase.START){
            RPGData data = RPGData.get(event.player);
            if (data != null){
                EntityPlayer player = event.player;
                // Getting stats
                int agi = data.getOrDefault(Stats.AGI, 0);

                // Adding stats
                if (player.ticksExisted % 100 == 0) {
                    if (ModConfig.statAgility && player.isSprinting()) {
                        if (!player.world.isRemote){
                            data.setVariable(Stats.AGI, agi + 1);
                            data.sync();
                        }
                    }
                }

                // Making stats affect player

                AbstractAttributeMap map = player.getAttributeMap();
                if (ModConfig.statAgility) {
                    IAttributeInstance speed = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
                    IAttributeInstance swim = map.getAttributeInstance(EntityPlayer.SWIM_SPEED);
                    if (player.isSprinting()){
                        checkStatSafely(agi, speed, ModConfig.costAgility, ModConfig.agiAmplifier,"agility");
                        checkStatSafely(agi, swim, ModConfig.costAgility, ModConfig.agiAmplifier,"agility");
                    }else{
                        removeStatModifier(speed);
                        removeStatModifier(swim);
                    }

                    if (player.isSneaking() && player.onGround){
                        player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 5, (int) (((float) agi /ModConfig.costAgility) * ModConfig.agiAmplifier), false,false));
                    }
                }
                if (player.ticksExisted % 100 == 0) {
                    if (ModConfig.statStrength) {
                        IAttributeInstance attack = map.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
                        int str = data.getOrDefault(Stats.STR, 0);
                        checkStatSafely(str, attack, ModConfig.costStrength, ModConfig.strAmplifier, "strength", ADD);
                    }

                    if (ModConfig.statVitality) {
                        int hp = data.getOrDefault(Stats.VIT, 0);
                        IAttributeInstance health = map.getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
                        checkStatSafely(hp, health, ModConfig.costVitality, 1, "health", ADD);
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event){
        if (event.getSource().getTrueSource() instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
            RPGData data = RPGData.get(player);
            if (data != null) {
                if (!event.getSource().isProjectile() && !event.getSource().isMagicDamage() && ModConfig.statStrength){
                    if (!player.world.isRemote) {
                        int str = data.getOrDefault(Stats.STR, 0);
                        data.setVariable(Stats.STR, str + 1);
                        data.sync();
                    }
                }
                if (event.getSource().isMagicDamage()){
                    if (!player.world.isRemote && ModConfig.statIntelligence) {
                        int intelligence = data.getOrDefault(Stats.INT, 0);
                        data.setVariable(Stats.INT, intelligence + 1);
                        data.sync();
                    }
                    event.setAmount(event.getAmount() * (1.0F + (float) data.getOrDefault(Stats.INT, 0) / ModConfig.costIntelligence));
                }

                if (event.getSource().isProjectile() && !event.getSource().isMagicDamage() && ModConfig.statArchery){
                    if (!player.world.isRemote) {
                        int intelligence = data.getOrDefault(Stats.ARCHERY, 0);
                        data.setVariable(Stats.ARCHERY, intelligence + 1);
                        data.sync();
                    }
                    event.setAmount(event.getAmount() * (1.0F + (float) data.getOrDefault(Stats.ARCHERY, 0) / ModConfig.costArchery));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent event){
        if (event.getEntity() instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) event.getEntity();
            RPGData data = RPGData.get(player);
            if (data != null && event.getSource() != DamageSource.MAGIC && event.getSource() != DamageSource.IN_FIRE && event.getSource() != DamageSource.ON_FIRE){
                if (!player.world.isRemote) {
                    int hp = data.getOrDefault(Stats.VIT, 0);
                    data.setVariable(Stats.VIT, hp + 1);
                    data.sync();
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMineSpeed(PlayerEvent.BreakSpeed event){
        EntityPlayer player = event.getEntityPlayer();
        RPGData data = RPGData.get(player);
        if (data != null){
            float spd = (float) data.getOrDefault(Stats.MINING_SPEED, 0) / ModConfig.costMiningSpeed;
            event.setNewSpeed(event.getNewSpeed() * (1.0F + spd));
        }
    }

    @SubscribeEvent
    public static void onMine(BlockEvent.BreakEvent event){
        EntityPlayer player = event.getPlayer();
        RPGData data = RPGData.get(player);
        if (data != null && event.getState().getBlockHardness(player.world, event.getPos()) >= 1.0){
            int mineSpd = data.getOrDefault(Stats.MINING_SPEED, 0);
            data.setVariable(Stats.MINING_SPEED, mineSpd + 1);
            data.sync();
        }
    }

    private static void checkStatSafely(double stat, IAttributeInstance attributeInstance, double cost, float amplifier, String modifierName) {
        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, (stat / cost) * amplifier, MULTIPLY);
        if (!attributeInstance.hasModifier(modifier)) attributeInstance.applyModifier(modifier);
        else {
            attributeInstance.removeModifier(uuid); attributeInstance.applyModifier(modifier);}
    }

    private static void checkStatSafely(double stat, IAttributeInstance attributeInstance, double cost,float amplifier, String modifierName, int mode) {
        AttributeModifier modifier = new AttributeModifier(uuid, modifierName, (stat / cost) * amplifier * 100, mode);
        if (!attributeInstance.hasModifier(modifier)) attributeInstance.applyModifier(modifier);
        else {
            attributeInstance.removeModifier(uuid); attributeInstance.applyModifier(modifier);}
    }

    private static void removeStatModifier(IAttributeInstance attributeInstance) {
        if (attributeInstance.getModifier(uuid) != null) attributeInstance.removeModifier(uuid);
    }
}
