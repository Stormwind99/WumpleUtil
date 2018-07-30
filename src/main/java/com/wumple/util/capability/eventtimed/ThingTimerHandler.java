package com.wumple.util.capability.eventtimed;

import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.ItemStackThing;
import com.wumple.util.capability.CapabilityUtils;
import com.wumple.util.container.Walker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
        return getCap((W)new ItemStackThing(stack));
    }
    
    public boolean evaluateTimer(World world, W thing)
    {
        ItemStack stack = thing.as(ItemStack.class);
        Entity entity = thing.as(Entity.class);
        TileEntity tileentity = thing.as(TileEntity.class);
        
        if (stack != null)
        {
            ItemStack newStack = evaluateTimer(world, stack);
            return (newStack != stack);
        }
        else if (tileentity != null)
        {
            return evaluateTimer(world, tileentity);
        }
        else if (entity != null)
        {
            return evaluateTimer(world, entity);
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public ItemStack evaluateTimer(World world, ItemStack stack)
    {
        W thing = (W)new ItemStackThing(stack);
        T cap = getCap(thing);
        
        W newThing = (cap != null) ? cap.evaluate(world, thing) : null;

        return (newThing != null) ? newThing.as(ItemStack.class)  : stack;
    }
    
    public boolean evaluateTimer(World world, Entity entity)
    {
        if (world.isRemote || !isEnabled())
        {
            return false;
        }
        
        // TODO evaluate self after contents?

        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            // check open container so user sees updates in open container
            // container listener would not handle this
            if ((player.openContainer != null) && !(player.openContainer instanceof ContainerPlayer))
            {
                evaluateTimerContents(world, player.openContainer);
            }
        }

        IItemHandler capability = CapabilityUtils.fetchCapability(entity, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (capability != null)
        {
            evaluateTimerContents(world, capability);
        }
        else if (entity instanceof EntityItem)
        {
            EntityItem item = (EntityItem) entity;

            ItemStack timerStack = evaluateTimer(world, item.getItem());

            if (item.getItem() != timerStack)
            {
                item.setItem(timerStack);
                return true;
            }
        }
        else if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            IInventory invo = player.inventory;
            evaluateTimerContents(world, invo);
        }
        else if (entity instanceof IInventory)
        {
            IInventory invo = (IInventory) entity;
            evaluateTimerContents(world, invo);
        }

        return false;
    }

    public boolean  evaluateTimer(World world, TileEntity tile)
    {
        if ((world.isRemote) || !isEnabled())
        {
            return false;
        }
        
        // TODO evaluate self after contents?

        IItemHandler capability = CapabilityUtils.fetchCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (capability != null)
        {
            evaluateTimerContents(world, capability);
        }
        else if (tile instanceof IInventory)
        {
            IInventory invo = (IInventory) tile; // as(tile, IInventory.class);

            evaluateTimerContents(world, invo);
        }
        
        return false;
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

    @SuppressWarnings("unchecked")
    public void evaluateTimer(World world, T cap, Integer index, IItemHandler itemhandler, ItemStack stack)
    {
        if (cap != null) { cap.evaluate(world, index, itemhandler, (W)new ItemStackThing(stack)); }
    }
    
    // ----------------------------------------------------------------------
    // Internal
    
    protected void evaluateTimerContents(World world, IItemHandler inventory)
    {
        Walker.walkContainer(inventory, (index, itemhandler, stack) -> {
            evaluateTimer(world, getCap(stack), index, itemhandler, stack);
        });
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

