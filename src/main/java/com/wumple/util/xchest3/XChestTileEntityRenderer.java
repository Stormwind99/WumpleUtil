package com.wumple.util.xchest3;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraft.client.renderer.tileentity.model.LargeChestModel;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class XChestTileEntityRenderer<T extends TileEntity & IChestLid> extends com.wumple.util.xchest2.XChestTileEntityRenderer<T>
{
	private static final ResourceLocation TEXTURE_NORMAL_DOUBLE = new ResourceLocation(
			"textures/entity/chest/normal_double.png");

	private final ChestModel largeChest = new LargeChestModel();
	
	protected ResourceLocation getTextureDouble()
	{
		return TEXTURE_NORMAL_DOUBLE;
	}
	
	protected ChestModel getModelDouble()
	{
		return largeChest;
	}

	@Override
	public void render(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage)
	{
		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		BlockState blockstate = tileEntityIn.hasWorld() ? tileEntityIn.getBlockState()
				: getBlock().getDefaultState().with(XChestBlock.FACING, Direction.SOUTH);
		ChestType chesttype = blockstate.has(XChestBlock.TYPE) ? blockstate.get(XChestBlock.TYPE) : ChestType.SINGLE;
		if (chesttype != ChestType.LEFT)
		{
			boolean flag = chesttype != ChestType.SINGLE;
			ChestModel chestmodel = this.getChestModel(tileEntityIn, destroyStage, flag);
			if (destroyStage >= 0)
			{
				GlStateManager.matrixMode(5890);
				GlStateManager.pushMatrix();
				GlStateManager.scalef(flag ? 8.0F : 4.0F, 4.0F, 1.0F);
				GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
				GlStateManager.matrixMode(5888);
			}
			else
			{
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			}

			GlStateManager.pushMatrix();
			GlStateManager.enableRescaleNormal();
			GlStateManager.translatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
			GlStateManager.scalef(1.0F, -1.0F, -1.0F);
			float f = blockstate.get(XChestBlock.FACING).getHorizontalAngle();
			if ((double) Math.abs(f) > 1.0E-5D)
			{
				GlStateManager.translatef(0.5F, 0.5F, 0.5F);
				GlStateManager.rotatef(f, 0.0F, 1.0F, 0.0F);
				GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
			}

			this.applyLidRotation(tileEntityIn, partialTicks, chestmodel);
			chestmodel.renderAll();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (destroyStage >= 0)
			{
				GlStateManager.matrixMode(5890);
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(5888);
			}

		}
	}

	protected ChestModel getChestModel(T tileEntityIn, int destroyStage, boolean doubleChest)
	{
		ResourceLocation resourcelocation;
		if (destroyStage >= 0)
		{
			resourcelocation = DESTROY_STAGES[destroyStage];
		}
		else
		{
			resourcelocation = doubleChest ? getTextureDouble() : getTexture();
		}

		this.bindTexture(resourcelocation);
		return doubleChest ? getModelDouble() : getModel();
	}

}
