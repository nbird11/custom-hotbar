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
    // Using offhand slot texture for all slots
    private static final Identifier SLOT_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar_offhand_left.png");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar_selection.png");
    
    // Constants for layout
    private static final int SLOT_SIZE = 24;  // Size of each slot and selection indicator
    
    @Shadow @Final private MinecraftClient client;
    
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (player == null) return;

        // Cancel vanilla hotbar rendering
        ci.cancel();

        // Get screen dimensions for default positioning
        int screenHeight = context.getScaledWindowHeight();
        int baseY = screenHeight - 23;
        int itemOffsetX = 3;
        int itemOffsetY = 4;

        // Default positions for each slot
        // TODO: make configurable later
        int[][] slotPositions = {
            {0, baseY - 25*2},      // Slot 1
            {25, baseY - 25*2},     // Slot 2
            {50, baseY - 25*2},     // Slot 3
            {75, baseY - 25*2},     // Slot 4
            {75, baseY - 25*1},     // Slot R
            {75, baseY - 25*0},     // Slot F
            {100, baseY - 25*2},    // Slot 5
            {100, baseY - 25*1},    // Slot T
            {100, baseY - 25*0}     // Slot G
        };
        int[] offhandPos = {25, baseY};  // Offhand Slot V

        // Get the currently selected slot
        int selectedSlot = player.getInventory().getSelectedSlot();

        // Render each slot individually
        for (int slot = 0; slot < 9; slot++) {
            int x = slotPositions[slot][0];
            int y = slotPositions[slot][1];
            
            // Draw slot background using offhand texture
            context.drawTexture(
                RenderLayer::getGuiTextured,
                SLOT_TEXTURE,
                x + 1, y,
                0, 0,
                SLOT_SIZE, SLOT_SIZE,
                29, 24  // Offhand texture dimensions
            );
            
            // Draw item in slot
            ItemStack itemStack = player.getInventory().getStack(slot);
            if (!itemStack.isEmpty()) {
                context.drawItem(itemStack, x + itemOffsetX, y + itemOffsetY);
                context.drawStackOverlay(client.textRenderer, itemStack, x + itemOffsetX, y + itemOffsetY);
            }
            
            // Highlight selected slot
            if (slot == selectedSlot) {
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    HOTBAR_SELECTION_TEXTURE,
                    x, y,
                    0, 0,
                    SLOT_SIZE, SLOT_SIZE,
                    24, 23  // Selection texture dimensions
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
                SLOT_TEXTURE,
                x, y,
                0, 0,
                SLOT_SIZE, SLOT_SIZE,
                29, 24  // Offhand texture dimensions
            );

            context.drawItem(offhandStack, x + itemOffsetX, y + itemOffsetY);
            context.drawStackOverlay(client.textRenderer, offhandStack, x + itemOffsetX, y + itemOffsetY);
        }
    }
}