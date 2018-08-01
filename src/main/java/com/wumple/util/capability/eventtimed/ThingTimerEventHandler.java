package com.wumple.util.capability.eventtimed;

import java.util.ArrayList;
import java.util.List;

import com.wumple.util.adapter.IThing;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public void onPlaceBlock(BlockEvent.PlaceEvent event)
    {    
        evaluateTimer(event.getWorld(), event.getPos());
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
            evaluateTimer(event.getEntityPlayer().world, event.getPos());
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
        evaluateTimerContents(event.getEntityPlayer().world, event.getContainer());
        // ContainerWrapper isn't working:
        //ContainerWrapper wrapper = new ContainerWrapper(event.getContainer());
        //evaluateTimerContents(event.getEntityPlayer().world, wrapper);
        //wrapper.detectAndSendChanges();
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
    
    /*
     * Draw debug screen extras
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
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
    @SideOnly(Side.CLIENT)
    public void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        // tile entity
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null)
        {
            BlockPos blockpos = (mc.objectMouseOver == null) ? null : mc.objectMouseOver.getBlockPos();
            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
            T rot = getCap(te);
            if (rot != null)
            {
                List<String> tips = new ArrayList<String>();
                rot.doTooltip(null, mc.player, true, tips);
                
                for (String tip : tips)
                {
                    e.getRight().add(tip);
                }
            }
        }
    }

}
