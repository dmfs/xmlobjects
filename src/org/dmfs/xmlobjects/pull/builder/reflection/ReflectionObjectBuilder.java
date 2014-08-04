/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.xmlobjects.pull.builder.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.Recyclable;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.dmfs.xmlobjects.pull.builder.AbstractXmlObjectBuilder;


/**
 * A builder for xml elements that are populated using refelection. At present, this is more like a proof of concept.
 * <p>
 * At present this builder requires T to have a default constructor without parameters.
 * </p>
 * 
 * <p>
 * TODO: is there a better way to get the actual class of T than by passing it to the constructor? We need a way that works with anonymous classes.
 * </p>
 * 
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ReflectionObjectBuilder<T> extends AbstractXmlObjectBuilder<T>
{

	/**
	 * The class this builder returns objects of.
	 */
	private final Class<T> mGenericClass;

	/**
	 * A map of all fields that should be populated from an attribute.
	 */
	protected final Map<QualifiedName, Field> mAttributeMap = new HashMap<QualifiedName, Field>(8);

	/**
	 * A map of all fields that should be populated from a child element.
	 */
	protected final Map<QualifiedName, Field> mElementMap = new HashMap<QualifiedName, Field>(8);

	/**
	 * A list of all fields that should be populated from a text.
	 */
	protected final List<Field> mTextList = new ArrayList<Field>(8);


	public ReflectionObjectBuilder(Class<T> genericClass)
	{
		mGenericClass = genericClass;
		buildFieldMaps(genericClass);

		Class<?> superClass = genericClass.getSuperclass();
		while (superClass != Object.class)
		{
			buildFieldMaps(superClass);
			superClass = superClass.getSuperclass();
		}
	}


	private void buildFieldMaps(Class<?> classParam)
	{
		Map<QualifiedName, Field> attributeMap = mAttributeMap;
		Map<QualifiedName, Field> elementMap = mElementMap;
		List<Field> textList = mTextList;
		for (Field field : classParam.getDeclaredFields())
		{
			Attribute attribute = field.getAnnotation(Attribute.class);
			if (attribute != null)
			{
				field.setAccessible(true);
				String name = attribute.name();
				String namespace = attribute.namespace();
				if (name.length() == 0)
				{
					name = field.getName();
				}
				attributeMap.put(QualifiedName.get(namespace, name), field);
			}
			else
			{
				Element element = field.getAnnotation(Element.class);
				if (element != null)
				{
					field.setAccessible(true);
					String name = element.name();
					String namespace = element.namespace();
					if (name.length() == 0)
					{
						name = field.getName();
					}
					elementMap.put(QualifiedName.get(namespace, name), field);
				}
				else
				{
					Text text = field.getAnnotation(Text.class);
					if (text != null)
					{
						field.setAccessible(true);
						textList.add(field);
					}
				}
			}
		}

	}


	@Override
	public T get(XmlElementDescriptor<T> descriptor, T recycle, ParserContext context) throws XmlObjectPullParserException
	{
		if (recycle instanceof Recyclable)
		{
			((Recyclable) recycle).recycle();
			return recycle;
		}

		T result;
		try
		{
			result = getInstance(context);
		}
		catch (InstantiationException e)
		{
			throw new XmlObjectPullParserException("can not instantiate instance of " + mGenericClass, e);
		}
		catch (IllegalAccessException e)
		{
			throw new XmlObjectPullParserException("can not instantiate instance of " + mGenericClass, e);
		}

		return result;
	}


	@Override
	public T update(XmlElementDescriptor<T> descriptor, T object, QualifiedName attribute, String value, ParserContext context)
		throws XmlObjectPullParserException
	{
		Field field = mAttributeMap.get(attribute);
		if (field != null)
		{
			assignValue(field, object, value);
		}
		return object;
	}


	@Override
	public T update(XmlElementDescriptor<T> descriptor, T object, String text, ParserContext context) throws XmlObjectPullParserException
	{
		for (Field field : mTextList)
		{
			assignValue(field, object, text);
		}
		return object;
	}


	@SuppressWarnings("unchecked")
	@Override
	public <V> T update(XmlElementDescriptor<T> descriptor, T object, XmlElementDescriptor<V> child, V data, ParserContext context)
		throws XmlObjectPullParserException
	{
		QualifiedName qualifiedName = child.qualifiedName;

		Field field = mElementMap.get(qualifiedName);
		if (field != null)
		{
			Class<?> fieldType = field.getType();
			try
			{
				if (Collection.class.isAssignableFrom(fieldType) && !fieldType.isInterface() && !Modifier.isAbstract(fieldType.getModifiers()))
				{
					Collection<Object> collection = (Collection<Object>) field.get(object);
					if (collection == null)
					{
						collection = (Collection<Object>) fieldType.newInstance();
						field.set(object, collection);
					}
					collection.add(data);
				}
				else if (fieldType.isAssignableFrom(data.getClass()))
				{
					field.set(object, data);
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new XmlObjectPullParserException("can not assign '" + data + "' to a field of type " + fieldType, e);
			}
			catch (IllegalAccessException e)
			{
				throw new XmlObjectPullParserException("can not assign '" + data + "' to a field of type " + fieldType, e);
			}
			catch (InstantiationException e)
			{
				throw new XmlObjectPullParserException("can not insanciate collection for " + fieldType, e);
			}
		}
		return object;
	}


	public T getInstance(ParserContext context) throws InstantiationException, IllegalAccessException
	{
		return mGenericClass.newInstance();
	}


	private void assignValue(Field field, Object object, String value) throws XmlObjectPullParserException
	{
		Class<?> fieldType = field.getType();
		try
		{
			if (fieldType == String.class)
			{
				field.set(object, value);
			}
			else if (fieldType == int.class || fieldType == Integer.class)
			{
				field.setInt(object, Integer.parseInt(value));
			}
			else if (fieldType == byte.class || fieldType == Byte.class)
			{
				field.setByte(object, Byte.parseByte(value));
			}
			else if (fieldType == char.class || fieldType == Character.class)
			{
				field.setChar(object, value.length() > 0 ? value.charAt(0) : null /* will throw if value is '' and field type is char */);
			}
			else if (fieldType == short.class || fieldType == Short.class)
			{
				field.setShort(object, Short.parseShort(value));
			}
			else if (fieldType == long.class || fieldType == Long.class)
			{
				field.setLong(object, Long.parseLong(value));
			}
			else if (fieldType == float.class || fieldType == Float.class)
			{
				field.setFloat(object, Float.parseFloat(value));
			}
			else if (fieldType == double.class || fieldType == Double.class)
			{
				field.setDouble(object, Double.parseDouble(value));
			}
			else if (fieldType == boolean.class || fieldType == Boolean.class)
			{
				field.setBoolean(object, Boolean.parseBoolean(value));
			}
			else if (fieldType == URI.class)
			{
				field.set(object, new URI(value));
			}
		}
		catch (NumberFormatException e)
		{
			throw new XmlObjectPullParserException("can not assign '" + value + "' to a field of type " + fieldType, e);
		}
		catch (IllegalArgumentException e)
		{
			throw new XmlObjectPullParserException("can not assign '" + value + "' to a field of type " + fieldType, e);
		}
		catch (IllegalAccessException e)
		{
			throw new XmlObjectPullParserException("can not assign '" + value + "' to a field of type " + fieldType, e);
		}
		catch (URISyntaxException e)
		{
			throw new XmlObjectPullParserException("can not parse URI in '" + value + "'", e);
		}
	}
}
