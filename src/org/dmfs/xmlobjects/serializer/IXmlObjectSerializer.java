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
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer.IXmlAttributeWriter;
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer.IXmlChildWriter;


/**
 * The inferface of a serializer that knows how to serialize objects of a certain type to XML.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <T>
 *            The type this serializer can serialize.
 */
public interface IXmlObjectSerializer<T>
{
	/**
	 * Called when the attributes of the elements need to be written.
	 * 
	 * @param descriptor
	 *            The {@link XmlElementDescriptor} of the current element.
	 * @param object
	 *            The object to serialize.
	 * @param attributeWriter
	 *            An {@link IXmlAttributeWriter} to write to.
	 * @param context
	 *            The current {@link SerializerContext}.
	 * @throws SerializerException
	 * @throws IOException
	 */
	public void writeAttributes(XmlElementDescriptor<T> descriptor, T object, IXmlAttributeWriter attributeWriter, SerializerContext context)
		throws SerializerException, IOException;


	/**
	 * Called when the children of the elements need to be written.
	 * 
	 * @param descriptor
	 *            The {@link XmlElementDescriptor} of the current element.
	 * @param object
	 *            The object to serialize.
	 * @param childWriter
	 *            An {@link IXmlChildWriter} to write to.
	 * @param context
	 *            The current {@link SerializerContext}.
	 * @throws SerializerException
	 * @throws IOException
	 */
	public void writeChildren(XmlElementDescriptor<T> descriptor, T object, IXmlChildWriter childWriter, SerializerContext context) throws SerializerException,
		IOException;
}
