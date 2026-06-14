package com.noeliceaga.banditmonkey.entity.goal;

import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class StealItemGoal extends Goal {

    private static final int STEAL_COOLDOWN_TICKS = 300;
    private static final Set<Item> SHINY_ITEMS = Set.of(
            Items.GOLD_INGOT, Items.GOLD_NUGGET, Items.GOLD_BLOCK,
            Items.DIAMOND, Items.DIAMOND_BLOCK,
            Items.EMERALD, Items.EMERALD_BLOCK,
            Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.DIAMOND_SWORD, Items.GOLDEN_SWORD,
            Items.NETHERITE_INGOT, Items.AMETHYST_SHARD
    );

    private final BanditMonkeyEntity monkey;
    private final double detectionRange;
    private final double stealRangeSq;
    private Player target;
    private int cooldown;

    public StealItemGoal(BanditMonkeyEntity monkey, double detectionRange, double stealRange) {
        this.monkey = monkey;
        this.detectionRange = detectionRange;
        this.stealRangeSq = stealRange * stealRange;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (monkey.isTame()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        target = monkey.level().getNearestPlayer(monkey, detectionRange);
        return target != null && !target.isCreative() && !target.isSpectator() && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !monkey.isTame()
                && monkey.distanceToSqr(target) < detectionRange * detectionRange;
    }

    @Override
    public void start() {
        monkey.getNavigation().moveTo(target, 1.5);
    }

    @Override
    public void tick() {
        monkey.getLookControl().setLookAt(target);

        if (monkey.distanceToSqr(target) <= stealRangeSq) {
            stealFrom(target);
            stop();
        } else {
            monkey.getNavigation().moveTo(target, 1.5);
        }
    }

    @Override
    public void stop() {
        target = null;
        monkey.getNavigation().stop();
        cooldown = STEAL_COOLDOWN_TICKS;
    }

    private void stealFrom(Player player) {
        if (monkey.level().isClientSide()) return;

        Inventory inventory = player.getInventory();
        int slot = findShinySlot(inventory);
        if (slot == -1) slot = findRandomItemSlot(inventory);
        if (slot == -1) return;

        ItemStack stack = inventory.getItem(slot);
        ItemStack stolen = stack.copyWithCount(1);
        stack.shrink(1);

        monkey.level().addFreshEntity(
                new ItemEntity(monkey.level(), monkey.getX(), monkey.getY(), monkey.getZ(), stolen)
        );

        player.displayClientMessage(
                Component.translatable("message.banditmonkey.stolen", stolen.getHoverName()),
                true
        );
    }

    private int findShinySlot(Inventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && SHINY_ITEMS.contains(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private int findRandomItemSlot(Inventory inventory) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (!inventory.getItem(i).isEmpty()) {
                slots.add(i);
            }
        }
        if (slots.isEmpty()) return -1;
        return slots.get(monkey.getRandom().nextInt(slots.size()));
    }
}