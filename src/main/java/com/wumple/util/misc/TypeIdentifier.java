package com.wumple.util.misc;

import java.util.Random;

import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

/*
 * Identify something in MC using a string, and generate it or transform something into it
 */
public class TypeIdentifier
{
    protected static Random random = new Random();
    public String id = null;

    public static TypeIdentifier build()
    {
        return new TypeIdentifier();
    }

    public static TypeIdentifier build(String idIn)
    {
        return new TypeIdentifier(idIn);
    }

    public TypeIdentifier()
    {
    }

    public TypeIdentifier(String idIn)
    {
        setID(idIn);
    }

    public void setID(String key)
    {
    	id = key;
    }

    public ItemStack create(int count)
    {
        if ((id == null) || (id.isEmpty()))
        {
            return ItemStack.EMPTY;
        }
        
        // MAYBE check if id begins with '#' and skip to creation from tag if so

        IForgeRegistry<Item> reg = GameRegistry.findRegistry(Item.class);
        ResourceLocation res = new ResourceLocation(id);
        Item item = reg.getValue(res);

        // MAYBE support more than just item tags?
        if (item == null)
        {
        	Tag<Item> tagItem = ItemTags.getCollection().get(res);
        	item = tagItem.getRandomElement(random);
        }

        if (item == null)
        {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, count);
    }
    
    protected IThing transform(IThing thing, IThing newthing)
    {
        // if source was tileentity or entity, then result should be ItemEntity
        if (thing.is(Entity.class) || thing.is(TileEntity.class))
        {
            BlockPos pos = thing.getPos();
            World world = thing.getWorld();
            
            // if source was tileentity, then set the tile to air since it rotted.  Entities just die.
            thing.invalidate();
           
            // if an itemstack resulted from rotting, put it into an ItemEntity at source's location (now empty)
            ItemStack newstack = (newthing != null) ? newthing.as(ItemStack.class) : null;
            if (newstack != null)
            {
                ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), newstack);
                world.addEntity(entity);
                return new EntityThing(entity);
            }
            // else just return the new ItemStackThing, which is likely null
            else
            {
                return newthing;
            }
        }
        // else just return the new ItemStackThing
        else
        {
            return newthing;
        }

    }
        
    public IThing createThing(int count)
    {
        // TODO - support creating blocks and entities
        return TUtil.to( create(count) );
    }
    
    public IThing createAndTransform(IThing thing)
    {
        int count = (thing != null) ? thing.getCount() : 1;
        IThing newthing = createThing(count);
        return transform(thing, newthing);
    }

}