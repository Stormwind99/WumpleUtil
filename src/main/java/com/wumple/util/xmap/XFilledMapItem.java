package com.wumple.util.xmap;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.wumple.util.map.MapUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.IPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Extends and modifies Minecraft's net.minecraft.item.FilledMapItem
 * 
 * Unfortunately FilledMapItem was not designed for extension, so mostly this is
 * copied code (including methods that call static functions)
 */
public class XFilledMapItem extends FilledMapItem implements IXFilledMapItem
{
	public static final String ID = "wumpleutil:xmap_filled";
	public static final int sizeX = 128;
	public static final int sizeZ = 128;
	public static final int minX = 0;
	public static final int minZ = 0;
	public static final int limitX = minX + sizeX;
	public static final int limitZ = minX + sizeZ;

	@Override
	public String getID()
	{
		return ID;
	}

	public XFilledMapItem(Item.Properties builder)
	{
		super(builder.maxStackSize(1));
	}

	// IXFilledMapItem

	@Override
	public boolean isAMapScaleValid(byte scale)
	{
		return isMapScaleValid(scale);
	}

	/// Creates new ItemStack of XFilledMapItem at (worldX, worldZ) with given params (along with new registered XMapData)
	@Override
	public ItemStack setupANewMap(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition,
			boolean unlimitedTracking)
	{
		return setupNewMap(worldIn, worldZ, worldZ, scale, unlimitedTracking, unlimitedTracking);
	}

	/// Creates and registers a new XMapData at (x,z) with given params
	protected MapData createMyMapData(ItemStack stack, World worldIn, int x, int z, int scale, boolean trackingPosition,
			boolean unlimitedTracking, DimensionType dimensionTypeIn)
	{
		return createMapData(stack, worldIn, x, z, scale, trackingPosition, unlimitedTracking, dimensionTypeIn);
	}

	// adapted from public static ItemStack func_219992_b(World worldIn, ItemStack stack)
	@Override
	public ItemStack copyMapShallow(ItemStack stack)
	{
		// this will copy stack's NBT which contains the map id
		return stack.copy();
	}
	
	// adapted from public static ItemStack func_219992_b(World worldIn, ItemStack stack)
	@Override
	public ItemStack copyMapDeep(ItemStack stack, World worldIn)
	{
		MapData mapdata = getMyMapData(stack, worldIn);
		if (mapdata != null)
		{
			ItemStack itemstack = stack.copy();
			MapData mapdata1 = createMyMapData(itemstack, worldIn, 0, 0, mapdata.scale, mapdata.trackingPosition,
					mapdata.unlimitedTracking, mapdata.dimension);
			mapdata1.copyFrom(mapdata);
			mapdata1.locked = mapdata.locked;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	@Override
	/// Fill in map pixels for viewer
	public void fillMapData(World worldIn, Entity viewer, MapData data)
	{
		updateMapDataArea(worldIn, viewer, data, minX, minZ, limitX, limitZ, null);
	}

	/// Fill in map pixels for viewer
	public boolean fillMapData(World worldIn, Entity viewer, ItemStack itemstack)
	{
		// get the map data
		MapData mapData = getMapData(itemstack, worldIn);

		fillMapData(worldIn, viewer, mapData);

		return (mapData != null);
	}

	// Other public methods (for example, for re-direction to MC's static methods)

	/// gets existing MapData from the World, null if doesn't exist
	@Override
	@Nullable
	public MapData getMyData(ItemStack stack, World worldIn)
	{
		return getData(stack, worldIn);
	}

	/// gets existing MapData or (on server) creates new MapData if it doesn't exist
	@Override
	@Nullable
	public MapData getMyMapData(ItemStack stack, World worldIn)
	{
		return getMapData(stack, worldIn);
	}

	protected static boolean isMapScaleValid(byte scale)
	{
		return (scale >= 0) && (scale <= 4);
	}
	
	// MAYBE
	// Duplicate Items.FILLED_MAP advanced tooltip behavior (map id) from special code in ItemStack.doTooltip()

	// ---------------------------------------------------------------------------------------
	// Copied from FilledMapItem
	//
	// PORTED means changed from parent
	// BADPARENT means same as parent, but calls static methods so copied here to copy modified static methods
	// PRIVPARENT means same as parent but private and called here, so copied here
	// PARENT means same as parent - probably safe to delete and let parent version be called

	/*
	// PARENT
	public static int getColor(ItemStack stack)
	public static String getMapName(int mapId)
	public static int getMapId(ItemStack stack)
	public static MapData getMapData(ItemStack stack, World worldIn) -> calls non-static getCustomMapData()
	*/

	// PORTED
	/*
	 * Creates new ItemStack of XFilledMapItem at (worldX, worldZ) with given params (along with new registered XMapData)
	 */
	public static ItemStack setupNewMap(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition,
			boolean unlimitedTracking)
	{
		ItemStack itemstack = new ItemStack(XMapAPI.getInstance().getFilledMapItem());
		createMapData(itemstack, worldIn, worldX, worldZ, scale, trackingPosition, unlimitedTracking,
				worldIn.dimension.getType());
		return itemstack;
	}

	// BADPARENT
	@Nullable
	public static MapData getData(ItemStack stack, World worldIn)
	{
		// Hack around/from ServerWorld.getMapData
		if (worldIn instanceof ServerWorld)
		{
			String mapName = getMapName(getMapId(stack));
			
			return worldIn.getServer().getWorld(DimensionType.OVERWORLD).getSavedData().get(() -> {
				return XMapAPI.getInstance().createMapData(mapName);
			}, mapName);
		}
		else
		{
			// ClientWorld.getMapData looks okay, so use original code
			return worldIn.getMapData(getMapName(getMapId(stack)));
		}

		// TODO what about ClientPlayNetHandler.handleMap calling new MapData?  It doesn't appear to cause any problems
	}

	// PORTED
	@Nullable
	protected MapData getCustomMapData(ItemStack stack, World worldIn)
	{
		MapData mapdata = getMyData(stack, worldIn);
		if (mapdata == null && !worldIn.isRemote)
		{
			mapdata = createMapData(stack, worldIn, worldIn.getWorldInfo().getSpawnX(),
					worldIn.getWorldInfo().getSpawnZ(), 3, false, false, worldIn.dimension.getType());
		}

		return mapdata;
	}

	// PORTED
	/*
	 * Creates and registers a new XMapData at (x,z) with given params
	 */
	private static MapData createMapData(ItemStack stack, World worldIn, int x, int z, int scale,
			boolean trackingPosition, boolean unlimitedTracking, DimensionType dimensionTypeIn)
	{
		int i = worldIn.getNextMapId();
		String mapName = getMapName(i);
		MapData mapdata = XMapAPI.getInstance().createMapData(mapName);
		mapdata.func_212440_a(x, z, scale, trackingPosition, unlimitedTracking, dimensionTypeIn);
		worldIn.registerMapData(mapdata);
		stack.getOrCreateTag().putInt("map", i);
		return mapdata;
	}

	// PORTED AND SPLIT
	@Override
	public void updateMapData(World worldIn, Entity viewer, MapData data)
	{
		int scaleNum = 1 << data.scale; // blocks per pixel?
		int xCenter = data.xCenter;
		int zCenter = data.zCenter;
		int viewerPixelX = MathHelper.floor(viewer.getPosX() - (double) xCenter) / scaleNum + 64;
		int viewerPixelZ = MathHelper.floor(viewer.getPosZ() - (double) zCenter) / scaleNum + 64;
		int pixelViewRangeTemp = 128 / scaleNum;

		if (worldIn.dimension.isNether())
		{
			pixelViewRangeTemp /= 2;
		}

		// ADDED
		final int pixelViewRange = Math.max(1, pixelViewRangeTemp);

		int startPixelX = viewerPixelX - pixelViewRange + 1;
		int endPixelX = viewerPixelX + pixelViewRange;
		int startPixelZ = viewerPixelZ - pixelViewRange - 1;
		int endPixelZ = viewerPixelZ + pixelViewRange;

		BiFunction<Integer, Integer, Boolean> usePixel = (pixelX, pixelZ) -> {
			int i2 = pixelX - viewerPixelX;
			int j2 = pixelZ - viewerPixelZ;
			boolean flag1 = i2 * i2 + j2 * j2 > (pixelViewRange - 2) * (pixelViewRange - 2);

			boolean flag2 = (pixelZ >= 0 && i2 * i2 + j2 * j2 < pixelViewRange * pixelViewRange
					&& (!flag1 || (pixelX + pixelZ & 1) != 0));

			return flag2;
		};

		updateMapDataArea(worldIn, viewer, data, startPixelX, startPixelZ, endPixelX, endPixelZ, usePixel);
	}

	// PORTED AND SPLIT
	/*
	 * Fill in map pixels for viewer
	 */
	public void updateMapDataArea(World worldIn, Entity viewer, MapData data, int startPixelX, int startPixelZ,
			int endPixelX, int endPixelZ, @Nullable BiFunction<Integer, Integer, Boolean> usePixel)
	{
		if (worldIn.dimension.getType() == data.dimension && viewer instanceof PlayerEntity)
		{
			int i = 1 << data.scale; // i is scaleNum (blocks per pixel?)
			int j = data.xCenter; // j is xCenter
			int k = data.zCenter; // k is zCenter

			/*
			// WAS
			int l = MathHelper.floor(viewer.posX - (double)j) / i + 64;
			int i1 = MathHelper.floor(viewer.posZ - (double)k) / i + 64;
			int j1 = 128 / i;
			if (worldIn.dimension.isNether()) {
			  j1 /= 2;
			}
			// END WAS
			*/

			MapData.MapInfo mapdata$mapinfo = data.getMapInfo((PlayerEntity) viewer);
			++mapdata$mapinfo.step;
			// WAS boolean flag = false;

			// k1 is pixelX
			// REPLACED for(int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
			for (int k1 = startPixelX; k1 < endPixelX; ++k1)
			{
				// REPLACED if ((k1 & 15) == (mapdata$mapinfo.step & 15) || flag) { 
				if (true)
				{
					// WAS flag = false;
					double d0 = 0.0D;

					// l1 is pixelZ
					// REPLACED for(int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
					for (int l1 = startPixelZ; l1 < endPixelZ; ++l1)
					{
						if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128)
						{
							// WAS int i2 = k1 - l;
							// WAS int j2 = l1 - i1;
							// WAS boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
							int k2 = (j / i + k1 - 64) * i;
							int l2 = (k / i + l1 - 64) * i;
							Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
							Chunk chunk = worldIn.getChunkAt(new BlockPos(k2, 0, l2));
							if (!chunk.isEmpty())
							{
								ChunkPos chunkpos = chunk.getPos();
								int i3 = k2 & 15;
								int j3 = l2 & 15;
								int k3 = 0;
								double d1 = 0.0D;
								if (worldIn.dimension.isNether())
								{
									int l3 = k2 + l2 * 231871;
									l3 = l3 * l3 * 31287121 + l3 * 11;
									if ((l3 >> 20 & 1) == 0)
									{
										multiset.add(
												Blocks.DIRT.getDefaultState().getMaterialColor(worldIn, BlockPos.ZERO),
												10);
									}
									else
									{
										multiset.add(
												Blocks.STONE.getDefaultState().getMaterialColor(worldIn, BlockPos.ZERO),
												100);
									}

									d1 = 100.0D;
								}
								else
								{
									BlockPos.Mutable blockpos$mutableblockpos1 = new BlockPos.Mutable();
									BlockPos.Mutable blockpos$mutableblockpos = new BlockPos.Mutable();

									for (int i4 = 0; i4 < i; ++i4)
									{
										for (int j4 = 0; j4 < i; ++j4)
										{
											// REPLACED int k4 = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;                                   
											int x = i4 + i3;
											int z = j4 + j3;
											int chunkIndex = z << 4 | x;
											int k4 = (chunkIndex >= 256) ? 255
													: chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
											// END REPLACED

											BlockState blockstate;
											if (k4 <= 1)
											{
												blockstate = Blocks.BEDROCK.getDefaultState();
											}
											else
											{
												while (true)
												{
													--k4;
													blockpos$mutableblockpos1.setPos(chunkpos.getXStart() + i4 + i3, k4,
															chunkpos.getZStart() + j4 + j3);
													blockstate = chunk.getBlockState(blockpos$mutableblockpos1);
													if (blockstate.getMaterialColor(worldIn,
															blockpos$mutableblockpos1) != MaterialColor.AIR || k4 <= 0)
													{
														break;
													}
												}

												if (k4 > 0 && !blockstate.getFluidState().isEmpty())
												{
													int l4 = k4 - 1;
													blockpos$mutableblockpos.setPos(blockpos$mutableblockpos1);

													while (true)
													{
														blockpos$mutableblockpos.setY(l4--);
														BlockState blockstate1 = chunk
																.getBlockState(blockpos$mutableblockpos);
														++k3;
														if (l4 <= 0 || blockstate1.getFluidState().isEmpty())
														{
															break;
														}
													}

													blockstate = this.func_211698_a(worldIn, blockstate,
															blockpos$mutableblockpos1);
												}
											}

											data.removeStaleBanners(worldIn, chunkpos.getXStart() + i4 + i3,
													chunkpos.getZStart() + j4 + j3);
											d1 += (double) k4 / (double) (i * i);
											multiset.add(
													blockstate.getMaterialColor(worldIn, blockpos$mutableblockpos1));
										}
									}
								}

								k3 = k3 / (i * i);
								double d2 = (d1 - d0) * 4.0D / (double) (i + 4)
										+ ((double) (k1 + l1 & 1) - 0.5D) * 0.4D;
								int i5 = 1;
								if (d2 > 0.6D)
								{
									i5 = 2;
								}

								if (d2 < -0.6D)
								{
									i5 = 0;
								}

								MaterialColor materialcolor = Iterables
										.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.AIR);
								if (materialcolor == MaterialColor.WATER)
								{
									d2 = (double) k3 * 0.1D + (double) (k1 + l1 & 1) * 0.2D;
									i5 = 1;
									if (d2 < 0.5D)
									{
										i5 = 2;
									}

									if (d2 > 0.9D)
									{
										i5 = 0;
									}
								}

								d0 = d1;

								// REPLACED if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
								if ((usePixel == null) || usePixel.apply(k1, l1))
								{
									// REPLACED byte b0 = data.colors[k1 + l1 * 128];
									byte b0 = data.colors[k1 + l1 * sizeX];
									byte b1 = (byte) (materialcolor.colorIndex * 4 + i5);
									if (b0 != b1)
									{
										// REPLACED data.colors[k1 + l1 * 128] = b1;
										data.colors[k1 + l1 * sizeX] = b1;
										data.updateMapData(k1, l1);
										// WAS flag = true;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	// PRIVPARENT
	protected BlockState func_211698_a(World worldIn, BlockState state, BlockPos pos)
	{
		IFluidState ifluidstate = state.getFluidState();
		return !ifluidstate.isEmpty() && !state.isSolidSide(worldIn, pos, Direction.UP) ? ifluidstate.getBlockState() : state;
	}

	// PRIVPARENT
	protected static boolean func_195954_a(Biome[] p_195954_0_, int p_195954_1_, int p_195954_2_, int p_195954_3_)
	{
		return p_195954_0_[p_195954_2_ * p_195954_1_ + p_195954_3_ * p_195954_1_ * 128 * p_195954_1_]
				.getDepth() >= 0.0F;
	}

	// BADPARENT
	/**
	 * Draws ambiguous landmasses representing unexplored terrain onto a treasure
	 * map
	 */
	public static void renderBiomePreviewMap(World worldIn, ItemStack map)
	{
		MapData mapdata = getMapData(map, worldIn);
		if (mapdata != null)
		{
			if (worldIn.dimension.getType() == mapdata.dimension)
			{
				int i = 1 << mapdata.scale;
				int j = mapdata.xCenter;
				int k = mapdata.zCenter;
				AbstractChunkProvider chunkProvider = worldIn.getChunkProvider();
				if (!(chunkProvider instanceof ServerChunkProvider)) {
					return;
				}
				ServerChunkProvider serverChunkProvider = (ServerChunkProvider) chunkProvider;
				Set<Biome> biomes = serverChunkProvider.getChunkGenerator().getBiomeProvider().getBiomes((j / i - 64) * i, (k / i - 64) * i, 128 * i, 128 * i);
				Biome[] abiome = (Biome[]) biomes.toArray();

				for (int l = 0; l < 128; ++l)
				{
					for (int i1 = 0; i1 < 128; ++i1)
					{
						if (l > 0 && i1 > 0 && l < 127 && i1 < 127)
						{
							Biome biome = abiome[l * i + i1 * i * 128 * i];
							int j1 = 8;
							if (func_195954_a(abiome, i, l - 1, i1 - 1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l - 1, i1 + 1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l - 1, i1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l + 1, i1 - 1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l + 1, i1 + 1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l + 1, i1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l, i1 - 1))
							{
								--j1;
							}

							if (func_195954_a(abiome, i, l, i1 + 1))
							{
								--j1;
							}

							int k1 = 3;
							MaterialColor materialcolor = MaterialColor.AIR;
							if (biome.getDepth() < 0.0F)
							{
								materialcolor = MaterialColor.ADOBE;
								if (j1 > 7 && i1 % 2 == 0)
								{
									k1 = (l + (int) (MathHelper.sin((float) i1 + 0.0F) * 7.0F)) / 8 % 5;
									if (k1 == 3)
									{
										k1 = 1;
									}
									else if (k1 == 4)
									{
										k1 = 0;
									}
								}
								else if (j1 > 7)
								{
									materialcolor = MaterialColor.AIR;
								}
								else if (j1 > 5)
								{
									k1 = 1;
								}
								else if (j1 > 3)
								{
									k1 = 0;
								}
								else if (j1 > 1)
								{
									k1 = 0;
								}
							}
							else if (j1 > 0)
							{
								materialcolor = MaterialColor.BROWN;
								if (j1 > 3)
								{
									k1 = 1;
								}
								else
								{
									k1 = 3;
								}
							}

							if (materialcolor != MaterialColor.AIR)
							{
								mapdata.colors[l + i1 * 128] = (byte) (materialcolor.colorIndex * 4 + k1);
								mapdata.updateMapData(l, i1);
							}
						}
					}
				}

			}
		}
	}

	// BADPARENT
	/**
	 * Called each tick as long the item is on a player inventory. Uses by maps to
	 * check if is on a player hand and update it's contents.
	 */
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if (!worldIn.isRemote)
		{
			MapData mapdata = getMapData(stack, worldIn);
			if (mapdata != null)
			{
				if (entityIn instanceof PlayerEntity)
				{
					PlayerEntity playerentity = (PlayerEntity) entityIn;
					mapdata.updateVisiblePlayers(playerentity, stack);
				}

				if (!mapdata.locked && (isSelected
						|| entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).getHeldItemOffhand() == stack))
				{
					this.updateMapData(worldIn, entityIn, mapdata);
				}

			}
		}
	}

	// BADPARENT
	@Nullable
	public IPacket<?> getUpdatePacket(ItemStack stack, World worldIn, PlayerEntity player)
	{
		return getMapData(stack, worldIn).getMapPacket(stack, worldIn, player);
	}

	// BADPARENT
	/**
	 * Called when item is crafted/smelted. Used only by maps so far.
	 */
	@Override
	public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn)
	{
		int scaling = MapUtil.extractMapScaleDirection(stack);

		if (scaling != 0)
		{
			scaleMap(stack, worldIn, scaling);
		}
	}

	// PORTED
	protected static void scaleMap(ItemStack stack, World world, int scaleChange)
	{
		MapData mapdata = getMapData(stack, world);
		if (mapdata != null)
		{
			createMapData(stack, world, mapdata.xCenter, mapdata.zCenter,
					MathHelper.clamp(mapdata.scale + scaleChange, 0, XMapAPI.getInstance().getMaxScale()),
					mapdata.trackingPosition, mapdata.unlimitedTracking, mapdata.dimension);
		}

	}

	// BADPARENT
	@Nullable
	public static ItemStack func_219992_b(World worldIn, ItemStack stack)
	{
		MapData mapdata = getMapData(stack, worldIn);
		if (mapdata != null)
		{
			ItemStack itemstack = stack.copy();
			MapData mapdata1 = createMapData(itemstack, worldIn, 0, 0, mapdata.scale, mapdata.trackingPosition,
					mapdata.unlimitedTracking, mapdata.dimension);
			mapdata1.copyFrom(mapdata);
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	// BADPARENT
	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn)
	{
		MapData mapdata = worldIn == null ? null : getMapData(stack, worldIn);
		if (mapdata != null && mapdata.locked)
		{
			tooltip.add((new TranslationTextComponent("filled_map.locked", getMapId(stack)))
					.applyTextStyle(TextFormatting.GRAY));
		}

		if (flagIn.isAdvanced())
		{
			if (mapdata != null)
			{
				tooltip.add((new TranslationTextComponent("filled_map.id", getMapId(stack)))
						.applyTextStyle(TextFormatting.GRAY));
				tooltip.add((new TranslationTextComponent("filled_map.scale", 1 << mapdata.scale))
						.applyTextStyle(TextFormatting.GRAY));
				tooltip.add((new TranslationTextComponent("filled_map.level", mapdata.scale,
						XMapAPI.getInstance().getMaxScale())).applyTextStyle(TextFormatting.GRAY));
			}
			else
			{
				tooltip.add((new TranslationTextComponent("filled_map.unknown")).applyTextStyle(TextFormatting.GRAY));
			}
		}

	}

	// BADPARENT
	/**
	 * Called when this item is used when targetting a Block
	 */
	public ActionResultType onItemUse(ItemUseContext context)
	{
		BlockState blockstate = context.getWorld().getBlockState(context.getPos());
		if (blockstate.isIn(BlockTags.BANNERS))
		{
			if (!context.getWorld().isRemote)
			{
				MapData mapdata = getMapData(context.getItem(), context.getWorld());
				mapdata.tryAddBanner(context.getWorld(), context.getPos());
			}

			return ActionResultType.SUCCESS;
		}
		else
		{
			return super.onItemUse(context);
		}
	}
}
