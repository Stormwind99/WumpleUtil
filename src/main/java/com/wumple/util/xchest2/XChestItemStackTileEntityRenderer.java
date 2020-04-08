package com.wumple.util.xchest2;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class XChestItemStackTileEntityRenderer<T extends Block> extends ItemStackTileEntityRenderer
{
	protected TileEntity getTileEntity() { return null; }
	protected boolean shouldRender(Block block) 
	{ 
		//return (block instanceof T);
		return false;
	}
	
	@Override
    public void renderByItem(ItemStack itemStackIn)
    {
		Item item = itemStackIn.getItem();
		Block block = Block.getBlockFromItem(item);
		
        if (shouldRender(block))
        {
        	TileEntityRendererDispatcher.instance.renderAsItem(getTileEntity());
        }
        else
        {
            super.renderByItem(itemStackIn);
        }
    }
}