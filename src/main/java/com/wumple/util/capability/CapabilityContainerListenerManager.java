package com.wumple.util.capability;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**
 * Manages the {@link IContainerListener}s that handle syncing of each item capability.
 *
 * @author Choonster
 */
public class CapabilityContainerListenerManager
{
    /**
     * The {@link CapabilityContainerListener} factories.
     */
    private static final Set<Function<EntityPlayerMP, CapabilityContainerListener<?>>> containerListenerFactories = new HashSet<>();

    /**
     * Register a factory for a {@link CapabilityContainerListener}.
     *
     * @param factory
     *            The factory
     */
    public static void registerListenerFactory(final Function<EntityPlayerMP, CapabilityContainerListener<?>> factory)
    {
        containerListenerFactories.add(factory);
    }

    @Mod.EventBusSubscriber
    @SuppressWarnings("unused")
    private static class EventHandler
    {

        /**
         * Add the listeners to a {@link Container}.
         *
         * @param player
         *            The player
         * @param container
         *            The Container
         */
        private static void addListeners(final EntityPlayerMP player, final Container container)
        {
            containerListenerFactories.forEach(factory -> container.addListener(factory.apply(player)));
        }

        /**
         * Add the listeners to {@link EntityPlayer#inventoryContainer} when an {@link EntityPlayerMP} logs in.
         *
         * @param event
         *            The event
         */
        @SubscribeEvent
        public static void playerLoggedIn(final PlayerLoggedInEvent event)
        {
            if (event.player instanceof EntityPlayerMP)
            {
                final EntityPlayerMP player = (EntityPlayerMP) event.player;
                addListeners(player, player.inventoryContainer);
            }
        }

        /**
         * Add the listeners to {@link EntityPlayer#inventoryContainer} when an {@link EntityPlayerMP} is cloned.
         *
         * @param event
         *            The event
         */
        @SubscribeEvent
        public static void playerClone(final PlayerEvent.Clone event)
        {
            if (event.getEntityPlayer() instanceof EntityPlayerMP)
            {
                final EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
                addListeners(player, player.inventoryContainer);
            }
        }

        /**
         * Add the listeners to a {@link Container} when it's opened by an {@link EntityPlayerMP}.
         *
         * @param event
         *            The event
         */
        @SubscribeEvent
        public static void containerOpen(final PlayerContainerEvent.Open event)
        {
            if (event.getEntityPlayer() instanceof EntityPlayerMP)
            {
                final EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
                addListeners(player, event.getContainer());
            }
        }
        
        /*
        // MAYBE remove listeners on container close
        // but how to know which ones since they were created by iterating over factories?
        // Oh no - add a capability that handles it.
        @SubscribeEvent
        public static void containerClose(PlayerContainerEvent.Close event)
        { 
        	   if (event.getEntityPlayer() instanceof EntityPlayerMP)
               {
                   final EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
                   removeListeners(player, event.getContainer());
               }
            }
        }
        */
    }
}
