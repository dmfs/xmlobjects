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

package org.dmfs.xmlobjects.pull.builder;

import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.IXmlObjectBuilder;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;


/**
 * A basic implementation of an {@link IXmlObjectBuilder}. This class provides default implementations for all methods.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <T>
 *            The type of the objects being built by this builder.
 */
public abstract class AbstractXmlObjectBuilder<T> implements IXmlObjectBuilder<T>
{

	@Override
	public T get(XmlElementDescriptor<T> descriptor, T recycle, ParserContext context) throws XmlObjectPullParserException
	{
		return null;
	}


	@Override
	public T update(XmlElementDescriptor<T> descriptor, T object, QualifiedName attribute, String value, ParserContext context)
		throws XmlObjectPullParserException
	{
		return object;
	}


	@Override
	public T update(XmlElementDescriptor<T> descriptor, T object, String text, ParserContext context) throws XmlObjectPullParserException
	{
		return object;
	}


	@Override
	public <V> T update(XmlElementDescriptor<T> descriptor, T object, XmlElementDescriptor<V> childDescriptor, V child, ParserContext context)
		throws XmlObjectPullParserException
	{
		return object;
	}


	@Override
	public <V> T update(XmlElementDescriptor<T> descriptor, T object, QualifiedName anonymousChildName, IXmlObjectBuilder<V> anonymousChildBuilder,
		V anonymousChild, ParserContext context) throws XmlObjectPullParserException
	{
		return object;
	}


	@Override
	public T finish(XmlElementDescriptor<T> descriptor, T object, ParserContext context) throws XmlObjectPullParserException
	{
		return object;
	}

}
