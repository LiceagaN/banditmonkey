package com.noeliceaga.banditmonkey.client.screen;

import com.noeliceaga.banditmonkey.menu.StealOrderMenu;
import com.noeliceaga.banditmonkey.network.SetStealTargetPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class StealOrderScreen extends AbstractContainerScreen<StealOrderMenu> {

    private static final int LEFT_PANEL_W   = 176;
    private static final int SIDE_PANEL_GAP = 4;
    private static final int SIDE_PANEL_W   = 60;

    private static final int LIST_AREA_Y  = 48;   // top of list container (panel-relative)
    private static final int ENTRY_H      = 12;
    private static final int ENTRY_GAP    = 2;
    private static final int ENTRY_STEP   = ENTRY_H + ENTRY_GAP; // 14
    private static final int MAX_VISIBLE  = 5;

    private UUID selectedPlayerUUID = null;
    private int scrollOffset = 0;

    public StealOrderScreen(StealOrderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = LEFT_PANEL_W + SIDE_PANEL_GAP + SIDE_PANEL_W;
        this.imageHeight = 128;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int gx = (this.width  - this.imageWidth)  / 2;
        int gy = (this.height - this.imageHeight) / 2;

        // ── Left panel ────────────────────────────────────────────────────────
        graphics.fill(gx, gy, gx + LEFT_PANEL_W, gy + imageHeight, 0xFFC6C6C6);
        graphics.fill(gx,                    gy,                   gx + LEFT_PANEL_W,     gy + 1,              0xFF555555);
        graphics.fill(gx,                    gy,                   gx + 1,                gy + imageHeight,    0xFF555555);
        graphics.fill(gx + LEFT_PANEL_W - 1, gy,                   gx + LEFT_PANEL_W,     gy + imageHeight,    0xFF555555);
        graphics.fill(gx,                    gy + imageHeight - 1, gx + LEFT_PANEL_W,     gy + imageHeight,    0xFF555555);

        // Slot backgrounds — wish list row (y=18)
        for (Slot slot : this.menu.slots) {
            int sx = gx + slot.x;
            int sy = gy + slot.y;
            graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
            graphics.fill(sx,     sy,     sx + 16, sy + 16, 0xFF8B8B8B);
        }

        // Divider below wish list
        graphics.fill(gx + 7, gy + 35, gx + LEFT_PANEL_W - 7, gy + 36, 0xFF555555);

        // ── Player list (MC-style dark inset container) ───────────────────────
        List<PlayerInfo> players = getOtherPlayers();
        int lx = gx + 7;
        int ly = gy + LIST_AREA_Y;
        int lw = LEFT_PANEL_W - 14;
        int lh = MAX_VISIBLE * ENTRY_STEP + 2;  // 1px padding top + bottom

        // Sunken border (black outer edge, MC inset look)
        graphics.fill(lx - 1, ly - 1, lx + lw + 1, ly + lh + 1, 0xFF000000);
        // Dark interior background
        graphics.fill(lx, ly, lx + lw, ly + lh, 0xFF1A1A1A);

        int maxIdx = Math.min(scrollOffset + MAX_VISIBLE, players.size());
        for (int i = scrollOffset; i < maxIdx; i++) {
            PlayerInfo info = players.get(i);
            UUID uuid = info.getProfile().id();
            int entryY = ly + 1 + (i - scrollOffset) * ENTRY_STEP;

            boolean selected = uuid.equals(selectedPlayerUUID);
            boolean hovered  = mouseX >= lx && mouseX < lx + lw
                            && mouseY >= entryY && mouseY < entryY + ENTRY_H;

            int bg = selected ? 0xFF1E3A5F : (hovered ? 0xFF404040 : 0xFF2B2B2B);
            graphics.fill(lx, entryY, lx + lw, entryY + ENTRY_H, bg);

            // Top accent line for active row
            if (selected || hovered) {
                graphics.fill(lx, entryY, lx + lw, entryY + 1,
                        selected ? 0xFF4A90D9 : 0xFF555555);
            }

            // Separator between entries
            if (i < maxIdx - 1) {
                graphics.fill(lx, entryY + ENTRY_H, lx + lw, entryY + ENTRY_H + 1, 0xFF111111);
            }

            int textColor = selected ? 0xFF88CCFF : (hovered ? 0xFFFFFFFF : 0xFFBBBBBB);
            graphics.drawString(this.font, info.getProfile().name(), lx + 5, entryY + 2, textColor, false);
        }

        if (players.isEmpty()) {
            int msgW = this.font.width("No other players online");
            graphics.drawString(this.font, "No other players online",
                    lx + (lw - msgW) / 2, ly + lh / 2 - 4, 0xFF666666, false);
        }

        // Scroll arrows ▲ / ▼
        if (scrollOffset > 0) {
            graphics.drawString(this.font, "▲", lx + lw - 9, ly + 1, 0xFFAAAAAA, false);
        }
        if (players.size() > scrollOffset + MAX_VISIBLE) {
            graphics.drawString(this.font, "▼", lx + lw - 9, ly + lh - 9, 0xFFAAAAAA, false);
        }

        // ── Right panel (monkey preview) ──────────────────────────────────────
        int px = gx + LEFT_PANEL_W + SIDE_PANEL_GAP;
        graphics.fill(px, gy, px + SIDE_PANEL_W, gy + imageHeight, 0xFFC6C6C6);
        graphics.fill(px,                    gy,                   px + SIDE_PANEL_W,     gy + 1,              0xFF555555);
        graphics.fill(px,                    gy,                   px + 1,                gy + imageHeight,    0xFF555555);
        graphics.fill(px + SIDE_PANEL_W - 1, gy,                   px + SIDE_PANEL_W,     gy + imageHeight,    0xFF555555);
        graphics.fill(px,                    gy + imageHeight - 1, px + SIDE_PANEL_W,     gy + imageHeight,    0xFF555555);

        LivingEntity monkey = getMonkeyEntity();
        if (monkey != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    px + 1, gy + 1, px + SIDE_PANEL_W - 1, gy + imageHeight - 1,
                    22, 0.0f, mouseX, mouseY, monkey);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, "Target player:", 8, LIST_AREA_Y - 10, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            int gx = (this.width  - this.imageWidth)  / 2;
            int gy = (this.height - this.imageHeight) / 2;
            int lx = gx + 7;
            int lw = LEFT_PANEL_W - 14;
            int entryStartY = gy + LIST_AREA_Y + 1;

            List<PlayerInfo> players = getOtherPlayers();
            int maxIdx = Math.min(scrollOffset + MAX_VISIBLE, players.size());

            for (int i = scrollOffset; i < maxIdx; i++) {
                int entryY = entryStartY + (i - scrollOffset) * ENTRY_STEP;
                if (mouseX >= lx && mouseX < lx + lw
                        && mouseY >= entryY && mouseY < entryY + ENTRY_H) {
                    UUID uuid = players.get(i).getProfile().id();
                    selectedPlayerUUID = uuid;
                    var conn = Minecraft.getInstance().getConnection();
                    if (conn != null) {
                        conn.send(new ServerboundCustomPayloadPacket(new SetStealTargetPayload(uuid)));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int gx = (this.width  - this.imageWidth)  / 2;
        int gy = (this.height - this.imageHeight) / 2;
        int lx = gx + 7;
        int lw = LEFT_PANEL_W - 14;
        int lh = MAX_VISIBLE * ENTRY_STEP + 2;
        if (mouseX >= lx && mouseX < lx + lw
                && mouseY >= gy + LIST_AREA_Y && mouseY < gy + LIST_AREA_Y + lh) {
            int maxScroll = Math.max(0, getOtherPlayers().size() - MAX_VISIBLE);
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int) Math.signum(scrollY), maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private List<PlayerInfo> getOtherPlayers() {
        var mc   = Minecraft.getInstance();
        var conn = mc.getConnection();
        if (conn == null || mc.player == null) return List.of();
        UUID self = mc.player.getUUID();
        return conn.getListedOnlinePlayers().stream()
                .filter(p -> !p.getProfile().id().equals(self))
                .sorted(Comparator.comparing(p -> p.getProfile().name()))
                .toList();
    }

    private LivingEntity getMonkeyEntity() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var entity = level.getEntity(this.menu.entityId);
        return entity instanceof LivingEntity le ? le : null;
    }
}
