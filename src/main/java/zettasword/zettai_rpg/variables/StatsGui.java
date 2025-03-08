package zettasword.zettai_rpg.variables;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import zettasword.zettai_rpg.ModConfig;
import zettasword.zettai_rpg.cap.RPGData;

import java.io.IOException;
import java.text.DecimalFormat;

public class StatsGui extends GuiScreen {

    private final RPGData stats;

    public StatsGui(RPGData playerStats) {
        this.stats = playerStats;
    }

    @Override
    public void initGui() {
        buttonList.clear(); // Clear any existing buttons
        // Add a "Close" button
        buttonList.add(new GuiButton(0, width / 2 - 100, height - 30, "Close"));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) { // Close button
            mc.displayGuiScreen(null); // Close the GUI
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Draw the default Minecraft background
        drawDefaultBackground();
        //drawTexturedModalRect(width / 2 - 128, height / 2 - 128, 0, 0, 256, 256);

        // Render the player doll using the new method
        drawEntityOnScreen(width / 4, height / 2, 30, (float) mouseX - ((float) width / 4), (float) (mouseY + 50) - ((float) height / 2), Minecraft.getMinecraft().player);

        DecimalFormat df = new DecimalFormat("#.##");

        // Draw stat values
        fontRenderer.drawString("Vitality: +" + df.format(calcProgress(stats.getOrDefault(Stats.VIT, 0), ModConfig.costVitality)), width / 2 - 100, height / 2 - 100, 0xFFFFFF);
        fontRenderer.drawString("Strength: +"
                        + df.format((((double) stats.getOrDefault(Stats.STR, 0) / ModConfig.costStrength)) * ModConfig.strAmplifier * 100),
                    width / 2 - 100, height / 2 - 80, 0xFFFFFF);

        fontRenderer.drawString("Agility: +"
                + df.format(
                        (((double) stats.getOrDefault(Stats.AGI, 0)
                                / ModConfig.costAgility) * ModConfig.agiAmplifier) * 100),
                width / 2 - 100, height / 2 - 60, 0xFFFFFF);

        fontRenderer.drawString("Intelligence: +" + df.format(calcProgress(stats.getOrDefault(Stats.INT, 0),ModConfig.costIntelligence)) + "%", width / 2 - 100, height / 2 - 40, 0xFFFFFF);
        fontRenderer.drawString("Skills:", width / 2 - 100, height / 2, 0xFFFFFF);
        fontRenderer.drawString("Mining: +" + df.format(calcProgress(stats.getOrDefault(Stats.MINING_SPEED, 0),ModConfig.costMiningSpeed)), width / 2 - 100, height / 2 + 20, 0xFFFFFF);
        fontRenderer.drawString("Archery: +" + df.format(calcProgress(stats.getOrDefault(Stats.ARCHERY, 0),ModConfig.costArchery)) + "%", width / 2 - 100, height / 2 + 40, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static double calcProgress(int currentExp, int levelCost) {
        // Calculate the percentage progress
        double percentageProgress = ((double) currentExp / levelCost) * 100;

        // Clamp the result higher than 0
        return Math.max(percentageProgress, 0);
    }

    public static double calc(int currentExp, int levelCost) {
        // Clamp the result between 0 and 100 (optional, depending on requirements)
        return ((double) currentExp / levelCost);
    }

    /**
     * Draws an entity on the screen.
     *
     * @param x         The X coordinate of the entity's position on the screen.
     * @param y         The Y coordinate of the entity's position on the screen.
     * @param size      The size of the entity.
     * @param mouseX    The horizontal mouse position relative to the entity.
     * @param mouseY    The vertical mouse position relative to the entity.
     * @param entity    The entity to render.
     */
    public static void drawEntityOnScreen(int x, int y, int size, float mouseX, float mouseY, EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 50.0F);
        GlStateManager.scale((float) (-size), (float) size, (float) size);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        // Save original rotation values
        float originalYawOffset = entity.renderYawOffset;
        float originalRotationYaw = entity.rotationYaw;
        float originalRotationPitch = entity.rotationPitch;
        float originalPrevRotationYawHead = entity.prevRotationYawHead;
        float originalRotationYawHead = entity.rotationYawHead;

        // Apply transformations for proper orientation
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        // Calculate pitch (vertical rotation) - Invert the Y-axis here
        float pitchAngle = ((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F; // Inverted sign for Y-axis
        GlStateManager.rotate(pitchAngle, 1.0F, 0.0F, 0.0F);

        // Calculate yaw (horizontal rotation)
        float yawAngle = -((float) Math.atan((double) (mouseX / 40.0F))) * 20.0F; // Negative sign ensures correct direction
        entity.renderYawOffset = yawAngle;
        entity.rotationYaw = yawAngle * 2; // Exaggerate yaw for better visibility
        entity.rotationPitch = pitchAngle; // Use the inverted pitch angle
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;

        // Render the entity
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        renderManager.setRenderShadow(false);
        renderManager.renderEntity(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, false);
        renderManager.setRenderShadow(true);

        // Restore original rotation values
        entity.renderYawOffset = originalYawOffset;
        entity.rotationYaw = originalRotationYaw;
        entity.rotationPitch = originalRotationPitch;
        entity.prevRotationYawHead = originalPrevRotationYawHead;
        entity.rotationYawHead = originalRotationYawHead;

        // Clean up OpenGL states
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}