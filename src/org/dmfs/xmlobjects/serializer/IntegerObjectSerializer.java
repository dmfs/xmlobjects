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


public class IntegerObjectSerializer extends AbstractObjectSerializer<Integer>
{

	public final static IntegerObjectSerializer INSTANCE = new IntegerObjectSerializer();


	@Override
	public void writeChildren(XmlElementDescriptor<Integer> descriptor, Integer object, IXmlChildWriter childWriter, SerializerContext context)
		throws SerializerException, IOException
	{
		if (object != null)
		{
			childWriter.writeText(object.toString());
		}
	}
}
