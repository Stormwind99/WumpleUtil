package com.wumple.util.nameable;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;

abstract public class NameableTileEntity extends TileEntity implements IWorldNameable, INameable
{
    protected String customName;

    // ----------------------------------------------------------------------
    // IWorldNameable

    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customName : getRealName();
    }

    @Override
    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public void setCustomName(String nameIn)
    {
        this.customName = nameIn;
    }

    @Override
    public ITextComponent getDisplayName()
    {   
        return (ITextComponent) (this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName()));
    }

    @Override
    abstract public String getRealName();

    // ----------------------------------------------------------------------
    // INBTSerializable

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }

        return compound;
    }

    // ----------------------------------------------------------------------
    // Update custom name to client
    // from https://github.com/Chisel-Team/Chisel/blob/1.10/dev/src/main/java/team/chisel/common/block/TileAutoChisel.java

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound ret = super.getUpdateTag();
        if (hasCustomName())
        {
            ret.setString("customName", getName());
        }
        return ret;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        handleUpdateTag(pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        if (tag.hasKey("customName"))
        {
            this.customName = tag.getString("customName");
        }
        super.handleUpdateTag(tag);
    }
}
