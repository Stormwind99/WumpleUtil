package com.wumple.util.capability.eventtimed;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

abstract public class TimerEventHandler<T extends IEventTimedItemStackCap<? extends Expiration> > extends TimerHandler<T>
{
    /*
    @SubscribeEvent
    abstract public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event);
    */
    
    abstract protected long getEvaluationInterval();
    abstract public boolean isEnabled();
    abstract public boolean isDebugging();

    public TimerEventHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        evaluateTimer(event.getWorld(), event.getEntity());
    }

    @SubscribeEvent
    public void onEntityItemPickup(EntityItemPickupEvent event)
    {
        evaluateTimer(event.getEntity().getEntityWorld(), event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event instanceof RightClickBlock)
        {
            TileEntity tile = event.getEntityPlayer().world.getTileEntity(event.getPos());

            evaluateTimer(event.getEntityPlayer().world, tile);
        }
    }

    // might duplicate onPlayerInteract - remove if so
    @SubscribeEvent
    public void onEntityInteract(EntityInteract event)
    {
        // think it is safe to rot even if (event.isCanceled())
        evaluateTimer(event.getEntityPlayer().world, event.getTarget());
    }

    // likely duplicates onPlayerInteract - remove if so
    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent.Open event)
    {
        // think it is safe to rot even if (event.isCanceled())
        evaluateTimer(event.getEntityPlayer().world, event.getContainer());
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event)
    {
        EntityLivingBase entity = event.getEntityLiving();

        if (entity.ticksExisted % getEvaluationInterval() == 0)
        {
            evaluateTimer(event.getEntityLiving().world, entity);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        T rot = (stack != null) ? getCap(stack) : null;

        if (rot != null)
        {
            rot.doTooltip(stack, event.getEntityPlayer(), event.getFlags().isAdvanced(), event.getToolTip());
        }
    }
    
    @SubscribeEvent
    public void onItemPickedUp(EntityItemPickupEvent event)
    {
        evaluateTimer(event.getEntity().getEntityWorld(), event.getItem());
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerChangedDimensionEvent event)
    {
        dimensionShift(event.player, event.fromDim, event.toDim);
    }

    // PlayerChangedDimensionEvent does not fire when traveling from end!
    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.isEndConquered())
        {
            int fromDim = 1; // the End
            int toDim = event.player.world.provider.getDimension();
            dimensionShift(event.player, fromDim, toDim);
        }
    }
    
    /**
     *  Prevents exploit of making foods with almost rotten food to prolong total life of food supplies
     */
    @SubscribeEvent
    public void onCrafted(ItemCraftedEvent event) 
    {
        if (!isEnabled() || event.player.world.isRemote || event.crafting == null
                || event.crafting.isEmpty() || event.crafting.getItem() == null)
        {
            return;
        }
        
        ItemStack crafting = event.crafting;
        IInventory craftMatrix = event.craftMatrix;
        World world = event.player.world;

        T ccap = getCap(crafting);

        if (ccap != null)
        {
            ccap.handleCraftedTimers(world, craftMatrix, crafting);
        } // else crafted item doesn't rot
    }
}
