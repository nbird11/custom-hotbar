package dev.nbird.customhotbar.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HotbarMixin {
    // Using Minecraft's vanilla widgets texture (contains hotbar slots, selection indicator, etc.)
    private static final Identifier WIDGETS_TEXTURE = Identifier.of("minecraft", "textures/gui/widgets.png");
    
    // Constants for layout
    private static final int SLOT_SIZE = 20;  // Size of each slot
    
    @Shadow @Final private MinecraftClient client;
    
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (player == null) return;

        // Cancel vanilla hotbar rendering
        ci.cancel();

        // Get screen dimensions for default positioning
        int screenHeight = context.getScaledWindowHeight();
        int baseY = screenHeight - 20;

        // Default positions for each slot - will be configurable later
        int[][] slotPositions = {
            {10, baseY - 25*3},     // Slot 1
            {35, baseY - 25*3},     // Slot 2
            {60, baseY - 25*3},     // Slot 3
            {85, baseY - 25*3},     // Slot 4
            {85, baseY - 25*2},     // Slot R
            {85, baseY - 25*1},     // Slot F
            {110, baseY - 25*3},    // Slot 5
            {110, baseY - 25*2},    // Slot T
            {110, baseY - 25*1}     // Slot G
        };

        // Position for offhand slot
        int[] offhandPos = {85, baseY};  // Offhand Slot V

        // Get the currently selected slot
        int selectedSlot = player.getInventory().getSelectedSlot();

        // Render each slot individually
        for (int slot = 0; slot < 9; slot++) {
            int x = slotPositions[slot][0];
            int y = slotPositions[slot][1];
            
            // Draw slot background
            context.drawTexture(
                RenderLayer::getGuiTextured,
                WIDGETS_TEXTURE,
                x, y,
                0, 0,
                SLOT_SIZE, SLOT_SIZE,
                256, 256
            );
            
            // Draw item in slot
            ItemStack itemStack = player.getInventory().getStack(slot);
            if (!itemStack.isEmpty()) {
                context.drawItem(itemStack, x + 2, y + 2);
                context.drawStackOverlay(client.textRenderer, itemStack, x + 2, y + 2);
            }
            
            // Highlight selected slot
            if (slot == selectedSlot) {
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    WIDGETS_TEXTURE,
                    x - 1, y - 1,
                    0, 22,
                    24, 22,
                    256, 256
                );
            }
        }

        // Render offhand slot
        ItemStack offhandStack = player.getOffHandStack();
        if (!offhandStack.isEmpty()) {
            int x = offhandPos[0];
            int y = offhandPos[1];

            context.drawTexture(
                RenderLayer::getGuiTextured,
                WIDGETS_TEXTURE,
                x, y,
                0, 0,
                SLOT_SIZE, SLOT_SIZE,
                256, 256
            );

            context.drawItem(offhandStack, x + 2, y + 2);
            context.drawStackOverlay(client.textRenderer, offhandStack, x + 2, y + 2);
        }
    }
}