package zettasword.zettai_rpg.variables;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import zettasword.zettai_rpg.cap.RPGData;

public class StatsKeyHandler {

    public static KeyBinding statsKey;

    public static void registerKeyBindings() {
        statsKey = new KeyBinding("key.stats", Keyboard.KEY_P, "key.categories.zettai_rpg");
        ClientRegistry.registerKeyBinding(statsKey);
    }

    public static void handleKeyPress(RPGData playerStats) {
        if (statsKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new StatsGui(playerStats));
        }
    }
}