package com.wumple.util.capability.eventtimed;

import java.util.ArrayList;
import java.util.List;

import com.wumple.util.adapter.IThing;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

abstract public class ThingTimerEventHandler<W extends IThing, T extends IEventTimedThingCap<W, ? extends Expiration> > extends ThingTimerHandler<W, T>
{
    /*
    @SubscribeEvent
    abstract public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event);
    */
    
    abstract protected long getEvaluationInterval();
    abstract public boolean isEnabled();
    abstract public boolean isDebugging();

    public ThingTimerEventHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        evaluateTimer(event.getWorld(), event.getEntity());
    }

    @SubscribeEvent
    public void onPlaceBlock(BlockEvent.EntityPlaceEvent event)
    {    
        evaluateTimer(event.getWorld().getWorld(), event.getPos());
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
            evaluateTimer(event.getPlayer().getEntityWorld(), event.getPos());
        }
    }

    // might duplicate onPlayerInteract - remove if so
    @SubscribeEvent
    public void onEntityInteract(EntityInteract event)
    {
        // think it is safe to rot even if (event.isCanceled())
        evaluateTimer(event.getPlayer().getEntityWorld(), event.getTarget());
    }

    // likely duplicates onPlayerInteract - remove if so
    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent.Open event)
    {
        // think it is safe to rot even if (event.isCanceled())
        evaluateTimerContents(event.getPlayer().getEntityWorld(), event.getContainer());
        // ContainerWrapper isn't working:
        //ContainerWrapper wrapper = new ContainerWrapper(event.getContainer());
        //evaluateTimerContents(event.getEntityPlayer().world, wrapper);
        //wrapper.detectAndSendChanges();
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event)
    {
    	LivingEntity entity = event.getEntityLiving();

        if (entity.ticksExisted % getEvaluationInterval() == 0)
        {
            evaluateTimer(event.getEntityLiving().world, entity);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        LazyOptional<? extends T> lrot = (stack != null) ? getCap(stack) : LazyOptional.empty();

        lrot.ifPresent(rot->
        {
            rot.doTooltip(stack, event.getPlayer(), event.getFlags().isAdvanced(), event.getToolTip());
        });
    }
    
    @SubscribeEvent
    public void onItemPickedUp(EntityItemPickupEvent event)
    {
        evaluateTimer(event.getEntity().getEntityWorld(), event.getItem());
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerChangedDimensionEvent event)
    {
        dimensionShift(event.getPlayer(), event.getFrom().getId(), event.getTo().getId());
    }

    // PlayerChangedDimensionEvent does not fire when traveling from end!
    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.isEndConquered())
        {
            int fromDim = 1; // the End
            int toDim = event.getPlayer().world.getDimension().getType().getId();
            dimensionShift(event.getPlayer(), fromDim, toDim);
        }
    }
    
    /**
     *  Prevents exploit of making foods with almost rotten food to prolong total life of food supplies
     */
    @SubscribeEvent
    public void onCrafted(ItemCraftedEvent event) 
    {
        if (!isEnabled() || event.getPlayer().getEntityWorld().isRemote()|| event.getCrafting() == null
                || event.getCrafting().isEmpty() || event.getCrafting().getItem() == null)
        {
            return;
        }
        
        ItemStack crafting = event.getCrafting();
        IInventory craftMatrix = event.getInventory();
        World world = event.getPlayer().getEntityWorld();

        LazyOptional<? extends T> lccap = getCap(crafting);

        lccap.ifPresent(ccap->
        {
            ccap.handleCraftedTimers(world, craftMatrix, crafting);
        }); // else crafted item doesn't rot
    }
    
    /*
     * Draw debug screen extras
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.gameSettings.showDebugInfo == true)
        {
            if (isDebugging())
            {
                addTileEntityDebug(e);
            }
        }
    }
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
     */
    @OnlyIn(Dist.CLIENT)
    public void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();
        
        // tile entity
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
        {
            final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
            BlockPos blockpos = rayTraceResult.getPos();

            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
            LazyOptional<? extends T> lrot = getCap(te);
            lrot.ifPresent(rot->
            {
                List<ITextComponent> tips = new ArrayList<ITextComponent>();
                rot.doTooltip(null, mc.player, true, tips);
                
                for (ITextComponent tip : tips)
                {
                    e.getRight().add(tip.getFormattedText());
                }
            });
        }
    }

}
