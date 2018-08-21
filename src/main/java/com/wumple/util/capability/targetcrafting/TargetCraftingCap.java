package com.wumple.util.capability.targetcrafting;

import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.base.misc.Util;
import com.wumple.util.capability.thing.ThingCap;
import com.wumple.util.misc.SUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

abstract public class TargetCraftingCap extends ThingCap<IThing> implements IContainerCraftingOwner
{
    /*
    // in derived class, do:
    
    // The {@link Capability} instance
    @CapabilityInject(IPantographCap.class)
    public static final Capability<IPantographCap> CAPABILITY = null;
    public static final EnumFacing DEFAULT_FACING = null;

    // IDs of the capability
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "pantograph");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPantographCap.class, new PantographCapStorage(), () -> new TargetCraftingCap());
    }
    */

    public static final int INPUT_SLOTS = 9;
    public static final int TARGET_SLOTS = 1;
    public static final int OUTPUT_SLOTS = 1;
    public static final int TOTAL_SLOTS = INPUT_SLOTS + TARGET_SLOTS + OUTPUT_SLOTS;
    public static final int OUTPUT_SLOT = TOTAL_SLOTS - 1;
    public static final int TARGET_SLOT = OUTPUT_SLOT - 1;
    
    public static final int STACK_LIMIT = 64;
    public static final int NO_SLOT = -1;
    public static final double USE_RANGE = 64.0D;

    protected NonNullList<ItemStack> itemStacks = NonNullList.<ItemStack> withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    protected int getFilledSlots()
    {
        int filledSlotCount = 0;
        for (int i = 0; i < INPUT_SLOTS; i++)
        {
            // MAYBE assume all itemstacks that made it into inventory are valid
            // Could reduce expense by eliminating config lookup on itemstack
            filledSlotCount += (isValidInputStack(itemStacks.get(i))) ? 1 : 0;
        }

        return filledSlotCount;
    }

    public boolean hasInputItems()
    {
        return getFilledSlots() > 0;
    }
    
    public boolean isActive()
    {
        return hasInputItems();
    }

    public boolean hasOutputItems()
    {
        return !SUtil.isEmpty(itemStacks.get(OUTPUT_SLOT));
    }

    public int getOutputItemCount()
    {
        ItemStack output = itemStacks.get(OUTPUT_SLOT);
        return (output != null) ? output.getCount() : 0;
    }
    
    abstract public boolean isValidInputStack(ItemStack itemStack);
    // { return itemStack.getItem() == Items.FILLED_MAP; }

    abstract public boolean isValidTargetStack(ItemStack itemStack);
    // { return itemStack.getItem() == Items.MAP; }
    
    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.itemStacks)
        {
            if (!SUtil.isEmpty(itemstack))
            {
                return false;
            }
        }

        return true;
    }

    protected World getWorld()
    {
        return owner.getWorld();
    }

    protected BlockPos getPos()
    {
        return owner.getPos();
    }

    // ----------------------------------------------------------------------
    /// IPantographCap

    @Override
    public NBTBase serializeNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();

        ItemStackHelper.saveAllItems(compound, itemStacks);

        return compound;
    }

    @Override
    public void deserializeNBT(NBTBase nbt)
    {
        NBTTagCompound compound = Util.as(nbt, NBTTagCompound.class);

        if (compound == null)
        {
            return;
        }

        ItemStackHelper.loadAllItems(compound, itemStacks);
    }

    // ----------------------------------------------------------------------
    /// Event handling via PantographyHandler

    @Override
    public void onBlockBreak(World worldIn, BlockPos pos)
    {
        setInventorySlotContents(OUTPUT_SLOT, null, false);
        InventoryHelper.dropInventoryItems(worldIn, pos, this);
        worldIn.updateComparatorOutputLevel(pos, worldIn.getBlockState(pos).getBlock());
    }

    @Override
    public void onRightBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer playerIn = event.getEntityPlayer();

        if (worldIn.getBlockState(pos.up()).doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN))
        {
            event.setCancellationResult(EnumActionResult.FAIL);
            event.setCanceled(true);
        }
        else if (worldIn.isRemote)
        {
            event.setCanceled(true);
        }
        else
        {
            // custom display names are updated on client via CustomNamedTileEntity.getUpdateTag() and friends
            openGui(playerIn, worldIn, pos);
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
    
    abstract public void openGui(EntityPlayer playerIn, World world, BlockPos pos);
    /* { playerIn.openGui(Pantography.instance, GuiHandlerPantograph.myGuiID, worldIn, pos.getX(), pos.getY(), pos.getZ()); }

    // ----------------------------------------------------------------------
    // TileEntity

    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        TileEntity te = owner.as(TileEntity.class);
        if (te != null)
        {
            te.updateContainingBlockInfo();
        }
        owner.invalidate();
    }

    // ----------------------------------------------------------------------
    // IWorldNameable
    
    public String getLocalizationID()
    {
        return "container.wumpleutil.targetcrafting";
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName()
    {
        IWorldNameable i = owner.as(IWorldNameable.class);
        return (i != null) ? i.getName() : getLocalizationID();
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        IWorldNameable i = owner.as(IWorldNameable.class);
        return (i != null) ? i.hasCustomName() : false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        IWorldNameable i = owner.as(IWorldNameable.class);
        return (i != null) ? i.getDisplayName() : null;
    }

    // ----------------------------------------------------------------------
    // IInventory

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory()
    {
        return this.itemStacks.size();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack item)
    {
        if ((slot >= 0) && (slot < TARGET_SLOT))
        {
            return isValidInputStack(item);
        }
        
        if (slot == TARGET_SLOT)
        {
            return isValidTargetStack(item);
        }

        return false;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return (index >= 0) && (index < this.itemStacks.size()) ? (ItemStack) this.itemStacks.get(index) : ItemStack.EMPTY;
    }
    
    protected void updateInternalState(int index)
    { }
    
    abstract public SoundEvent getCraftingSound();
    
    void playCraftingSound(BlockPos pos, World world)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
    
        double d1 = (double) i + 0.5D;
        double d2 = (double) k + 0.5D;
        world.playSound((EntityPlayer) null, d1, (double) j + 0.5D, d2, getCraftingSound(),
                SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
    }

    protected ItemStack postRemove(ItemStack newStack, int index)
    {
        updateInternalState(index);
        this.onCraftMatrixChanged(this, null);

        if (index == OUTPUT_SLOT)
        {
            playCraftingSound(owner.getPos(), owner.getWorld());
        }
        
        return newStack;
    }
    
    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return postRemove(ItemStackHelper.getAndSplit(this.itemStacks, index, count), index);
    }
    
    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return postRemove(ItemStackHelper.getAndRemove(this.itemStacks, index), index);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        setInventorySlotContents(index, stack, true);
    }

    public void setInventorySlotContents(int index, ItemStack stack, boolean notify)
    {
        if (stack == null)
        {
            stack = ItemStack.EMPTY;
        }

        if (index >= 0 && index < this.itemStacks.size())
        {
            this.itemStacks.set(index, stack);
            markDirty();
            if (notify)
            {
                this.onCraftMatrixChanged(this, null);
            }
        }
    }

    
    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int getInventoryStackLimit()
    {
        return STACK_LIMIT;
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        BlockPos pos = getPos();

        return player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= USE_RANGE;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        this.itemStacks.clear();
    }

    @Override
    public void markDirty()
    {
        owner.markDirty();
    }

    
    // ----------------------------------------------------------------------
    // IItemHandler

    // this avoids a lot of boilerplate code, at expense of another object and indirection
    protected IItemHandlerModifiable itemHandler;

    @Override
    public IItemHandlerModifiable handler()
    {
        if (itemHandler == null)
        {
            itemHandler = new InvWrapper(this);
        }

        return itemHandler;
    }

    // ----------------------------------------------------------------------
    /// IInteractionObject

    abstract public String getGuiID();
    // { return Integer.toString(GuiHandlerPantograph.myGuiID); }   
    
    // ----------------------------------------------------------------------
    /// ITooltipProvider

    @Override
    public void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
    {
        String key = "misc.wumpleutil.tooltip.targetcrafting.inactive"; // Inactive
        
        if (isActive())
        {
            key = "misc.wumpleutil.tooltip.targetcrafting.active"; // Active
        }
        
        tips.add(new TextComponentTranslation(key, getOutputItemCount()).getUnformattedText());
        
        if (advanced)
        {
            // Do advanced tooltip
        }
    }
    
    // ----------------------------------------------------------------------
    // Examples

    /*
    boolean checkCreate(World worldIn)
    {
        ItemStack targetStack = itemStacks.get(TARGET_SLOT);
        List<ItemStack> inputs = itemStacks.subList(0, INPUT_SLOTS);
        
        return checkCreateRect(worldIn, targetStack, inputs) != null;
    }

    ItemStack doCreate(World worldIn)
    {
        ItemStack targetStack = itemStacks.get(TARGET_SLOT);
        List<ItemStack> inputs = itemStacks.subList(0, INPUT_SLOTS);

        return doCreate(worldIn, targetStack, inputs);
    }
    
    @Nullable
    protected ItemStack craftIt(World world)
    {
        if (checkCreate(world))
        {
            return doCreate(world);
        }
        
        return null;
    }
    */
    
    @Nullable
    abstract protected ItemStack craftIt(World world);
    
    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn, BiConsumer<Integer, ItemStack> updater)
    {
        World world = getWorld();
        if (!world.isRemote)
        {
            ItemStack craftingResult = craftIt(world);
            
            if (craftingResult == null)
            {
                craftingResult = ItemStack.EMPTY;
            }
            
            setInventorySlotContents(OUTPUT_SLOT, craftingResult, false);
            
            // Note: SlotInput will force a SlotOutput.onSlotChanged() to send output slot update to client

            // Another update method:
            if (updater != null)
            {
                updater.accept(OUTPUT_SLOT, craftingResult);
            }
        }
    }
}
