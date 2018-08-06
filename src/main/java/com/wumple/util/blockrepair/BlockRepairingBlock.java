package com.wumple.util.blockrepair;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class BlockRepairingBlock extends BlockContainer
{
    public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB NO_COLLIDE_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    public BlockRepairingBlock()
    {
        super(Material.PLANTS);
        // stone, fallback default
        setHardness(1.5F);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && source instanceof World)
        {
            return getSelectedBoundingBox(state, (World) source, pos);
        }
        else
        {
            return NO_COLLIDE_AABB;
        }
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        /*
        // For CoroUtil's ItemRepairingGel:
        // special behavior to let block only be selectable to repair if correct context
        if (Minecraft.getMinecraft().player != null 
                // && (!Minecraft.getMinecraft().player.isSneaking()
                && Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemRepairingGel)
                ) {
            return AABB;
        } 
        else {
            return NO_COLLIDE_AABB;
        }
        */
        return NO_COLLIDE_AABB;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return super.canCollideCheck(state, hitIfLiquid);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityRepairingBlock();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        // hint: if not in debug mode, return EnumBlockRenderType.INVISIBLE
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos)
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

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
    {
        TileEntity tEnt = world.getTileEntity(pos);

        if (tEnt instanceof TileEntityRepairingBlock)
        {
            return ((TileEntityRepairingBlock) tEnt).getOrig_explosionResistance();
        }
        else
        {
            return super.getExplosionResistance(world, pos, exploder, explosion);
        }

    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        // deny from happening
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos,
            EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer)
    {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.AIR;
    }
}