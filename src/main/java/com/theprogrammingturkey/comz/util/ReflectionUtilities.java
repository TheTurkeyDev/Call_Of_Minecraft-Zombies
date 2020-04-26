package com.theprogrammingturkey.comz.util;

import java.lang.reflect.Field;
import java.util.logging.Level;

import com.theprogrammingturkey.comz.COMZombies;

public class ReflectionUtilities
{

	/**
	 * sets a value of an {@link Object} via reflection
	 *
	 * @param instance  instance the class to use
	 * @param fieldName the name of the {@link Field} to modify
	 * @param value     the value to set
	 */
	public static void setValue(Object instance, String fieldName, Object value)
	{
		try
		{
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(instance, value);
		} catch(Exception e)
		{
			COMZombies.log.log(Level.WARNING, COMZombies.consoleprefix + " Failed on particles!");
		}
	}

	/**
	 * get a value of an {@link Object}'s {@link Field}
	 *
	 * @param instance  the target {@link Object}
	 * @param fieldName name of the {@link Field}
	 * @return the value of {@link Object} instance's {@link Field} with the
	 * name of fieldName
	 */
	public static Object getValue(Object instance, String fieldName) throws Exception
	{
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

}