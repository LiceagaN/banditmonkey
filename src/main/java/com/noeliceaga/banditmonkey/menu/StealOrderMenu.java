package com.noeliceaga.banditmonkey.menu;

import com.noeliceaga.banditmonkey.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StealOrderMenu extends AbstractContainerMenu {

    public static final int WISH_LIST_SIZE = 9;

    public final int entityId;

    // Server-side: wraps the entity's actual container
    public StealOrderMenu(int containerId, Inventory playerInventory, Container wishList, int entityId) {
        super(MenuRegistry.STEAL_ORDER.get(), containerId);
        this.entityId = entityId;
        checkContainerSize(wishList, WISH_LIST_SIZE);

        // Wish list row (y=18)
        for (int i = 0; i < WISH_LIST_SIZE; i++) {
            addSlot(new Slot(wishList, i, 8 + i * 18, 18));
        }

    }

    // Client-side: MC's slot sync will populate the empty container; reads entity ID from buf
    public StealOrderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(WISH_LIST_SIZE), buf.readInt());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
