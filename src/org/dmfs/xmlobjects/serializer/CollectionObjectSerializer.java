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
import java.util.Collection;

import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer.IXmlChildWriter;


/**
 * An {@link IXmlObjectSerializer} for collections of elements of the same type.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <T>
 *            The type of the elements in the collection.
 */
public class CollectionObjectSerializer<T> extends AbstractObjectSerializer<Collection<T>>
{
	private final XmlElementDescriptor<T> mChildDescriptor;


	/**
	 * Create a new {@link CollectionObjectSerializer} for collections with elements of the given {@link XmlElementDescriptor}.
	 * 
	 * @param childDescriptor
	 *            The {@link XmlElementDescriptor} that describes the child elements.
	 */
	public CollectionObjectSerializer(XmlElementDescriptor<T> childDescriptor)
	{
		mChildDescriptor = childDescriptor;
	}


	@Override
	public void writeChildren(XmlElementDescriptor<Collection<T>> descriptor, Collection<T> object, IXmlChildWriter childWriter, SerializerContext context)
		throws SerializerException, IOException
	{
		for (T element : object)
		{
			childWriter.writeChild(mChildDescriptor, element);
		}
	}
}
