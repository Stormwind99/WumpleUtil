package com.wumple.util.xchest2;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class XChestTileEntity extends LockableLootTileEntity implements IChestLid, ITickableTileEntity
{
	private NonNullList<ItemStack> chestContents = NonNullList.withSize(27, ItemStack.EMPTY);
	protected float lidAngle;
	protected float prevLidAngle;
	protected int numPlayersUsing;
	private int ticksSinceSync;
	private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandlerModifiable> chestHandler;

	protected XChestTileEntity(TileEntityType<?> typeIn)
	{
		super(typeIn);
	}

	public XChestTileEntity()
	{
		this(TileEntityType.CHEST);
	}

	public int getSizeInventory()
	{
		return 27;
	}

	public boolean isEmpty()
	{
		for (ItemStack itemstack : this.chestContents)
		{
			if (!itemstack.isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	protected ITextComponent getDefaultName()
	{
		return new TranslationTextComponent("container.chest");
	}
	
	protected SoundEvent getOpenSoundEvent()
	{
		return SoundEvents.BLOCK_CHEST_OPEN;
	}
	
	protected void playOpenSound()
	{
		this.playSound(getOpenSoundEvent());
	}
	
	protected SoundEvent getCloseSoundEvent()
	{
		return SoundEvents.BLOCK_CHEST_CLOSE;
	}
	
	protected void playCloseSound()
	{
		this.playSound(getCloseSoundEvent());
	}

	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(compound))
		{
			ItemStackHelper.loadAllItems(compound, this.chestContents);
		}

	}

	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		if (!this.checkLootAndWrite(compound))
		{
			ItemStackHelper.saveAllItems(compound, this.chestContents);
		}

		return compound;
	}

	public void tick()
	{
		int i = this.pos.getX();
		int j = this.pos.getY();
		int k = this.pos.getZ();
		++this.ticksSinceSync;
		this.numPlayersUsing = calculatePlayersUsingSync(this.world, this, this.ticksSinceSync, i, j, k,
				this.numPlayersUsing);
		this.prevLidAngle = this.lidAngle;
		float f = 0.1F;
		if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F)
		{
			playOpenSound();
		}

		if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F)
		{
			float f1 = this.lidAngle;
			if (this.numPlayersUsing > 0)
			{
				this.lidAngle += 0.1F;
			}
			else
			{
				this.lidAngle -= 0.1F;
			}

			if (this.lidAngle > 1.0F)
			{
				this.lidAngle = 1.0F;
			}

			float f2 = 0.5F;
			if (this.lidAngle < 0.5F && f1 >= 0.5F)
			{
				playCloseSound();
			}

			if (this.lidAngle < 0.0F)
			{
				this.lidAngle = 0.0F;
			}
		}

	}

	public int calculatePlayersUsingSync(World p_213977_0_, LockableTileEntity p_213977_1_, int p_213977_2_,
			int p_213977_3_, int p_213977_4_, int p_213977_5_, int p_213977_6_)
	{
		if (!p_213977_0_.isRemote && p_213977_6_ != 0
				&& (p_213977_2_ + p_213977_3_ + p_213977_4_ + p_213977_5_) % 200 == 0)
		{
			p_213977_6_ = calculatePlayersUsing(p_213977_0_, p_213977_1_, p_213977_3_, p_213977_4_, p_213977_5_);
		}

		return p_213977_6_;
	}

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
				if (iinventory == p_213976_1_)
				{
					++i;
				}
			}
		}

		return i;
	}

	protected void playSound(SoundEvent soundIn)
	{
		double d0 = (double) this.pos.getX() + 0.5D;
		double d1 = (double) this.pos.getY() + 0.5D;
		double d2 = (double) this.pos.getZ() + 0.5D;

		this.world.playSound((PlayerEntity) null, d0, d1, d2, soundIn, SoundCategory.BLOCKS, 0.5F,
				this.world.rand.nextFloat() * 0.1F + 0.9F);
	}

	public boolean receiveClientEvent(int id, int type)
	{
		if (id == 1)
		{
			this.numPlayersUsing = type;
			return true;
		}
		else
		{
			return super.receiveClientEvent(id, type);
		}
	}

	public void openInventory(PlayerEntity player)
	{
		if (!player.isSpectator())
		{
			if (this.numPlayersUsing < 0)
			{
				this.numPlayersUsing = 0;
			}

			++this.numPlayersUsing;
			this.onOpenOrClose();
		}

	}

	public void closeInventory(PlayerEntity player)
	{
		if (!player.isSpectator())
		{
			--this.numPlayersUsing;
			this.onOpenOrClose();
		}

	}

	protected void onOpenOrClose()
	{
		Block block = this.getBlockState().getBlock();
		if (block instanceof XChestBlock)
		{
			this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
			this.world.notifyNeighborsOfStateChange(this.pos, block);
		}

	}

	protected NonNullList<ItemStack> getItems()
	{
		return this.chestContents;
	}

	protected void setItems(NonNullList<ItemStack> itemsIn)
	{
		this.chestContents = itemsIn;
	}

	@OnlyIn(Dist.CLIENT)
	public float getLidAngle(float partialTicks)
	{
		return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
	}

	public static int getPlayersUsing(IBlockReader reader, BlockPos posIn)
	{
		BlockState blockstate = reader.getBlockState(posIn);
		if (blockstate.hasTileEntity())
		{
			TileEntity tileentity = reader.getTileEntity(posIn);
			if (tileentity instanceof XChestTileEntity)
			{
				return ((XChestTileEntity) tileentity).numPlayersUsing;
			}
		}

		return 0;
	}

	public static void swapContents(XChestTileEntity chest, XChestTileEntity otherChest)
	{
		NonNullList<ItemStack> nonnulllist = chest.getItems();
		chest.setItems(otherChest.getItems());
		otherChest.setItems(nonnulllist);
	}

	protected Container createMenu(int id, PlayerInventory player)
	{
		return ChestContainer.createGeneric9X3(id, player, this);
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		if (this.chestHandler != null)
		{
			this.chestHandler.invalidate();
			this.chestHandler = null;
		}
	}

	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
			net.minecraftforge.common.capabilities.Capability<T> cap, Direction side)
	{
		if (!this.removed && cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if (this.chestHandler == null)
			{
				this.chestHandler = net.minecraftforge.common.util.LazyOptional.of(this::createHandler);
			}
			return this.chestHandler.cast();
		}
		return super.getCapability(cap, side);
	}

	protected net.minecraftforge.items.IItemHandlerModifiable createHandler()
	{
		return new net.minecraftforge.items.wrapper.InvWrapper(this);
	}

	@Override
	public void remove()
	{
		super.remove();
		if (chestHandler != null)
			chestHandler.invalidate();
	}
}