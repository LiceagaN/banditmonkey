package com.noeliceaga.banditmonkey.client.screen;

import com.noeliceaga.banditmonkey.menu.StealOrderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class StealOrderScreen extends AbstractContainerScreen<StealOrderMenu> {

    // Set true when "Set Target" is clicked — next entity right-click becomes the steal target
    public static volatile boolean targetingMode = false;

    public StealOrderScreen(StealOrderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // 17px header + 1 wish list row (18px) + 11px gap + 3 inventory rows (54px) + 4px gap + 1 hotbar row (18px) + 9px padding
        this.imageHeight = 131;
        this.inventoryLabelY = this.imageHeight - 94; // 37 — sits between wish list and inventory
    }

    @Override
    protected void init() {
        super.init();
        int gx = (this.width - this.imageWidth) / 2;
        int gy = (this.height - this.imageHeight) / 2;
        addRenderableWidget(Button.builder(
                Component.translatable("button.banditmonkey.set_target"),
                btn -> {
                    targetingMode = true;
                    this.onClose();
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                                Component.translatable("message.banditmonkey.select_target"), false);
                    }
                })
                .bounds(gx + 100, gy + 4, 68, 12)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int gx = (this.width - this.imageWidth) / 2;
        int gy = (this.height - this.imageHeight) / 2;

        // Main panel — vanilla gray
        graphics.fill(gx, gy, gx + imageWidth, gy + imageHeight, 0xFFC6C6C6);

        // Outer border
        graphics.fill(gx,              gy,              gx + imageWidth, gy + 1,             0xFF555555);
        graphics.fill(gx,              gy,              gx + 1,          gy + imageHeight,    0xFF555555);
        graphics.fill(gx + imageWidth - 1, gy,          gx + imageWidth, gy + imageHeight,    0xFF555555);
        graphics.fill(gx,              gy + imageHeight - 1, gx + imageWidth, gy + imageHeight, 0xFF555555);

        // Slot backgrounds (covers wish list row + player inventory + hotbar)
        for (Slot slot : this.menu.slots) {
            int sx = gx + slot.x;
            int sy = gy + slot.y;
            // Inset shadow (dark top-left)
            graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
            // Slot interior
            graphics.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
        }

        // Divider below wish list row
        graphics.fill(gx + 7, gy + 35, gx + imageWidth - 7, gy + 36, 0xFF555555);

        // Divider between inventory and hotbar
        graphics.fill(gx + 7, gy + 101, gx + imageWidth - 7, gy + 102, 0xFF555555);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title in header
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        // "Inventory" label above player slots
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
