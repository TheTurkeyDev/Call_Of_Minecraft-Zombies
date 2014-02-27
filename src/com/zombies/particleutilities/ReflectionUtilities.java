package com.zombies.particleutilities;

import java.lang.reflect.Field;

public class ReflectionUtilities
{

	/**
	 * sets a value of an {@link Object} via reflection
	 * 
	 * @param instance
	 *            instance the class to use
	 * @param fieldName
	 *            the name of the {@link Field} to modify
	 * @param value
	 *            the value to set
	 * @throws Exception
	 */
	public static void setValue(Object instance, String fieldName, Object value) throws Exception
	{
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	/**
	 * get a value of an {@link Object}'s {@link Field}
	 * 
	 * @param instance
	 *            the target {@link Object}
	 * @param fieldName
	 *            name of the {@link Field}
	 * @return the value of {@link Object} instance's {@link Field} with the
	 *         name of fieldName
	 * @throws Exception
	 */
	public static Object getValue(Object instance, String fieldName) throws Exception
	{
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

}