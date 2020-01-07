package com.wumple.util.xmap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.MapData;

public class XMapData extends MapData implements IXMapData
{

	public XMapData(String mapname)
	{
		super(mapname);
	}

	/**
	 * reads in data from the CompoundNBT into this MapDataBase
	 */
	@Override
	public void read(CompoundNBT nbt)
	{
		// get scale before clamping
		byte scaleIn = nbt.getByte("scale");
	
		// FYI MapData.read clamps scale, like this:
		// this.scale = (byte)MathHelper.clamp(nbt.getByte("scale"), 0, 4);
		super.read(nbt);
	
		// re-set the scale without the fixed max clamping
		// MAYBE this.scale = (byte)MathHelper.clamp(scaleIn, 0, XMapAPI.getInstance().getMaxScale());
		this.scale = scaleIn;
	}

}