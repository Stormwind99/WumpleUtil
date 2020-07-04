package com.wumple.util.xchest2;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.*;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.*;
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
    public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
		Item item = itemStackIn.getItem();
		Block block = Block.getBlockFromItem(item);
		
        if (shouldRender(block))
        {
            TileEntityRendererDispatcher.instance.renderItem(getTileEntity(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }
        else
        {
            super.render(itemStackIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }
}