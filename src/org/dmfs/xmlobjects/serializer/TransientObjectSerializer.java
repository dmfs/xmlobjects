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

package org.dmfs.xmlobjects.serializer;

import java.io.IOException;

import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer.IXmlChildWriter;


/**
 * An {@link IXmlObjectSerializer} for transient elements.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <T>
 */
public class TransientObjectSerializer<T> extends AbstractObjectSerializer<T>
{
	/**
	 * The descriptor of the child element.
	 */
	private final XmlElementDescriptor<T> mChildDescriptor;


	/**
	 * Create a new {@link TransientObjectSerializer} for with the given child descriptor.
	 * 
	 * @param childDescriptor
	 */
	public TransientObjectSerializer(XmlElementDescriptor<T> childDescriptor)
	{
		mChildDescriptor = childDescriptor;
	}


	@Override
	public void writeChildren(XmlElementDescriptor<T> descriptor, T object, IXmlChildWriter childWriter, SerializerContext context) throws SerializerException,
		IOException
	{
		childWriter.writeChild(mChildDescriptor, object);
	}
}
