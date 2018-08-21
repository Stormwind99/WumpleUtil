package com.wumple.util.capability.targetcrafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// @Mod.EventBusSubscriber
abstract public class CapHandler
{
    public CapHandler()
    {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    protected boolean isDebugging()
    { return false; }
    
    abstract protected IContainerCraftingOwner getTCCap(ICapabilityProvider provider);
    
    protected IContainerCraftingOwner getTCCap(World worldIn, BlockPos pos)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return getTCCap(tileentity);
    }
    
    // @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getEntityPlayer().isSpectator()) { return; }
            
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        
        IContainerCraftingOwner cap = getTCCap(worldIn, pos);

        if (cap != null)
        {
            cap.onRightBlockClicked(event);
        }
    }
    
    // @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        World worldIn = event.getWorld();
        
        if (worldIn.isRemote)
        {
            return;
        }
        
        BlockPos pos = event.getPos();
        IContainerCraftingOwner cap = getTCCap(worldIn, pos);
        
        if (cap != null)
        {
            cap.onBlockBreak(worldIn, pos);
        }
    }

    @SideOnly(Side.CLIENT)
    // @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.gameSettings.showDebugInfo == true)
        {
            if (isDebugging())
            {
                addTileEntityDebug(e);
            }
        }
    }
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
     */
    @SideOnly(Side.CLIENT)
    public void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        // tile entity
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null)
        {
            BlockPos blockpos = (mc.objectMouseOver == null) ? null : mc.objectMouseOver.getBlockPos();
            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
            IContainerCraftingOwner cap = getTCCap(te);
            if (cap != null)
            {
                List<String> tips = new ArrayList<String>();
                cap.doTooltip(null, mc.player, true, tips);
                
                for (String tip : tips)
                {
                    e.getRight().add(tip);
                }
            }
        }
    }
}
