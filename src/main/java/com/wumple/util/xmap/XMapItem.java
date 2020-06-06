package com.wumple.util.xmap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MapItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class XMapItem extends MapItem implements IXMapItem
{
	public static final String ID = "wumpleutil:xmap";
	
	public XMapItem(Properties builder)
	{
		super(builder);
	}

	public String getID()
	{
		return ID;
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when
	 * this item is used on a Block, see {@link #onItemUse}.
	 */
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
	{
		int scale = XMapAPI.getInstance().getDefaultScale();
		ItemStack itemstack = XMapAPI.getInstance().setupNewMap(worldIn, MathHelper.floor(playerIn.getPosX()), MathHelper.floor(playerIn.getPosZ()), (byte)scale, true, false);
		ItemStack itemstack1 = playerIn.getHeldItem(handIn);
		if (!playerIn.abilities.isCreativeMode)
		{
			itemstack1.shrink(1);
		}
	
		if (itemstack1.isEmpty())
		{
			return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
		}
		else
		{
			if (!playerIn.inventory.addItemStackToInventory(itemstack.copy()))
			{
				playerIn.dropItem(itemstack, false);
			}
	
			playerIn.addStat(Stats.ITEM_USED.get(this));
			return new ActionResult<>(ActionResultType.SUCCESS, itemstack1);
		}
	}

}