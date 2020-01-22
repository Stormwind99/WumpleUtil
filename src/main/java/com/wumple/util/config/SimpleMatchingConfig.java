package com.wumple.util.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.wumple.util.base.misc.Util;

public class SimpleMatchingConfig<T>
{

	protected final Map<String, T> map;
	public final T FALSE_VALUE;

	public SimpleMatchingConfig(T falseValueIn)
	{
        map = new HashMap<String, T>();
        FALSE_VALUE = falseValueIn;
	}
	
    public SimpleMatchingConfig(Map<String, T> configIn, T falseValueIn)
    {
        map = configIn;
        FALSE_VALUE = falseValueIn;
    }

	public Map<String, T> getMap()
	{
		return map;
	}

	public boolean addDefaultProperty(String name, T amountIn)
	{
	    if (name == null)
	    {
	        name = "";
	    }
	
	    map.putIfAbsent(name, amountIn);
	
	    return true;
	}

	public boolean addDefaultProperty(String[] items, T amountIn)
	{
	    boolean success = true;
	
	    for (String item : items)
	    {
	        success &= addDefaultProperty(item, amountIn);
	    }
	
	    return success;
	}

	public T getProperty(String key)
	{
	    T amount = null;
	
	    if ((key != null) && map.containsKey(key))
	    {
	        amount = map.get(key);
	    }
	
	    return amount;
	}

	@Nullable
	public T getProperty(List<String> keys)
	{
	    T amount = null;
	
	    for (String key : keys)
	    {
	        amount = getProperty(key);
	        if (amount != null)
	        {
	            break;
	        }
	    }
	
	    return amount;
	}
	
    public T getValue(String s)
    {
        return Util.getValueOrDefault(getProperty(s), FALSE_VALUE);
    }
}