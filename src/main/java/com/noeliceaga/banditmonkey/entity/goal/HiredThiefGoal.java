package com.noeliceaga.banditmonkey.entity.goal;

import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class HiredThiefGoal extends Goal {

    private final BanditMonkeyEntity monkey;
    private Player target;
    private int cooldown;

    public HiredThiefGoal(BanditMonkeyEntity monkey) {
        this.monkey = monkey;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!monkey.isTame() || monkey.isOrderedToSit()) return false;
        UUID targetUUID = monkey.getStealTargetUUID();
        if (targetUUID == null) return false;
        if (cooldown > 0) { cooldown--; return false; }
        if (!(monkey.level() instanceof ServerLevel serverLevel)) return false;
        Entity entity = serverLevel.getEntity(targetUUID);
        if (!(entity instanceof Player player) || !player.isAlive()) {
            monkey.setStealTargetUUID(null);
            return false;
        }
        target = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && monkey.getStealTargetUUID() != null;
    }

    @Override
    public void stop() {
        target = null;
        monkey.getNavigation().stop();
    }

    @Override
    public void tick() {
        monkey.getLookControl().setLookAt(target, 30f, 30f);
        monkey.getNavigation().moveTo(target, 1.3);

        if (monkey.distanceTo(target) <= 2.2f) {
            stealFromPlayer(target);
            monkey.setStealTargetUUID(null);
            cooldown = 200;
        }
    }

    private void stealFromPlayer(Player player) {
        List<ItemStack> wishList = monkey.getWishListItems();
        Inventory inv = player.getInventory();

        // Try wish list first
        if (!wishList.isEmpty()) {
            for (ItemStack wish : wishList) {
                if (wish.isEmpty()) continue;
                for (int i = 0; i < 36; i++) {
                    ItemStack slot = inv.getItem(i);
                    if (!slot.isEmpty() && slot.getItem() == wish.getItem()) {
                        deliver(slot.copyWithCount(1), player.getName().getString());
                        slot.shrink(1);
                        return;
                    }
                }
            }
        }

        // Fallback: steal any item
        for (int i = 0; i < 36; i++) {
            ItemStack slot = inv.getItem(i);
            if (!slot.isEmpty()) {
                deliver(slot.copyWithCount(1), player.getName().getString());
                slot.shrink(1);
                return;
            }
        }

        notifyFailed(player.getName().getString());
    }

    private void deliver(ItemStack stolen, String victimName) {
        LivingEntity owner = monkey.getOwner();
        if (!(owner instanceof Player ownerPlayer)) return;
        if (!ownerPlayer.getInventory().add(stolen)) {
            if (monkey.level() instanceof ServerLevel serverLevel) {
                monkey.spawnAtLocation(serverLevel, stolen);
            }
        }
        ownerPlayer.displayClientMessage(Component.translatable(
                "message.banditmonkey.hired_stolen", stolen.getDisplayName(),
                Component.literal(victimName)), false);
    }

    private void notifyFailed(String victimName) {
        LivingEntity owner = monkey.getOwner();
        if (owner instanceof Player ownerPlayer) {
            ownerPlayer.displayClientMessage(Component.translatable(
                    "message.banditmonkey.steal_failed", Component.literal(victimName)), false);
        }
    }
}
