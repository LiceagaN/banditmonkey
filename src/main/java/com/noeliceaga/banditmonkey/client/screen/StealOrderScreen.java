package com.noeliceaga.banditmonkey.client.screen;

import com.noeliceaga.banditmonkey.menu.StealOrderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class StealOrderScreen extends AbstractContainerScreen<StealOrderMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    // Set true when the player clicks "Set Target" — next entity right-click becomes the steal target
    public static volatile boolean targetingMode = false;

    public StealOrderScreen(StealOrderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Match a 1-row chest: 17px header + 18px row + 96px player section
        this.imageHeight = 131;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
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
                .bounds(guiLeft + 100, guiTop + 4, 68, 12)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Top section (title bar + 1 row of slots): first 35px of texture
        graphics.blit(TEXTURE, x, y, this.imageWidth, 35, 0.0f, 0.0f, (float) this.imageWidth, 35.0f);
        // Bottom section (player inventory): texture offset y=126
        graphics.blit(TEXTURE, x, y + 35, this.imageWidth, 96, 0.0f, 126.0f, (float) this.imageWidth, 96.0f);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        graphics.drawString(this.font,
                Component.translatable("label.banditmonkey.wish_list"),
                8, 8, 0x808080, false);
    }
}
