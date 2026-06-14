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

    // Server-side: wraps the entity's actual container
    public StealOrderMenu(int containerId, Inventory playerInventory, Container wishList) {
        super(MenuRegistry.STEAL_ORDER.get(), containerId);
        checkContainerSize(wishList, WISH_LIST_SIZE);

        // Wish list row — matches a single-row chest layout (y=18)
        for (int i = 0; i < WISH_LIST_SIZE; i++) {
            addSlot(new Slot(wishList, i, 8 + i * 18, 18));
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 46 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 104));
        }
    }

    // Client-side: MC's slot sync will populate the empty container
    public StealOrderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(WISH_LIST_SIZE));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (slotIndex < WISH_LIST_SIZE) {
                if (!moveItemStackTo(stack, WISH_LIST_SIZE, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, 0, WISH_LIST_SIZE, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
