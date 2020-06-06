package com.wumple.util.xchest2;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class XChestBlock extends ContainerBlock implements IWaterLoggable
{
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape field_196315_B = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

	/*
	protected interface InventoryFactory<T>
	{
		T forSingle(XChestTileEntity p_212856_1_);
	}
	
	protected static final XChestBlock.InventoryFactory<IInventory> field_220109_i = new XChestBlock.InventoryFactory<IInventory>()
	{
		public IInventory forSingle(XChestTileEntity p_212856_1_)
		{
			return p_212856_1_;
		}
	};
	
	protected static final XChestBlock.InventoryFactory<INamedContainerProvider> field_220110_j = new XChestBlock.InventoryFactory<INamedContainerProvider>()
	{
		public INamedContainerProvider forSingle(XChestTileEntity p_212856_1_)
		{
			return p_212856_1_;
		}
	};
	*/

	protected XChestBlock(Block.Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH)
				.with(WATERLOGGED, Boolean.valueOf(false)));
	}

	@OnlyIn(Dist.CLIENT)
	public boolean hasCustomBreakingProgress(BlockState state)
	{
		return true;
	}

	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos)
	{
		if (stateIn.get(WATERLOGGED))
		{
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}

		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return field_196315_B;
	}

	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		Direction direction = context.getPlacementHorizontalFacing().getOpposite();
		IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
		
		return this.getDefaultState().with(FACING, direction).with(WATERLOGGED,
				Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
	}

	public IFluidState getFluidState(BlockState state)
	{
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (stack.hasDisplayName())
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof XChestTileEntity)
			{
				((XChestTileEntity) tileentity).setCustomName(stack.getDisplayName());
			}
		}

	}

	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof IInventory)
			{
				InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (worldIn.isRemote)
		{
			return ActionResultType.SUCCESS;
		}
		else
		{
			INamedContainerProvider inamedcontainerprovider = this.getContainer(state, worldIn, pos);
			if (inamedcontainerprovider != null)
			{
				player.openContainer(inamedcontainerprovider);
				player.addStat(this.getOpenStat());
			}

			return ActionResultType.SUCCESS;
		}
	}

	protected Stat<ResourceLocation> getOpenStat()
	{
		return Stats.CUSTOM.get(Stats.OPEN_CHEST);
	}

	@Nullable
	public <T> T getChestInventory(BlockState p_220106_0_, IWorld p_220106_1_, BlockPos p_220106_2_,
			boolean allowBlocked) // , XChestBlock.InventoryFactory<T> p_220106_4_)
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
			return (T)XChestTileEntity; //p_220106_4_.forSingle(XChestTileEntity);
		}
	}

	@Nullable
	public IInventory getInventory(BlockState p_220105_0_, World p_220105_1_, BlockPos p_220105_2_,
			boolean allowBlocked)
	{
		return getChestInventory(p_220105_0_, p_220105_1_, p_220105_2_, allowBlocked); //, field_220109_i);
	}

	@Nullable
	public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos)
	{
		return getChestInventory(state, worldIn, pos, false); //, field_220110_j);
	}

	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return new XChestTileEntity();
	}

	protected boolean isBlocked(IWorld p_220108_0_, BlockPos p_220108_1_)
	{
		return isBelowSolidBlock(p_220108_0_, p_220108_1_) || isCatSittingOn(p_220108_0_, p_220108_1_);
	}

	protected boolean isBelowSolidBlock(IBlockReader p_176456_0_, BlockPos worldIn)
	{
		BlockPos blockpos = worldIn.up();
		return p_176456_0_.getBlockState(blockpos).isNormalCube(p_176456_0_, blockpos);
	}

	protected boolean isCatSittingOn(IWorld p_220107_0_, BlockPos p_220107_1_)
	{
		List<CatEntity> list = p_220107_0_.getEntitiesWithinAABB(CatEntity.class,
				new AxisAlignedBB((double) p_220107_1_.getX(), (double) (p_220107_1_.getY() + 1),
						(double) p_220107_1_.getZ(), (double) (p_220107_1_.getX() + 1),
						(double) (p_220107_1_.getY() + 2), (double) (p_220107_1_.getZ() + 1)));
		if (!list.isEmpty())
		{
			for (CatEntity catentity : list)
			{
				if (catentity.isSitting())
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean hasComparatorInputOverride(BlockState state)
	{
		return true;
	}

	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
	{
		return Container.calcRedstoneFromInventory(getInventory(blockState, worldIn, pos, false));
	}

	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, WATERLOGGED);
	}

	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
	{
		return false;
	}
}
