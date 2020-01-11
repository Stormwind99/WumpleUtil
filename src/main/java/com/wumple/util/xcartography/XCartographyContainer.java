package com.wumple.util.xcartography;

import com.wumple.util.map.MapUtil;
import com.wumple.util.xmap.XMapAPI;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.storage.MapData;

/*
 * Modified for extensibility from net.minecraft.inventory.container.CartographyContainer
 */
public class XCartographyContainer extends Container
{
	// ------------------------------------------------------------------------
	// Added to CartographyContainer to make more extendible via inheritance

	protected boolean isAllowedInSlot0In(ItemStack stack)
	{
		return XMapAPI.getInstance().isFilledMap(stack);
	}

	protected boolean isAllowedInSlot1In(ItemStack stack)
	{
		return isScalingInput(stack) || isCopyInput(stack) || isLockingInput(stack);
	}

	protected boolean isLockingInput(ItemStack stack)
	{
		return stack.getItem() == Items.GLASS_PANE;
	}

	protected boolean isScalingInput(ItemStack stack)
	{
		return stack.getItem() == Items.PAPER;
	}

	protected boolean isCopyInput(ItemStack stack)
	{
		return XMapAPI.getInstance().isEmptyMap(stack);
	}

	protected boolean isMapDataLocked(MapData mapdata)
	{
		return mapdata.locked;
	}

	protected boolean isMapDataScaleable(MapData mapdata)
	{
		return (mapdata.scale < XMapAPI.getInstance().getMaxScale());
	}

	// ------------------------------------------------------------------------
	// From CartographyContainer

	protected final IWorldPosCallable worldpos;
	protected boolean onTaking;

	public final IInventory inventoryIn = new Inventory(2)
	{
		public void markDirty()
		{
			XCartographyContainer.this.onCraftMatrixChanged(this);
			super.markDirty();
		}
	};

	protected final CraftResultInventory inventoryOut = new CraftResultInventory()
	{
		public void markDirty()
		{
			XCartographyContainer.this.onCraftMatrixChanged(this);
			super.markDirty();
		}
	};

	public XCartographyContainer(ContainerType<?> type, int idIn, PlayerInventory playerInventoryIn,
			final IWorldPosCallable worldPosIn)
	{
		super(type, idIn);
		this.worldpos = worldPosIn;
		init(idIn, playerInventoryIn, worldPosIn);
	}

	public XCartographyContainer(int idIn, PlayerInventory playerInventoryIn)
	{
		this(ContainerType.CARTOGRAPHY, idIn, playerInventoryIn, IWorldPosCallable.DUMMY);
	}

	protected void init(int idIn, PlayerInventory playerInventoryIn, final IWorldPosCallable worldPosIn)
	{
		addSlot0In(idIn, playerInventoryIn, worldPosIn);
		addSlot1In(idIn, playerInventoryIn, worldPosIn);
		addSlot2Out(idIn, playerInventoryIn, worldPosIn);
		addSlotsInventory(idIn, playerInventoryIn, worldPosIn);
	}

	// inventoryIn slot #0
	protected void addSlot0In(int idIn, PlayerInventory playerInventoryIn, final IWorldPosCallable worldPosIn)
	{
		this.addSlot(new Slot(this.inventoryIn, 0, 15, 15)
		{
			public boolean isItemValid(ItemStack stack)
			{
				return isAllowedInSlot0In(stack);
			}
		});
	}

	// inventoryIn slot #1
	protected void addSlot1In(int idIn, PlayerInventory playerInventoryIn, final IWorldPosCallable worldPosIn)
	{
		this.addSlot(new Slot(this.inventoryIn, 1, 15, 52)
		{
			public boolean isItemValid(ItemStack stack)
			{
				return isAllowedInSlot1In(stack);
			}
		});
	}

	// inventoryOut slot #2
	protected void addSlot2Out(int idIn, PlayerInventory playerInventoryIn, final IWorldPosCallable worldPosIn)
	{
		this.addSlot(new Slot(this.inventoryOut, 2, 145, 39)
		{
			public boolean isItemValid(ItemStack stack)
			{
				return false;
			}

			public ItemStack decrStackSize(int amount)
			{
				ItemStack itemstack = super.decrStackSize(amount);
				ItemStack itemstack1 = XCartographyContainer.this.doIt(itemstack);

				return itemstack1;
			}

			protected void onCrafting(ItemStack stack, int amount)
			{
				this.decrStackSize(amount);
				super.onCrafting(stack, amount);
			}

			public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack)
			{
				stack.getItem().onCreated(stack, thePlayer.world, thePlayer);
				worldPosIn.consume((worldIn, posIn) -> {
					worldIn.playSound((PlayerEntity) null, posIn, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
				});
				return super.onTake(thePlayer, stack);
			}
		});
	}

	// playerInventoryIn Slot # 0 - 8
	protected void addSlotsPlayerHotbar(int idIn, PlayerInventory playerInventoryIn, final IWorldPosCallable worldPosIn)
	{
		for (int k = 0; k < 9; ++k)
		{
			this.addSlot(new Slot(playerInventoryIn, k, 8 + k * 18, 142));
		}
	}

	// playerInventoryIn Slot # 9 - 36
	protected void addSlotsPlayerInventory(int idIn, PlayerInventory playerInventoryIn,
			final IWorldPosCallable worldPosIn)
	{
		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlot(new Slot(playerInventoryIn, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
	}

	protected void addSlotsInventory(int idIn, PlayerInventory playerInventoryIn, final IWorldPosCallable worldPosIn)
	{
		addSlotsPlayerInventory(idIn, playerInventoryIn, worldPosIn);
		addSlotsPlayerHotbar(idIn, playerInventoryIn, worldPosIn);
	}

	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.worldpos, playerIn, Blocks.CARTOGRAPHY_TABLE);
	}

	public void onCraftMatrixChanged(IInventory inventoryIn)
	{
		ItemStack itemstackIn0 = this.inventoryIn.getStackInSlot(0);
		ItemStack itemStackIn1 = this.inventoryIn.getStackInSlot(1);
		ItemStack itemStackOut = this.inventoryOut.getStackInSlot(2);
		if (itemStackOut.isEmpty() || !itemstackIn0.isEmpty() && !itemStackIn1.isEmpty())
		{
			if (!itemstackIn0.isEmpty() && !itemStackIn1.isEmpty())
			{
				this.processInputs(itemstackIn0, itemStackIn1, itemStackOut);
			}
		}
		else
		{
			this.inventoryOut.removeStackFromSlot(2);
		}

	}

	// create item to show in the output slot when items are put into the input slots 
	// don't do any significant processing - that will be done in transferCraftedStack()
	protected void processInputs(ItemStack itemStackIn0, ItemStack itemStackIn1, ItemStack itemStackOut)
	{
		this.worldpos.consume((worldIn, posIn) -> {
			MapData mapdata = XMapAPI.getInstance().getMapData(itemStackIn0, worldIn);
			if (mapdata != null)
			{
				ItemStack itemstack;
				if (isScalingInput(itemStackIn1) && !isMapDataLocked(mapdata) && isMapDataScaleable(mapdata))
				{
					itemstack = itemStackIn0.copy();
					itemstack.setCount(1);
					MapUtil.mapScaleDirection(itemstack, 1);
					this.detectAndSendChanges();
				}
				else if (isLockingInput(itemStackIn1) && !isMapDataLocked(mapdata))
				{
					itemstack = itemStackIn0.copy();
					itemstack.setCount(1);
					this.detectAndSendChanges();
				}
				else
				{
					if (!isCopyInput(itemStackIn1))
					{
						this.inventoryOut.removeStackFromSlot(2);
						this.detectAndSendChanges();
						return;
					}

					itemstack = itemStackIn0.copy();
					itemstack.setCount(2);
					this.detectAndSendChanges();
				}

				if (!ItemStack.areItemStacksEqual(itemstack, itemStackOut))
				{
					this.inventoryOut.setInventorySlotContents(2, itemstack);
					this.detectAndSendChanges();
				}

			}
		});
	}

	public boolean canMergeSlot(ItemStack stack, Slot slotIn)
	{
		return false;
	}

	protected ItemStack doIt(ItemStack itemstack)
	{
		ItemStack itemstack1 = (!this.onTaking) ? transferCraftedStack(itemstack) : itemstack;

		// decrease inputs
		this.inventoryIn.decrStackSize(0, 1);
		this.inventoryIn.decrStackSize(1, 1);

		return itemstack1;
	}

	protected ItemStack transferCraftedStack(ItemStack itemstack1)
	{
		ItemStack itemstack2 = itemstack1;

		if (isLockingInput(this.inventoryIn.getStackInSlot(1)))
		{
			itemstack2 = this.worldpos.apply((worldIn, posIn) -> {
				// create locked copy of input map
				ItemStack itemstack3 = XMapAPI.getInstance().copyMapDeepLocked(this.inventoryIn.getStackInSlot(0), worldIn);
				if (itemstack3 != null)
				{
					itemstack3.setCount(1);
					return itemstack3;
				}

				return itemstack1;
			}).orElse(itemstack1);
		}

		return itemstack2;
	}

	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			ItemStack itemstack2 = itemstack1;
			Item item = itemstack1.getItem();
			itemstack = itemstack1.copy();
			// output slot (#2)
			if (index == 2)
			{
				itemstack2 = transferCraftedStack(itemstack1);

				item.onCreated(itemstack2, playerIn.world, playerIn);
				if (!this.mergeItemStack(itemstack2, 3, 39, true))
				{
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack2, itemstack);
			}
			// inventory (not #0 or #1) (or output slot #2 due to above)
			else if (index != 1 && index != 0)
			{
				if (isCopyInput(itemstack1))
				{
					if (!this.mergeItemStack(itemstack1, 0, 1, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (!isAllowedInSlot1In(itemstack1))
				{
					// inventory slot
					if (index >= 3 && index < 30)
					{
						if (!this.mergeItemStack(itemstack1, 30, 39, false))
						{
							return ItemStack.EMPTY;
						}
					}
					// hotbar slot
					else if (index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (!this.mergeItemStack(itemstack1, 1, 2, false))
				{
					return ItemStack.EMPTY;
				}
			}
			// any other slot (inventory or hotbar)
			else if (!this.mergeItemStack(itemstack1, 3, 39, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack2.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}

			slot.onSlotChanged();
			if (itemstack2.getCount() == itemstack.getCount())
			{
				return ItemStack.EMPTY;
			}

			this.onTaking = true;
			slot.onTake(playerIn, itemstack2);
			this.onTaking = false;
			this.detectAndSendChanges();
		}

		return itemstack;
	}

	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		this.inventoryOut.removeStackFromSlot(2);
		this.worldpos.consume((worldIn, posIn) -> {
			this.clearContainer(playerIn, playerIn.world, this.inventoryIn);
		});
	}
}