package com.wumple.util.config;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.Item;

public class StringMatchingDualConfig<U> extends DualMatchingConfig<String, U>
{

	public StringMatchingDualConfig(Map<String, String> config1In, String falseValue1In, Map<String, U> config2In,
			U falseValue2In)
	{
		super(config1In, falseValue1In, config2In, falseValue2In);
	}
	
    public boolean addDefaultProperty(String name, @Nullable Item amount1In, U amount2In)
    {
        return addDefaultProperty(name, amount1In.getRegistryName().toString(), amount2In);
    }
    
    public boolean addDefaultProperty(Item item, String backup, @Nullable Item amount1In, U amount2In)
    {
          return addDefaultProperty(item, backup, amount1In.getRegistryName().toString(), amount2In);
    }
}
