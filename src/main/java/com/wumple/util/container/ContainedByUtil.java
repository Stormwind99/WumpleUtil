package com.wumple.util.container;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;
import com.wumple.util.capability.CapabilityUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainedByUtil
{
    // horrid hack - if container contains item we know about, it is the container we are looking for
    public static boolean isPlayerWithContainerOpen(PlayerEntity player, TileEntity container, ItemStack itemToSearchFor)
    {
        boolean add = false;

        // horrid hack - if container contains item we know about, it is the container we are looking for
        if (!add && (player.openContainer != null) && (itemToSearchFor != null) && (!itemToSearchFor.isEmpty()))
        {
            NonNullList<ItemStack> stack = player.openContainer.getInventory();

            if (stack.contains(itemToSearchFor))
            {
                add = true;
            }
        }

        // MAYBE if player.openContainer.listeners if it wasn't private (use AT)?

        return add;
    }

    protected static <T extends Entity> List<T> getEntitiesNearby(Class<? extends T> classEntity, BlockPos pos, World world)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        AxisAlignedBB box = new AxisAlignedBB((double) ((float) i - 5.0F), (double) ((float) j - 5.0F), (double) ((float) k - 5.0F),
                (double) ((float) (i + 1) + 5.0F), (double) ((float) (j + 1) + 5.0F),
                (double) ((float) (k + 1) + 5.0F));

        return world.getEntitiesWithinAABB(classEntity, box);
    }

    // horrible hack - find players with container open, by searching nearby and for
    // a known item in the container
    public static NonNullList<PlayerEntity> getPlayersWithContainerOpen(TileEntity container, ItemStack itemToSearchFor)
    {
        NonNullList<PlayerEntity> users = NonNullList.create();

        for (PlayerEntity player : getEntitiesNearby(PlayerEntity.class, container.getPos(), container.getWorld()))
        {
            if (isPlayerWithContainerOpen(player, container, itemToSearchFor))
            {
                users.add(player);
            }
        }

        return users;
    }

    // horrible hack - find players with container open, by searching nearby and for
    // a known item in the container
    public static NonNullList<PlayerEntity> getPlayersWithContainerOpen(Entity container, ItemStack itemToSearchFor)
    {
        NonNullList<PlayerEntity> users = NonNullList.create();

        for (PlayerEntity player : getEntitiesNearby(PlayerEntity.class, container.getPosition(), container.world))
        {
            if (isPlayerWithContainerOpen(player, null, itemToSearchFor))
            {
                users.add(player);
            }
        }

        return users;
    }

    // horrible hack - get the TileEntity corresponding to container
    public static List<TileEntity> getTileEntitiesNearby(BlockPos position, World world)
    {
        int i = position.getX();
        int j = position.getY();
        int k = position.getZ();

        final int x1 = (int) ((float) i - 5.0F);
        final int y1 = (int) ((float) j - 5.0F);
        final int z1 = (int) ((float) k - 5.0F);
        final int x2 = (int) ((float) (i + 1) + 5.0F);
        final int y2 = (int) ((float) (j + 1) + 5.0F);
        final int z2 = (int) ((float) (k + 1) + 5.0F);

        ArrayList<TileEntity> list = new ArrayList<TileEntity>();

        // iterate over all tileentities nearby
        for (BlockPos pos : BlockPos.getAllInBoxMutable(x1, y1, z1, x2, y2, z2))
        {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity != null)
            {
                list.add(tileentity);
            }
        }

        return list;
    }

    // horrible hack - get the TileEntity corresponding to container
    public static TileEntity getTileEntityForContainer(Container container, ItemStack itemToSearchFor, BlockPos position, World world)
    {
        // iterate over all tileentities nearby
        for (TileEntity tileentity : getTileEntitiesNearby(position, world))
        {
            if (doesContain(tileentity, itemToSearchFor))
            {
                return tileentity;
            }
        }

        return null;
    }

    static public boolean doesContain(LazyOptional<IItemHandler> lcapability, ItemStack itemToSearchFor)
    {
        // check TileEntity's IItemHandler capability, if provided
    	return lcapability.map(capability->
        {
            for (int slot = 0; slot < capability.getSlots(); ++slot)
            {
                if (capability.getStackInSlot(slot) == itemToSearchFor)
                {
                    return true;
                }
            }
            
            return false;
        }
        ).orElse(false);
    }

    /*
     * Does the tileentity (via its IItemHandler cap) contain itemToSearchFor?
     */
    static public boolean doesContain(TileEntity tileentity, ItemStack itemToSearchFor)
    {
        // check TileEntity's IItemHandler capability, if provided
        LazyOptional<IItemHandler> capability = CapabilityUtils.fetchCapability(tileentity, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        return doesContain(capability, itemToSearchFor);
    }

    /*
     * Does the tileentity (via its IItemHandler cap) contain itemToSearchFor?
     */
    static public boolean doesContain(Entity entity, ItemStack itemToSearchFor)
    {
        // check TileEntity's IItemHandler capability, if provided
        LazyOptional<IItemHandler> capability = CapabilityUtils.fetchCapability(entity, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        return doesContain(capability, itemToSearchFor);
    }
    
    // horrible hack - guess the world and pos of thing containing stack
    static public Pair<World, BlockPos> getContainedWorldPos(ItemStack stack, Entity entityHint, TileEntity tileEntityHint)
    {
        BlockPos posGuess = null;
        World worldGuess = null;
        if (entityHint != null)
        {
            posGuess = entityHint.getPosition();
            worldGuess = entityHint.getEntityWorld();
        }
        else if (tileEntityHint != null)
        {
            posGuess = tileEntityHint.getPos();
            worldGuess = tileEntityHint.getWorld();
        }
        /*
         * else { posGuess = FoodFunk.proxy.getClientPlayer().getPos(); }
         */

        return Pair.of(worldGuess, posGuess);
    }

    // horrible hack - find the thing containing stack
    static public IThing getContainedBy(ItemStack stack, Entity entityHint, TileEntity tileEntityHint)
    {
        if (doesContain(entityHint, stack))
        {
            return TUtil.to(entityHint);
        }

        if (doesContain(tileEntityHint, stack))
        {
            return TUtil.to(tileEntityHint);
        }

        Pair<World, BlockPos> guess = getContainedWorldPos(stack, entityHint, tileEntityHint);
        
        BlockPos posGuess = guess.getRight();
        World worldGuess = guess.getLeft();

        // TODO open TileEntity container
        // TODO open Entity container

        if ((posGuess == null) || (worldGuess == null))
        {
            return null;
        }

        // other Entities nearby
        for (Entity entity : getEntitiesNearby(Entity.class, posGuess, worldGuess))
        {
            if (doesContain(entity, stack))
            {
                return new EntityThing(entity);
            }
        }

        // other TileEntities nearby
        for (TileEntity entity : getTileEntitiesNearby(posGuess, worldGuess))
        {
            if (doesContain(entity, stack))
            {
                return TUtil.to(entity);
            }
        }

        return null;
    }
    
    public static void checkUpdateSlot(Integer index, IItemHandler itemhandler, ItemStack stack, int count, ItemStack newStack)
    {
        if (newStack == null || newStack.isEmpty() || (newStack != stack))
        {
            if (newStack == null)
            {
                newStack = ItemStack.EMPTY;
            }
            // Equivalent to inventory.setInventorySlotContents(i, rotItem);
            @SuppressWarnings("unused")
            ItemStack oldStack = itemhandler.extractItem(index, count, false);
            itemhandler.insertItem(index, newStack, false);
        }
    }
}