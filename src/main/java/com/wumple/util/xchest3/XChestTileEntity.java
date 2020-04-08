package com.wumple.util.xchest3;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class XChestTileEntity extends com.wumple.util.xchest2.XChestTileEntity
{

	public int calculatePlayersUsing(World p_213976_0_, LockableTileEntity p_213976_1_, int p_213976_2_,
			int p_213976_3_, int p_213976_4_)
	{
		int i = 0;
		float f = 5.0F;

		for (PlayerEntity playerentity : p_213976_0_.getEntitiesWithinAABB(PlayerEntity.class,
				new AxisAlignedBB((double) ((float) p_213976_2_ - 5.0F), (double) ((float) p_213976_3_ - 5.0F),
						(double) ((float) p_213976_4_ - 5.0F), (double) ((float) (p_213976_2_ + 1) + 5.0F),
						(double) ((float) (p_213976_3_ + 1) + 5.0F), (double) ((float) (p_213976_4_ + 1) + 5.0F))))
		{
			if (playerentity.openContainer instanceof ChestContainer)
			{
				IInventory iinventory = ((ChestContainer) playerentity.openContainer).getLowerChestInventory();
				if (iinventory == p_213976_1_ || iinventory instanceof DoubleSidedInventory
						&& ((DoubleSidedInventory) iinventory).isPartOfLargeChest(p_213976_1_))
				{
					++i;
				}
			}
		}

		return i;
	}
	
	protected void playSound(SoundEvent soundIn)
	{
		ChestType chesttype = this.getBlockState().get(XChestBlock.TYPE);
		if (chesttype != ChestType.LEFT)
		{
			double d0 = (double) this.pos.getX() + 0.5D;
			double d1 = (double) this.pos.getY() + 0.5D;
			double d2 = (double) this.pos.getZ() + 0.5D;
			if (chesttype == ChestType.RIGHT)
			{
				Direction direction = ChestBlock.getDirectionToAttached(this.getBlockState());
				d0 += (double) direction.getXOffset() * 0.5D;
				d2 += (double) direction.getZOffset() * 0.5D;
			}

			this.world.playSound((PlayerEntity) null, d0, d1, d2, soundIn, SoundCategory.BLOCKS, 0.5F,
					this.world.rand.nextFloat() * 0.1F + 0.9F);
		}
	}

	protected net.minecraftforge.items.IItemHandlerModifiable createHandler()
	{
		BlockState state = this.getBlockState();
		if (!(state.getBlock() instanceof ChestBlock))
		{
			return new net.minecraftforge.items.wrapper.InvWrapper(this);
		}
		ChestType type = state.get(XChestBlock.TYPE);
		if (type != ChestType.SINGLE)
		{
			BlockPos opos = this.getPos().offset(ChestBlock.getDirectionToAttached(state));
			BlockState ostate = this.getWorld().getBlockState(opos);
			if (state.getBlock() == ostate.getBlock())
			{
				ChestType otype = ostate.get(XChestBlock.TYPE);
				if (otype != ChestType.SINGLE && type != otype
						&& state.get(ChestBlock.FACING) == ostate.get(ChestBlock.FACING))
				{
					TileEntity ote = this.getWorld().getTileEntity(opos);
					if (ote instanceof XChestTileEntity)
					{
						IInventory top = type == ChestType.RIGHT ? this : (IInventory) ote;
						IInventory bottom = type == ChestType.RIGHT ? (IInventory) ote : this;
						return new net.minecraftforge.items.wrapper.CombinedInvWrapper(
								new net.minecraftforge.items.wrapper.InvWrapper(top),
								new net.minecraftforge.items.wrapper.InvWrapper(bottom));
					}
				}
			}
		}
		return new net.minecraftforge.items.wrapper.InvWrapper(this);
	}
}
