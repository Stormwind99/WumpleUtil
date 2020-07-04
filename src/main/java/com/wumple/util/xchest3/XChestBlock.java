package com.wumple.util.xchest3;

import javax.annotation.Nullable;

import com.wumple.util.xchest2.XChestTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class XChestBlock extends com.wumple.util.xchest2.XChestBlock
{
	public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
	protected static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D);
	protected static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D);
	protected static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
	protected static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D);

	protected XChestBlock(Block.Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH)
				.with(TYPE, ChestType.SINGLE).with(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos)
	{
		if (stateIn.get(WATERLOGGED))
		{
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}

		if (facingState.getBlock() == this && facing.getAxis().isHorizontal())
		{
			ChestType chesttype = facingState.get(TYPE);
			if (stateIn.get(TYPE) == ChestType.SINGLE && chesttype != ChestType.SINGLE
					&& stateIn.get(FACING) == facingState.get(FACING)
					&& getDirectionToAttached(facingState) == facing.getOpposite())
			{
				return stateIn.with(TYPE, chesttype.opposite());
			}
		}
		else if (getDirectionToAttached(stateIn) == facing)
		{
			return stateIn.with(TYPE, ChestType.SINGLE);
		}

		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		if (state.get(TYPE) == ChestType.SINGLE)
		{
			return super.getShape(state, worldIn, pos, context);
		}
		else
		{
			switch (getDirectionToAttached(state))
			{
			case NORTH:
			default:
				return SHAPE_NORTH;
			case SOUTH:
				return SHAPE_SOUTH;
			case WEST:
				return SHAPE_WEST;
			case EAST:
				return SHAPE_EAST;
			}
		}
	}

	public Direction getDirectionToAttached(BlockState state)
	{
		Direction direction = state.get(FACING);
		return state.get(TYPE) == ChestType.LEFT ? direction.rotateY() : direction.rotateYCCW();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		ChestType chesttype = ChestType.SINGLE;
		Direction direction = context.getPlacementHorizontalFacing().getOpposite();
		IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
		//boolean flag = context.isPlacerSneaking();
		boolean flag = context.func_225518_g_();
		Direction direction1 = context.getFace();
		if (direction1.getAxis().isHorizontal() && flag)
		{
			Direction direction2 = this.getDirectionToAttach(context, direction1.getOpposite());
			if (direction2 != null && direction2.getAxis() != direction1.getAxis())
			{
				direction = direction2;
				chesttype = direction2.rotateYCCW() == direction1.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
			}
		}

		if (chesttype == ChestType.SINGLE && !flag)
		{
			if (direction == this.getDirectionToAttach(context, direction.rotateY()))
			{
				chesttype = ChestType.LEFT;
			}
			else if (direction == this.getDirectionToAttach(context, direction.rotateYCCW()))
			{
				chesttype = ChestType.RIGHT;
			}
		}

		return this.getDefaultState().with(FACING, direction).with(TYPE, chesttype).with(WATERLOGGED,
				Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
	}

	@Nullable
	protected Direction getDirectionToAttach(BlockItemUseContext p_196312_1_, Direction p_196312_2_)
	{
		BlockState blockstate = p_196312_1_.getWorld().getBlockState(p_196312_1_.getPos().offset(p_196312_2_));
		return blockstate.getBlock() == this && blockstate.get(TYPE) == ChestType.SINGLE ? blockstate.get(FACING)
				: null;
	}
	
	@Nullable
	public <T> T getChestInventory(BlockState p_220106_0_, IWorld p_220106_1_, BlockPos p_220106_2_,
			boolean allowBlocked, XChestBlock.InventoryFactory<T> p_220106_4_)
	{
		TileEntity tileentity = p_220106_1_.getTileEntity(p_220106_2_);
		if (!(tileentity instanceof XChestTileEntity))
		{
			return (T) null;
		}
		else if (!allowBlocked && isBlocked(p_220106_1_, p_220106_2_))
		{
			return (T) null;
		}
		else
		{
			XChestTileEntity XChestTileEntity = (XChestTileEntity) tileentity;
			ChestType chesttype = p_220106_0_.get(TYPE);
			if (chesttype == ChestType.SINGLE)
			{
				return p_220106_4_.forSingle(XChestTileEntity);
			}
			else
			{
				BlockPos blockpos = p_220106_2_.offset(getDirectionToAttached(p_220106_0_));
				BlockState blockstate = p_220106_1_.getBlockState(blockpos);
				if (blockstate.getBlock() == p_220106_0_.getBlock())
				{
					ChestType chesttype1 = blockstate.get(TYPE);
					if (chesttype1 != ChestType.SINGLE && chesttype != chesttype1
							&& blockstate.get(FACING) == p_220106_0_.get(FACING))
					{
						if (!allowBlocked && isBlocked(p_220106_1_, blockpos))
						{
							return (T) null;
						}

						TileEntity tileentity1 = p_220106_1_.getTileEntity(blockpos);
						if (tileentity1 instanceof XChestTileEntity)
						{
							XChestTileEntity chesttileentity1 = chesttype == ChestType.RIGHT ? XChestTileEntity
									: (XChestTileEntity) tileentity1;
							XChestTileEntity chesttileentity2 = chesttype == ChestType.RIGHT
									? (XChestTileEntity) tileentity1
									: XChestTileEntity;
							return p_220106_4_.forDouble(chesttileentity1, chesttileentity2);
						}
					}
				}

				return p_220106_4_.forSingle(XChestTileEntity);
			}
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, TYPE, WATERLOGGED);
	}
	
	// ---------------------------------------
	
	protected static final XChestBlock.InventoryFactory<IInventory> field_220109_i = new XChestBlock.InventoryFactory<IInventory>()
	{
		public IInventory forDouble(XChestTileEntity p_212855_1_, XChestTileEntity p_212855_2_)
		{
			return new DoubleSidedInventory(p_212855_1_, p_212855_2_);
		}

		public IInventory forSingle(XChestTileEntity p_212856_1_)
		{
			return p_212856_1_;
		}
	};
	
	protected static final XChestBlock.InventoryFactory<INamedContainerProvider> field_220110_j = new XChestBlock.InventoryFactory<INamedContainerProvider>()
	{
		public INamedContainerProvider forDouble(final XChestTileEntity p_212855_1_, final XChestTileEntity p_212855_2_)
		{
			final IInventory iinventory = new DoubleSidedInventory(p_212855_1_, p_212855_2_);
			return new INamedContainerProvider()
			{
				@Nullable
				public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_,
						PlayerEntity p_createMenu_3_)
				{
					if (p_212855_1_.canOpen(p_createMenu_3_) && p_212855_2_.canOpen(p_createMenu_3_))
					{
						p_212855_1_.fillWithLoot(p_createMenu_2_.player);
						p_212855_2_.fillWithLoot(p_createMenu_2_.player);
						return ChestContainer.createGeneric9X6(p_createMenu_1_, p_createMenu_2_, iinventory);
					}
					else
					{
						return null;
					}
				}

				public ITextComponent getDisplayName()
				{
					if (p_212855_1_.hasCustomName())
					{
						return p_212855_1_.getDisplayName();
					}
					else
					{
						return (ITextComponent) (p_212855_2_.hasCustomName() ? p_212855_2_.getDisplayName()
								: new TranslationTextComponent("container.chestDouble"));
					}
				}
			};
		}

		public INamedContainerProvider forSingle(XChestTileEntity p_212856_1_)
		{
			return p_212856_1_;
		}
	};

	protected interface InventoryFactory<T>
	{
		T forDouble(XChestTileEntity p_212855_1_, XChestTileEntity p_212855_2_);

		T forSingle(XChestTileEntity p_212856_1_);
	}
}
