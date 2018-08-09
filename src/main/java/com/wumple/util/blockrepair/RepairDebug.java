package com.wumple.util.blockrepair;

import com.wumple.util.ModConfig;
import com.wumple.util.Reference;
import com.wumple.util.base.misc.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RepairDebug
{
    /*
     * Draw debug screen extras
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    static public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        if (ModConfig.zdebugging.debug == true)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.gameSettings.showDebugInfo == true)
            {
                addTileEntityDebug(e);
            }
        }
    }
    
    /*
     * Add Entity debug text to debug screen if looking at Entity
     */
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
     */
    @SideOnly(Side.CLIENT)
    public static void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null)
        {
            BlockPos blockpos = (mc.objectMouseOver == null) ? null : mc.objectMouseOver.getBlockPos();
            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
            
            IRepairingTimes itimes = Util.as(te, IRepairingTimes.class);
            
            if (itimes != null)
            {
                long currentTime = mc.world.getTotalWorldTime();
                long repairTime = itimes.getTimeToRepairAt();
                long giveupTime = itimes.getTimeToGiveUpAt();
                e.getRight().add(I18n.format("misc.wumpleutil.debug.repairtimes", currentTime, repairTime, giveupTime));
            }
        }
    }

}