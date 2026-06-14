package com.noeliceaga.banditmonkey.entity;

import com.mojang.serialization.Codec;
import com.noeliceaga.banditmonkey.entity.goal.AutoVillagerThiefGoal;
import com.noeliceaga.banditmonkey.entity.goal.HiredThiefGoal;
import com.noeliceaga.banditmonkey.entity.goal.StealItemGoal;
import com.noeliceaga.banditmonkey.menu.StealOrderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanditMonkeyEntity extends TamableAnimal implements GeoEntity, MenuProvider {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.monkey.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.monkey.walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer stealWishList = new SimpleContainer(9);
    @Nullable private UUID stealTargetUUID = null;

    public BanditMonkeyEntity(EntityType<? extends BanditMonkeyEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new AutoVillagerThiefGoal(this));
        goalSelector.addGoal(3, new HiredThiefGoal(this));
        goalSelector.addGoal(4, new StealItemGoal(this, 12.0, 2.0));
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2, 6.0f, 2.0f));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (!isTame() && item.is(Items.APPLE)) {
            if (!player.getAbilities().instabuild) item.shrink(1);
            if (!level().isClientSide()) {
                tame(player);
                setOrderedToSit(true);
            }
            return InteractionResult.SUCCESS;
        }

        if (isOwnedBy(player)) {
            if (player.isShiftKeyDown()) {
                if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit());
            } else {
                if (!level().isClientSide() && player instanceof ServerPlayer sp) {
                    sp.openMenu(this, buf -> buf.writeInt(this.getId()));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.banditmonkey.steal_order");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StealOrderMenu(containerId, playerInventory, stealWishList, this.getId());
    }

    // ── Wish list / steal target ───────────────────────────────────────────────

    @Nullable
    public UUID getStealTargetUUID() {
        return stealTargetUUID;
    }

    public void setStealTargetUUID(@Nullable UUID uuid) {
        this.stealTargetUUID = uuid;
    }

    public List<ItemStack> getWishListItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < stealWishList.getContainerSize(); i++) {
            ItemStack stack = stealWishList.getItem(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
    }

    // ── NBT persistence ───────────────────────────────────────────────────────

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < stealWishList.getContainerSize(); i++) {
            items.add(stealWishList.getItem(i));
        }
        output.store("StealWishList", Codec.list(ItemStack.OPTIONAL_CODEC), items);
        if (stealTargetUUID != null) output.putString("StealTarget", stealTargetUUID.toString());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("StealWishList", Codec.list(ItemStack.OPTIONAL_CODEC)).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), stealWishList.getContainerSize()); i++) {
                stealWishList.setItem(i, list.get(i));
            }
        });
        input.getString("StealTarget").ifPresent(s -> stealTargetUUID = UUID.fromString(s));
    }

    // ── Breeding / GeckoLib ───────────────────────────────────────────────────

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 5, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }
}
