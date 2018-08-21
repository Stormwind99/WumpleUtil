package com.wumple.util.capability.targetcrafting;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.targetcrafting.container.ContainerCrafting;
import com.wumple.util.capability.thing.IThingCap;
import com.wumple.util.tooltip.ITooltipProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IContainerCraftingOwner extends IInventory, INBTSerializable<NBTBase>, IThingCap<IThing>, ITooltipProvider, IInteractionObject
{
    void onCraftMatrixChanged(IInventory inventoryIn, @Nullable BiConsumer<Integer, ItemStack> updater);
    
    void onBlockBreak(World worldIn, BlockPos pos);

    void onRightBlockClicked(PlayerInteractEvent.RightClickBlock event);
    
    IItemHandlerModifiable handler();
 
    default public Container createContainer(InventoryPlayer inventory)
    {
        return createContainer(inventory, null);
    }

    default public Container createContainer(InventoryPlayer inventory, EntityPlayer playerIn)
    {
        return new ContainerCrafting(inventory, this);
    }
}
