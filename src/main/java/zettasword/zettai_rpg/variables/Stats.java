package zettasword.zettai_rpg.variables;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import zettasword.zettai_rpg.cap.IStoredVariable;
import zettasword.zettai_rpg.cap.Persistence;
import zettasword.zettai_rpg.cap.RPGData;

public class Stats {
    public static final IStoredVariable<Integer> STR = IStoredVariable.StoredVariable.ofInt("attack_damage", Persistence.ALWAYS).setSynced();
    public static final IStoredVariable<Integer> AGI = IStoredVariable.StoredVariable.ofInt("movement_speed", Persistence.ALWAYS).setSynced();
    public static final IStoredVariable<Integer> VIT = IStoredVariable.StoredVariable.ofInt("max_hp", Persistence.ALWAYS).setSynced();
    public static final IStoredVariable<Integer> INT = IStoredVariable.StoredVariable.ofInt("intelligence", Persistence.ALWAYS).setSynced();
    public static final IStoredVariable<Integer> MINING_SPEED = IStoredVariable.StoredVariable.ofInt("mining_speed", Persistence.ALWAYS).setSynced();
    public static final IStoredVariable<Integer> ARCHERY = IStoredVariable.StoredVariable.ofInt("archery", Persistence.ALWAYS).setSynced();

    /**
     * Called from {@link zettasword.zettai_rpg.ZettaiRPG#init(FMLInitializationEvent)}
     * Registers the player-specific WizardData attributes.
     */
    public static void init() {
        RPGData.registerStoredVariables(STR, AGI, VIT, INT, MINING_SPEED, ARCHERY);
    }
}
