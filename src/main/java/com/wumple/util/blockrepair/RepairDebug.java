package com.wumple.util.blockrepair;

import com.wumple.util.ModConfiguration;
import com.wumple.util.base.misc.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

// @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RepairDebug
{
	/// Draw debug screen extras
	// @SubscribeEvent(priority = EventPriority.HIGH)
	static public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
	{
		if (ModConfiguration.BlockRepairDebugging.debug.get() == true)
		{
			Minecraft mc = Minecraft.getInstance();
			if (mc.gameSettings.showDebugInfo == true)
			{
				addTileEntityDebug(e);
			}
		}
	}

	/*
	 * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
	 */
	public static void addTileEntityDebug(RenderGameOverlayEvent.Text e)
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
		{
			BlockRayTraceResult result = (BlockRayTraceResult) mc.objectMouseOver;
			BlockPos blockpos = (result == null) ? null : result.getPos();
			TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);

			IRepairingTimes itimes = Util.as(te, IRepairingTimes.class);

			if (itimes != null)
			{
				long currentTime = mc.world.getGameTime();
				long repairTime = itimes.getTimeToRepairAt();
				long giveupTime = itimes.getTimeToGiveUpAt();
				e.getRight().add(I18n.format("misc.wumpleutil.debug.repairtimes", currentTime, repairTime, giveupTime));
			}
		}
	}

}