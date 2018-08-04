package com.wumple.util.misc;

import java.util.Random;

import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class TypeIdentifier
{
    protected static Random random = new Random();
    public String id = null;
    public Integer meta = null;

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

    public TypeIdentifier(String idIn, Integer metaIn)
    {
        setID(idIn);
        meta = metaIn;
    }

    public void setID(String key)
    {
        // metadata support - class:name@metadata
        int length = (key != null) ? key.length() : 0;
        if ((length >= 2) && (key.charAt(length - 2) == '@'))
        {
            String metastring = key.substring(length - 1);
            meta = Integer.valueOf(metastring);
            id = key.substring(0, length - 2);
        }
        else if ((length >= 3) && (key.charAt(length - 3) == '@'))
        {
            String metastring = key.substring(length - 2);
            meta = Integer.valueOf(metastring);
            id = key.substring(0, length - 3);
        }
        else
        {
            id = key;
            meta = null;
        }
    }

    public ItemStack create(int count)
    {
        if ((id == null) || (id.isEmpty()))
        {
            return ItemStack.EMPTY;
        }

        Item item = Item.REGISTRY.getObject(new ResourceLocation(id));

        if (item == null)
        {
            NonNullList<ItemStack> ores = OreDictionary.getOres(id);
            if (!ores.isEmpty())
            {
                ItemStack choice = ores.get(random.nextInt(ores.size()));
                return choice.copy();
            }
        }

        if (item == null)
        {
            return ItemStack.EMPTY;
        }

        return (meta == null) ? new ItemStack(item, count)
                : new ItemStack(item, count, meta.intValue());
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
           
            // if an itemstack resulted from rotting, put it into an EntityItem at source's location (now empty)
            ItemStack newstack = (newthing != null) ? newthing.as(ItemStack.class) : null;
            if (newstack != null)
            {
                EntityItem entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), newstack);
                world.spawnEntity(entity);
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