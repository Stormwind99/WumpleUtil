package com.wumple.util.xcartography;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wumple.util.xmap.XMapAPI;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class XCartographyScreen extends ContainerScreen<XCartographyContainer>
{
	protected static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("minecraft",
			"textures/gui/container/cartography_table.png");

	public XCartographyScreen(XCartographyContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);
	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground(); // TUT?
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredToolTip(p_render_1_, p_render_2_);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.font.drawString(this.title.getFormattedText(), 8.0F, 4.0F, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F,
				(float) (this.ySize - 96 + 2), 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		this.renderBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(CONTAINER_TEXTURE);
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i, j, 0, 0, this.xSize, this.ySize);
		
		drawMapSpecial();
	}
	
	protected void drawMapSpecial()
	{
		int i = this.guiLeft;
		int j = this.guiTop;
		
		ItemStack itemstack1 = this.container.getSlot(1).getStack();
		boolean isMap = container.isCopyInput(itemstack1);
		boolean isPaper = container.isScalingInput(itemstack1);
		boolean isGlassPane = container.isLockingInput(itemstack1);
		ItemStack itemstack = this.container.getSlot(0).getStack();
		boolean isLocked = false;
		
		MapData mapdata;
		if (container.isAllowedInSlot0In(itemstack))
		{
			mapdata = XMapAPI.getInstance().getMapDataIfExists(itemstack, this.minecraft.world);
			if (mapdata != null)
			{
				// draw lock?
				if (mapdata.locked)
				{
					isLocked = true;
					if (isPaper || isGlassPane)
					{
						this.blit(i + 35, j + 31, this.xSize + 50, 132, 28, 21);
					}
				}

				// draw zoom-out rectangle?
				byte newMapScale = (byte)(mapdata.scale+1);
				if (isPaper && !XMapAPI.getInstance().isMapScaleValid(newMapScale)) // WAS mapdata.scale >= 4)
				{
					isLocked = true;
					this.blit(i + 35, j + 31, this.xSize + 50, 132, 28, 21);
				}
			}
		}
		else
		{
			mapdata = null;
		}

		this.drawMap(mapdata, isMap, isPaper, isGlassPane, isLocked);
	}

	protected void drawMap(@Nullable MapData mapDataIn, boolean isMap, boolean isPaper, boolean isGlassPane,
			boolean isLocked)
	{
		if (isPaper && !isLocked)
		{
			drawMapZoomOut(mapDataIn);
		}
		else if (isMap)
		{
			drawMapCopyMap(mapDataIn);
		}
		else if (isGlassPane)
		{
			drawMapLockMap(mapDataIn);
		}
		else
		{
			drawMapDefault(mapDataIn);
		}
	}

	protected void drawMapItem(@Nullable MapData mapDataIn, int x, int y, float scale)
	{
		if (mapDataIn != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float) x, (float) y, 1.0F);
			GlStateManager.scalef(scale, scale, 1.0F);
			this.minecraft.gameRenderer.getMapItemRenderer().renderMap(mapDataIn, true);
			GlStateManager.popMatrix();
		}
	}

	// ---

	protected void drawMapZoomOut(MapData mapDataIn)
	{
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i + 67, j + 13, this.xSize, 66, 66, 66);
		this.drawMapItem(mapDataIn, i + 85, j + 31, 0.226F);
	}

	protected void drawMapCopyMap(MapData mapDataIn)
	{
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i + 67 + 16, j + 13, this.xSize, 132, 50, 66);
		this.drawMapItem(mapDataIn, i + 86, j + 16, 0.34F);
		this.minecraft.getTextureManager().bindTexture(CONTAINER_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.0F, 1.0F);
		this.blit(i + 67, j + 13 + 16, this.xSize, 132, 50, 66);
		this.drawMapItem(mapDataIn, i + 70, j + 32, 0.34F);
		GlStateManager.popMatrix();
	}
	
	protected void drawMapLockMap(MapData mapDataIn)
	{
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i + 67, j + 13, this.xSize, 0, 66, 66);
		this.drawMapItem(mapDataIn, i + 71, j + 17, 0.45F);
		this.minecraft.getTextureManager().bindTexture(CONTAINER_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.0F, 1.0F);
		this.blit(i + 66, j + 12, 0, this.ySize, 66, 66);
		GlStateManager.popMatrix();
	}
	
	protected void drawMapDefault(MapData mapDataIn)
	{
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i + 67, j + 13, this.xSize, 0, 66, 66);
		this.drawMapItem(mapDataIn, i + 71, j + 17, 0.45F);
	}

}
