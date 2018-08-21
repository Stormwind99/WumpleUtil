package com.wumple.util.capability.targetcrafting.container;

import com.wumple.util.capability.targetcrafting.IContainerCraftingOwner;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiCrafting extends GuiContainer
{
    protected static final ResourceLocation guiTextures = new ResourceLocation("wumpleutil", "textures/gui/targetcrafting.png");
    protected IContainerCraftingOwner owner;

    public GuiCrafting(InventoryPlayer inventory, IContainerCraftingOwner ownerIn)
    {
        super(ownerIn.createContainer(inventory));
        owner = ownerIn;
    }
    
    protected ResourceLocation getGuiTextures()
    {
        return guiTextures;
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) 
    {
    	super.drawScreen(mouseX, mouseY, partialTicks);
    	this.renderHoveredToolTip(mouseX, mouseY);
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
    	GlStateManager.pushAttrib();

        String name = this.owner.hasCustomName() ? this.owner.getName() : I18n.format(this.owner.getName(), new Object[0]);
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2, 4210752);

        GlStateManager.popAttrib();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
    	GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(getGuiTextures());
        int halfW = (this.width - this.xSize) / 2;
        int halfH = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(halfW, halfH, 0, 0, this.xSize, this.ySize);
    }
}

