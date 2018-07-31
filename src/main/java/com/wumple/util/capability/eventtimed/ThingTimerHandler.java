package com.wumple.util.capability.eventtimed;

import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;
import com.wumple.util.container.Walker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

abstract public class ThingTimerHandler<W extends IThing, T extends IEventTimedThingCap<W, ? extends Expiration> >
{
    abstract protected T getCap(ICapabilityProvider stack);
    abstract protected boolean isEnabled();
    abstract public int getADimensionRatio(int dim);
    
    @SuppressWarnings("unchecked")
    protected T getCap(ItemStack stack)
    {
        return getCap((W)TUtil.to(stack));
    }
    
    public W evaluateTimer(World world, W thing)
    {
        evaluateTimerContents(world, thing);
        return evaluateTimerSelf(world, thing);
    }
    
    public W evaluateTimerSelf(World world, W thing)
    {
        T cap = getCap(thing);
        
        W newThing = (cap != null) ? cap.evaluate(world, thing) : null;

        return newThing;
    }
    
    public void evaluateTimerContents(World world, W thing)
    {
        // special case for players
        EntityPlayer player = thing.as(EntityPlayer.class);
        if (player != null)
        {
            // check open container so user sees updates in open container
            // container listener would not handle this
            if ((player.openContainer != null) && !(player.openContainer instanceof ContainerPlayer))
            {
                //evaluateTimerContents(world, new ContainerWrapper(player.openContainer));
                evaluateTimerContents(world, player.openContainer);
            }
        }

        IItemHandler capability = thing.fetchCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (capability != null)
        {
            evaluateTimerContents(world, capability);
        }
    }
    
    // ----------------------------------------------------------------------
    // Convenience methods
    
    @SuppressWarnings("unchecked")
    public boolean evaluateTimer(World world, BlockPos pos)
    {
        W thing = (W)TUtil.to(world.getTileEntity(pos));
        IThing newThing = evaluateTimer(world, thing);
        return (newThing != null);
    }
    
    @SuppressWarnings("unchecked")
    public ItemStack evaluateTimer(World world, ItemStack stack)
    {
        W thing = (W)TUtil.to(stack);
        W newThing = evaluateTimer(world, thing);
        return (newThing != null) ? newThing.as(ItemStack.class)  : stack;
    }
    
    @SuppressWarnings("unchecked")
    public boolean evaluateTimer(World world, Entity entity)
    {
        W thing = (W)TUtil.to(entity);
        W newThing = evaluateTimer(world, thing);
        return (newThing != null);
    }
    
    public void evaluateTimer(World world, Container container)
    {
        if ((world.isRemote) || !isEnabled())
        {
            return;
        }

        if (container instanceof IInventory)
        {
            IInventory invo = (IInventory) container;

            evaluateTimerContents(world, invo);
        }
        else
        {
            evaluateTimerContents(world, container);
        }
    }
    
    public void dimensionShift(EntityPlayer player, int fromDim, int toDim)
    {
        if ((player.getEntityWorld().isRemote) || !isEnabled())
        {
            return;
        }
        
        int fromDimensionRatio = getADimensionRatio(fromDim);
        int toDimensionRatio = getADimensionRatio(toDim);
        
        Walker.walkContainer(player, (index, handler, stack) -> {
            T cap = getCap(stack);

            if (cap != null)
            {
                cap.ratioShift(fromDimensionRatio, toDimensionRatio);
            }
        } );
    }
    
    // ----------------------------------------------------------------------
    // Internal
    
    protected void evaluateTimerContents(World world, IItemHandler inventory)
    {
        Walker.walkContainer(inventory, (index, itemhandler, stack) -> {
            evaluateTimer(world, getCap(stack), index, itemhandler, stack);
        });
    }
    
    @SuppressWarnings("unchecked")
    public void evaluateTimer(World world, T cap, Integer index, IItemHandler itemhandler, ItemStack stack)
    {
        if (cap != null) { cap.evaluate(world, index, itemhandler, (W)TUtil.to(stack)); }
    }
     
    protected void evaluateTimerContents(World world, Container inventory)
    {
        Walker.walkContainer(inventory, (index, container, stack) -> {
            ItemStack timerItem = evaluateTimer(world, stack);

            if (timerItem == null || timerItem.isEmpty() || (timerItem.getItem() != stack.getItem()))
            {
                if (timerItem == null)
                {
                    timerItem = ItemStack.EMPTY;
                }

                inventory.putStackInSlot(index, timerItem);
            }

        });

        inventory.detectAndSendChanges();
    }
    
    protected void evaluateTimerContents(World world, IInventory inventory)
    {
        boolean dirty = false;

        Walker.walkContainer(inventory, (index, container, stack) -> {
            // TODO timerItem == slotItem is true when slotItem is just updated (shouldn't
            //   happen unless init missed) and not expired
            ItemStack timerItem = evaluateTimer(world, stack);

            if (timerItem == null || timerItem.isEmpty() || (timerItem.getItem() != stack.getItem()))
            {
                if (timerItem == null)
                {
                    timerItem = ItemStack.EMPTY;
                }

                inventory.setInventorySlotContents(index, timerItem);
            }
        });

        if (dirty && inventory instanceof TileEntity)
        {
            ((TileEntity) inventory).markDirty();
        }
    }
}