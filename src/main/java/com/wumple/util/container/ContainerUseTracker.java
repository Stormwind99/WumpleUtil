package com.wumple.util.container;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.CapabilityUtils;
import com.wumple.util.misc.GuiUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/*
 * Horrible hack to have access to TileEntity AND Container used by PlayerEntity at once
 * during Tooltips, etc.
 * 
 * Only usable on client.  Server will have empty info since we don't track multiple players.
 */

@Mod.EventBusSubscriber
public class ContainerUseTracker
{
    public static TileEntity lastUsedTileEntity = null;
    public static Entity lastUsedEntity = null;
    public static Entity lastUsedBy = null;
    public static Container lastUsedContainer = null;
    // MAYBE lastIItemHandler
    // MAYBE lastIInventory

    public static void forget()
    {
        lastUsedTileEntity = null;
        lastUsedEntity = null;
        lastUsedBy = null;
        lastUsedContainer = null;
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        forget();
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event)
    {
        forget();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onInteract(PlayerInteractEvent event)
    {
        if (event.getSide() == LogicalSide.CLIENT)
        {
            if (event.getFace() != null)
            {
                BlockPos pos = event.getPos();
                TileEntity entity = event.getWorld().getTileEntity(pos);
                lastUsedTileEntity = entity;
                lastUsedEntity = null;
                if (entity != null)
                {
                    lastUsedBy = event.getEntity();
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onEntityInteract(EntityInteract event)
    {
        if (event.getSide() == LogicalSide.CLIENT)
        {
            Entity target = event.getTarget();
            if (target != null)
            {
                lastUsedEntity = target;
                lastUsedTileEntity = null;
                if (target != null)
                {
                    lastUsedBy = event.getEntity();
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onOpen(PlayerContainerEvent.Open event)
    {
        if (lastUsedBy == event.getEntity())
        {
            lastUsedContainer = event.getContainer();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onGuiOpen(GuiOpenEvent event)
    {
        Screen screen = event.getGui();
        if (screen instanceof ContainerScreen)
        {
        	ContainerScreen<?> gui = (ContainerScreen<?>) event.getGui();
            if (gui.getContainer() instanceof PlayerContainer)
            {
                lastUsedContainer = gui.getContainer();
                lastUsedBy = Minecraft.getInstance().player; // gui.inventorySlots.player is private
                lastUsedEntity = lastUsedBy;
            }
        }
        else if (screen == null)
        {
            forget();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClose(PlayerContainerEvent.Close event)
    {
        forget();
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static <T> LazyOptional<T> getContainerCapability(PlayerEntity entity, ItemStack stack, Capability<T> capability, @Nullable Direction facing)
    {
        LazyOptional<T> cap = LazyOptional.empty();

        IThing thing = ContainedByUtil.getContainedBy(stack, entity, null);
        if (thing != null)
        {
            cap = thing.getCapability(capability, facing);
        }

        if (lastUsedBy == entity)
        {
            if (GuiUtil.isOpenContainerSlotUnderMouse(stack))
            {
                // check Entities, such as for MinecartChest
                if (!cap.isPresent())
                {
                    // Client doesn't have container contents. so we don't check if
                    // ContainerUtil.doesContain(lastUsedEntity, stack)
                    cap = CapabilityUtils.fetchCapability(lastUsedEntity, capability, facing);
                }

                // check TileEntities, such as for Chest
                if (!cap.isPresent())
                {
                    // Client doesn't have container contents. so we don't check if
                    // ContainerUtil.doesContain(lastUsedTileEntity, stack)
                    cap = CapabilityUtils.fetchCapability(lastUsedTileEntity, capability, facing);
                }
            }
        }

        return cap;
    }
}

