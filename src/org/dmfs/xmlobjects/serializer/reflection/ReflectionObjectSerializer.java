package org.dmfs.xmlobjects.serializer.reflection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlContext;
import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.builder.reflection.Attribute;
import org.dmfs.xmlobjects.pull.builder.reflection.Element;
import org.dmfs.xmlobjects.pull.builder.reflection.Text;
import org.dmfs.xmlobjects.serializer.AbstractObjectSerializer;
import org.dmfs.xmlobjects.serializer.SerializerContext;
import org.dmfs.xmlobjects.serializer.SerializerException;
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer.IXmlAttributeWriter;
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer.IXmlChildWriter;


/**
 * Proof of concept serializer using reflection to get the fields and field values.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <T>
 *            The class to serialize.
 */
public abstract class ReflectionObjectSerializer<T> extends AbstractObjectSerializer<T>
{
	/**
	 * A map of all fields that should serialized to an attribute.
	 */
	protected final Map<QualifiedName, Field> mAttributeMap = new HashMap<QualifiedName, Field>(8);

	/**
	 * A map of all fields that should be serialized to a child element.
	 */
	protected final List<FieldHolder> mElementList = new ArrayList<FieldHolder>(8);


	public ReflectionObjectSerializer(Class<T> genericClass)
	{
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
		List<FieldHolder> elementMap = mElementList;
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
					elementMap.add(new FieldHolder(QualifiedName.get(namespace, name), field));
				}
				Text text = field.getAnnotation(Text.class);
				if (text != null)
				{
					elementMap.add(new FieldHolder(null, field));
				}
			}
		}
	}


	@Override
	public void writeAttributes(XmlElementDescriptor<T> descriptor, T object, IXmlAttributeWriter attributeWriter, SerializerContext context)
		throws SerializerException, IOException
	{
		for (Entry<QualifiedName, Field> attribute : mAttributeMap.entrySet())
		{
			Object value;
			try
			{
				value = attribute.getValue().get(object);
			}
			catch (IllegalArgumentException e)
			{
				throw new SerializerException("can not read attribute " + attribute.getKey(), e);
			}
			catch (IllegalAccessException e)
			{
				throw new SerializerException("can not read attribute " + attribute.getKey(), e);
			}

			if (value != null)
			{
				attributeWriter.writeAttribute(attribute.getKey(), value.toString());
			}
		}
	}


	@Override
	public void writeChildren(XmlElementDescriptor<T> descriptor, T object, IXmlChildWriter childWriter, SerializerContext context) throws SerializerException,
		IOException
	{
		XmlContext xmlContext = context.getXmlContext();
		for (FieldHolder fieldHolder : mElementList)
		{
			Object value;
			try
			{
				value = fieldHolder.field.get(object);
			}
			catch (IllegalArgumentException e)
			{
				throw new SerializerException("can not read field " + fieldHolder.field.getName(), e);
			}
			catch (IllegalAccessException e)
			{
				throw new SerializerException("can not read field " + fieldHolder.field.getName(), e);
			}

			if (fieldHolder.name != null)
			{
				@SuppressWarnings("unchecked")
				XmlElementDescriptor<Object> childDescriptor = (XmlElementDescriptor<Object>) XmlElementDescriptor.get(fieldHolder.name, xmlContext);

				Class<?> fieldType = fieldHolder.field.getType();

				if (Collection.class.isAssignableFrom(fieldType) && !fieldType.isInterface() && !Modifier.isAbstract(fieldType.getModifiers()))
				{
					for (Object child : (Collection<?>) value)
					{
						childWriter.writeChild(childDescriptor, child);
					}
				}
				else
				{
					childWriter.writeChild(childDescriptor, value);
				}
			}
			else if (value != null)
			{
				childWriter.writeText(value.toString());
				;
			}
		}
	}

	private final static class FieldHolder
	{
		public final QualifiedName name;
		public final Field field;


		public FieldHolder(QualifiedName name, Field field)
		{
			this.name = name;
			this.field = field;
		}
	}
}