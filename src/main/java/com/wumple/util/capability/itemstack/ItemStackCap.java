package com.wumple.util.capability.itemstack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

abstract public class ItemStackCap implements IItemStackCap
{
    /*
    // in derived class, do:
    
    // The {@link Capability} instance
    @CapabilityInject(IRot.class)
    public static final Capability<IRot> CAPABILITY = null;
    public static final EnumFacing DEFAULT_FACING = null;

    // IDs of the capability
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "rot");
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IRot.class, new RotStorage(), () -> new Rot());

        CapabilityContainerListenerManager.registerListenerFactory(ContainerListenerRot::new);
    }
    */

    // what itemstack is this cap attached to?
    protected ItemStack owner = null;
    // silly unique id to increment and put into NBT to force an update to client
    protected byte forceId = 0;

    public ItemStackCap()
    {
    }

    public ItemStackCap(ItemStackCap other)
    {
        owner = other.owner;
        forceId = other.forceId;
    }

    /*
    @Override
    public byte getForceId()
    {
        return this.forceId;
    }

    @Override
    public void setForceId(byte newid)
    {
        this.forceId = newid;
    }
    */

    @Override
    public void forceUpdate()
    {
        // HACK to force Container.detectAndSendChanges to detect change and notify ContainerListener
        // In past used to just serialize current cap NBT data, but this seemed to be making client not
        //   stack all new items if client receives new item before this tag set - making it not match other 
        //   items it will match after NBT arrives.
        
        NBTTagCompound tag = owner.getOrCreateSubCompound("Update");
        byte sendid = tag.getByte("i");
        sendid++;
        tag.setByte("i", sendid);
    }
    
    /*
    protected void setForceIdNBT(byte sendid)
    {
        NBTTagCompound tag = owner.getOrCreateSubCompound("Update");
        tag.setByte("i", sendid);        
    }
    */

    /*
     * Set the owner of this capability, and init based on that owner
     */
    @Override
    public void setOwner(ItemStack ownerIn)
    {
        if (ownerIn != owner)
        {
            owner = ownerIn;

            // on server, setting default waits until later so a World will be present
            // on client, tooltip will init with reasonable guess until update is received from server
            
            // set to first value so stacking will work on client before first update received
            forceUpdate();
        }
    }

    @Override
    public ItemStack getOwner()
    {
        return owner;
    }
}
