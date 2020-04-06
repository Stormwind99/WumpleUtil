package com.wumple.util.xchest;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class XChestTileEntityRenderer<T extends TileEntity & IChestLid> extends TileEntityRenderer<T>
{
	private static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation("textures/entity/chest/normal.png");
	private final ChestModel simpleChest = new ChestModel();

	public XChestTileEntityRenderer()
	{
	}

	protected ResourceLocation getTexture()
	{
		return TEXTURE_NORMAL;
	}

	protected Block getBlock()
	{
		return Blocks.CHEST;
	}

	protected ChestType getChestType(BlockState blockstate)
	{
		ChestType chesttype = blockstate.has(ChestBlock.TYPE) ? blockstate.get(ChestBlock.TYPE) : ChestType.SINGLE;
		
		return chesttype;
	}

	public void render(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage)
	{
		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		BlockState blockstate = tileEntityIn.hasWorld() ? tileEntityIn.getBlockState()
				: getBlock().getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
		ChestType chesttype = getChestType(blockstate);
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
			float f = blockstate.get(ChestBlock.FACING).getHorizontalAngle();
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
			resourcelocation = getTexture();
		}

		this.bindTexture(resourcelocation);
		return this.simpleChest;
	}

	protected void applyLidRotation(T p_199346_1_, float p_199346_2_, ChestModel p_199346_3_)
	{
		float f = ((IChestLid) p_199346_1_).getLidAngle(p_199346_2_);
		f = 1.0F - f;
		f = 1.0F - f * f * f;
		p_199346_3_.getLid().rotateAngleX = -(f * ((float) Math.PI / 2F));
	}
}
