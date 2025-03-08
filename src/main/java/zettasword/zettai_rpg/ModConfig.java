package zettasword.zettai_rpg;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(ZettaiRPG.MODID)
public class ModConfig {
    public static Configuration config;

    public static boolean statVitality;
    public static int costVitality;

    // Example configuration fields
    public static boolean statAgility;
    public static int costAgility;
    public static float agiAmplifier;

    public static boolean statStrength;
    public static int costStrength;
    public static float strAmplifier;

    public static boolean statIntelligence;
    public static int costIntelligence;
    public static float intAmplifier;

    // Skills
    public static boolean statMiningSpeed;
    public static int costMiningSpeed;

    public static boolean statArchery;
    public static int costArchery;


    // Method to load the configuration
    public static void preInit(FMLPreInitializationEvent event) {
        // Create a new Configuration instance using the suggested config file
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
    }

    // Method to synchronize the configuration
    public static void syncConfig() {
        try {
            // Load the configuration file
            config.load();

            statVitality = config.getBoolean("Vitality (HP Addition) is Enabled?", Configuration.CATEGORY_GENERAL, true, "Should we increase player HP with Vitality.");
            costVitality = config.getInt("Vitality Cost", Configuration.CATEGORY_GENERAL, 10000, 1, 20000, "Player gets 1 point each time player is damaged. By increasing the cost, you make it harder to have more Health. It's the cost of half-a-heart.");

            // Define your properties with default values and comments
            statAgility = config.getBoolean("Agility is Enabled?", Configuration.CATEGORY_GENERAL, true, "Should we speed up player with Agility stat.");
            costAgility = config.getInt("Agility Cost", Configuration.CATEGORY_GENERAL, 100, 1, 20000, "Player gets 1 point each 5 seconds if he is sprinting. By increasing the cost, you decrease how much gained points affect the player movement speed.");
            agiAmplifier = config.getFloat("Agility Amplifier", Configuration.CATEGORY_GENERAL, 0.1F, 0.1F, 20000F, "For each point player gets this much of speed to player!");


            statStrength = config.getBoolean("Strength is Enabled?", Configuration.CATEGORY_GENERAL, true, "Should we increase player melee damage with Strength stat.");
            costStrength = config.getInt("Strength Cost", Configuration.CATEGORY_GENERAL, 1000, 1, 20000, "Player gets 1 point each time he does damage. By increasing the cost, you decrease how much gained points affect player melee damage.");
            strAmplifier = config.getFloat("Strength Amplifier", Configuration.CATEGORY_GENERAL, 0.1F, 0.1F, 20000F, "For each point player gets this much of attack damage!");

            statIntelligence = config.getBoolean("Intelligence is Enabled?", Configuration.CATEGORY_GENERAL, true, "Should we increase player magic damage with Intelligence stat");
            costIntelligence = config.getInt("Intelligence Cost", Configuration.CATEGORY_GENERAL, 1000, 1, 20000, "Player gets 1 point each time he does magic damage. By increasing the cost, you decrease how much gained points affect player magic damage.");
            intAmplifier = config.getFloat("Intelligence Amplifier", Configuration.CATEGORY_GENERAL, 0.1F, 0.1F, 20000F, "For each point player gets this much of additional magic damage in percents!");

            statMiningSpeed = config.getBoolean("Skill: Mining Speed", Configuration.CATEGORY_GENERAL, true, "Should we increase player mining speed?");
            costMiningSpeed = config.getInt("Mining Speed Cost", Configuration.CATEGORY_GENERAL, 1000, 1, 20000, "Player gets 1 point for each block mined.");

            statArchery = config.getBoolean("Skill: Archery", Configuration.CATEGORY_GENERAL, true, "Should we increase player projectiles damage?");
            costArchery = config.getInt("Archery Cost", Configuration.CATEGORY_GENERAL, 1000, 1, 20000, "Player gets 1 point each time player shoots an projectile and it hits the target.");

        } catch (Exception e) {
            // Handle exceptions while loading the config
            ZettaiRPG.log.error("Zettai RPG: Failed to load config file:" + e);
        } finally {
            // Save the configuration if it has changed
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
