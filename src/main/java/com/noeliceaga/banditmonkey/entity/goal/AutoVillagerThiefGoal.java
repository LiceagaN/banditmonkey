package com.noeliceaga.banditmonkey.entity.goal;

import com.noeliceaga.banditmonkey.entity.BanditMonkeyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AutoVillagerThiefGoal extends Goal {

    private static final int COOLDOWN_TICKS = 24000; // 1 Minecraft day
    private static final double RANGE = 100.0;

    private final BanditMonkeyEntity monkey;
    private Villager target;
    private int cooldown = 0;

    public AutoVillagerThiefGoal(BanditMonkeyEntity monkey) {
        this.monkey = monkey;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!monkey.isTame() || monkey.isOrderedToSit()) return false;
        if (cooldown > 0) { cooldown--; return false; }
        if (!(monkey.level() instanceof ServerLevel serverLevel)) return false;

        AABB box = monkey.getBoundingBox().inflate(RANGE);
        List<Villager> nearby = serverLevel.getEntitiesOfClass(Villager.class, box, Villager::isAlive);
        if (nearby.isEmpty()) return false;

        target = nearby.get(monkey.getRandom().nextInt(nearby.size()));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && cooldown == 0;
    }

    @Override
    public void stop() {
        monkey.getNavigation().stop();
        target = null;
    }

    @Override
    public void tick() {
        monkey.getLookControl().setLookAt(target, 30f, 30f);
        monkey.getNavigation().moveTo(target, 1.3);

        if (monkey.distanceTo(target) <= 2.2f) {
            stealFromVillager(target);
            target = null;
            cooldown = COOLDOWN_TICKS;
        }
    }

    private void stealFromVillager(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        List<ItemStack> candidates = new ArrayList<>();

        if (offers != null) {
            for (MerchantOffer offer : offers) {
                ItemStack result = offer.getResult();
                if (!result.isEmpty()) candidates.add(result);
            }
        }

        ItemStack stolen = candidates.isEmpty()
                ? new ItemStack(Items.EMERALD, 1 + monkey.getRandom().nextInt(3))
                : candidates.get(monkey.getRandom().nextInt(candidates.size())).copyWithCount(1);

        deliver(stolen, villager.getName().getString());
    }

    private void deliver(ItemStack stolen, String victimName) {
        LivingEntity owner = monkey.getOwner();
        if (!(owner instanceof Player ownerPlayer)) return;
        if (!ownerPlayer.getInventory().add(stolen)) {
            if (monkey.level() instanceof ServerLevel sl) monkey.spawnAtLocation(sl, stolen);
        }
        ownerPlayer.displayClientMessage(Component.translatable(
                "message.banditmonkey.hired_stolen", stolen.getDisplayName(),
                Component.literal(victimName)), false);
    }
}
