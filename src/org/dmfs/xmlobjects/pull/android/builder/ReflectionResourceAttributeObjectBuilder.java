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

package org.dmfs.xmlobjects.pull.android.builder;

import java.lang.reflect.Field;
import java.net.URI;

import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.dmfs.xmlobjects.pull.android.AndroidParserContext;
import org.dmfs.xmlobjects.pull.builder.reflection.ReflectionObjectBuilder;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.util.Xml;


/**
 * A builder for xml elements that are populated using refelection. At present, this is more like a proof of concept. This builder can resolve Android resource
 * references.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ReflectionResourceAttributeObjectBuilder<T> extends ReflectionObjectBuilder<T>
{

	public ReflectionResourceAttributeObjectBuilder(Class<T> classParam)
	{
		super(classParam);
	}


	@Override
	public T update(XmlElementDescriptor<T> descriptor, T object, QualifiedName attribute, String value, ParserContext context)
		throws XmlObjectPullParserException
	{
		Resources resources = null;
		if (context instanceof AndroidParserContext)
		{
			resources = ((AndroidParserContext) context).getResources();
		}

		AttributeSet p = Xml.asAttributeSet(context.getXmlPullParser());

		Field field = mAttributeMap.get(attribute);
		if (field != null)
		{
			final String name = attribute.name;
			final String namespace = attribute.namespace;

			Object resultValue = null;
			int res = p.getAttributeResourceValue(namespace, name, 0 /* the invalid resource id */);
			if (field.getType() == String.class)
			{
				if (res == 0)
				{
					resultValue = p.getAttributeValue(namespace, name);
				}
				else if (resources != null)
				{
					resultValue = resources.getString(res);
				}

			}
			else if (field.getType() == int.class || field.getType() == Integer.class)
			{
				if (res == 0)
				{
					resultValue = p.getAttributeIntValue(namespace, name, 0);
				}
				else if (resources != null)
				{
					try
					{
						resultValue = resources.getInteger(res);
					}
					catch (NotFoundException e)
					{
						resultValue = res;
					}
				}
				else
				{
					// special case, return the resource id if there are no resources
					resultValue = res;
				}
			}
			else if (field.getType() == float.class || field.getType() == Float.class)
			{
				resultValue = p.getAttributeFloatValue(namespace, name, 0);
			}
			else if (field.getType() == boolean.class || field.getType() == Boolean.class)
			{
				if (res == 0)
				{
					resultValue = p.getAttributeBooleanValue(namespace, name, false);
				}
				else if (resources != null)
				{
					resultValue = resources.getBoolean(res);
				}
			}
			else if (field.getType() == URI.class)
			{
				String uri = null;
				if (res == 0)
				{
					uri = p.getAttributeValue(namespace, name);
				}
				else if (resources != null)
				{
					uri = resources.getString(res);
				}

				if (uri != null)
				{
					resultValue = URI.create(uri);
				}
			}

			if (resultValue != null)
			{
				field.setAccessible(true);
				try
				{
					field.set(object, resultValue);
				}
				catch (IllegalArgumentException e)
				{
				}
				catch (IllegalAccessException e)
				{
				}
			}
			else
			{
				return super.update(descriptor, object, attribute, value, context);
			}

		}
		return object;
	}
}
