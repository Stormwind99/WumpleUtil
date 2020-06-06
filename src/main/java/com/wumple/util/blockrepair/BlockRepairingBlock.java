package com.wumple.util.blockrepair;

import javax.annotation.Nullable;

import com.wumple.util.ModConfiguration;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/*
 * Originally based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class BlockRepairingBlock extends AirBlock implements ITileEntityProvider
{
	public BlockRepairingBlock()
	{
		super(Block.Properties
				.create(Material.AIR)
				.hardnessAndResistance(-1.0F, 3600000.0F)
				.noDrops()
				.doesNotBlockMovement()
				.variableOpacity()
				);
	}
	
	// ------------------------------------------------------------------------
	// Overrides

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		// show debug model if debugging, invisible if not debugging
		if (ModConfiguration.BlockRepairDebugging.showRepairingBlocks.get())
		{
			return BlockRenderType.MODEL;
		}
		else
		{
			return super.getRenderType(state);
		}
	}

	/*
	@Override
	@Deprecated
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos)
	{
		TileEntity tEnt = worldIn.getTileEntity(pos);
	
		if (tEnt instanceof TileEntityRepairingBlock)
		{
			return ((TileEntityRepairingBlock) tEnt).getOrig_hardness();
		}
		else
		{
			return super.getBlockHardness(blockState, worldIn, pos);
		}
	}
	*/

	@Override
	public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn)
	{
		// deny from happening
	}

	@Override
	@Deprecated
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext)
	{
		return true;
	}
	
	// ------------------------------------------------------------------------
	// ITileEntityProvider and TileEntity related
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return new TileEntityRepairingBlock();
	}
	
	@Override
	public boolean hasTileEntity()
	{
		return true;
	}
	
	// ------------------------------------------------------------------------
	// From ContainerBlock

	/**
	 * Called on server when World#addBlockEvent is called. If server returns true,
	 * then also called on the client. On the Server, this may perform additional
	 * changes to the world, like pistons replacing the block with an extended base.
	 * On the client, the update may involve replacing tile entities or effects such
	 * as sounds or particles
	 * 
	 * @deprecated call via
	 *             {@link BlockState#onBlockEventReceived(World,BlockPos,int,int)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param)
	{
		super.eventReceived(state, worldIn, pos, id, param);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	@Nullable
	public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos)
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider) tileentity : null;
	}
}