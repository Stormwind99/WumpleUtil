package com.wumple.util.blockrepair;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import com.wumple.util.ModConfig;
import com.wumple.util.Reference;
import com.wumple.util.base.misc.Util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

/*
 * Originally based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class RepairManager
{
	public static void log(String msg)
	{
		if (isDebugEnabled())
		{
			LogManager.getLogger(Reference.MOD_ID).info(msg);
		}
	}

	public static boolean isDebugEnabled()
	{
		return ModConfig.BlockRepairDebugging.debug.get();
	}

	public RepairManager()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	protected Block getRepairingBlock()
	{
		return MyObjectHolder.blockRepairingBlock;
	}

	/*
	 * Place repair block methods
	 */

	/**
	 *
	 * Some mod blocks might require getting data only while their block is still
	 * around, so we get it here and save it rather than on the fly later
	 *
	 * @param world
	 * @param pos
	 */
	protected IRepairingTimes replaceBlockAndBackup(IWorld world, BlockPos pos, int ticksToRepair)
	{
		RepairingState rstate = new RepairingState(world, pos, ticksToRepair);

		return setupRepairingBlock(rstate, world, pos);
	}

	protected IRepairingTimes setupRepairingBlock(RepairingState rstate, IWorld world, BlockPos pos)
	{
		world.setBlockState(pos, getRepairingBlock().getDefaultState(), 3);

		TileEntity tEnt = world.getTileEntity(pos);
		IRepairing repairing = Util.as(tEnt, IRepairing.class);
		if (repairing != null)
		{
			// BlockState state = world.getBlockState(pos);
			RepairManager.log("set repairing block for pos: " + pos + ", " + rstate.orig_blockState.getBlock());
			repairing.setRepairingState(rstate);
			return repairing;
		}
		else
		{
			RepairManager.log("failed to set repairing block for pos: " + pos);
			return null;
		}
	}

	protected boolean canConvertToRepairingBlock(IWorld world, BlockState state)
	{
		// TODO
		return true;
	}

	public boolean replaceBlock(IWorld world, BlockState state, BlockPos pos, int ticks)
	{
		if (!canConvertToRepairingBlock(world, state))
		{
			RepairManager.log("cant use repairing block on: " + state);
			return false;
		}

		replaceBlockAndBackup(world, pos, ticks);
		return true;
	}

	public boolean shouldRepair(PlayerEntity player, Block block, BlockState blockstate, IWorld world, BlockPos pos)
	{
		// override to change behavior
		return false;
	}

	public int getTicksToRepair()
	{
		// override to change behavior
		return 200;
	}

	// idea from LilRichy's RegrowableLeaves EventHandler.breakEvent()
	public void onBreak(BlockEvent.BreakEvent event)
	{
		BlockPos pos = event.getPos();
		BlockState blockstate = event.getState();
		IWorld world = event.getWorld();
		Block block = blockstate.getBlock();
		PlayerEntity player = event.getPlayer();

		// don't allow breaking repairing blocks normally
		TileEntity tEnt = world.getTileEntity(pos);
		IRepairing repairing = Util.as(tEnt, IRepairing.class);
		if (repairing != null)
		{
			event.setCanceled(true);
			return;
		}

		// only handle replacement on server
		if (world.isRemote())
		{
			return;
		}

		// check if should replace with repairing block
		if (shouldRepair(player, block, blockstate, world, pos))
		{
			// replace with repairing block
			replaceBlock(world, blockstate, pos, getTicksToRepair());

			// harvest original block
			block.harvestBlock(world.getWorld(), player, pos, blockstate, null, ItemStack.EMPTY);

			// don't handle event later since we did handled it
			event.setCanceled(true);
		}
	}

	// after explosions, make sure block to put repairing block into is air and not already repairing block
	protected boolean isBlockOkayForRepairingBlock(World world, BlockPos pos)
	{
		boolean isOkay = world.getBlockState(pos).isAir(world, pos);

		if (isOkay)
		{
			TileEntity tEnt = world.getTileEntity(pos);
			isOkay = !(tEnt instanceof IRepairing);
		}

		return isOkay;
	}

	public void onDetonate(ExplosionEvent.Detonate event)
	{
		// only handle replacement on server
		if (event.getWorld().isRemote())
		{
			return;
		}

		PlayerEntity player = null;
		World world = event.getWorld();
		int ticksToRepair = getTicksToRepair();

		HashMap<BlockPos, RepairingState> pendingRepairs = new HashMap<BlockPos, RepairingState>();

		// remember data for all potentially repairable blocks in explosion
		List<BlockPos> positions = event.getAffectedBlocks();
		for (BlockPos pos : positions)
		{
			BlockState blockstate = world.getBlockState(pos);
			Block block = blockstate.getBlock();

			// check if should replace with repairing block
			if (shouldRepair(player, block, blockstate, world, pos))
			{
				pendingRepairs.put(pos, new RepairingState(world, pos, ticksToRepair));
			}
		}

		// create repairing blocks later after explosion finished
		MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		if (server != null)
		{
			server.enqueue(new TickDelayedTask(server.getTickCounter(), 
					new Runnable()
			{
				@Override
				public void run()
				{
					pendingRepairs.forEach((p, s) -> {
						if (isBlockOkayForRepairingBlock(world, p))
						{
							setupRepairingBlock(s, world, p);
						}
					});
				}
			}) );
		}

	}

	// TODO Fire - but no event to handle!
}