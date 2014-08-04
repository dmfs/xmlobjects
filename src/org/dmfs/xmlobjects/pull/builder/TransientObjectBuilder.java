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

import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;


/**
 * A builder that just returns one of its child elements. It always returns the last child with the given {@link XmlElementDescriptor}. It's handy in cases like
 * this, when the parent element can have only one child element of a specific type:
 * 
 * <pre>
 * &lt;location>&lt;href>http://example.com&lt;/href>&lt;/location>
 * </pre>
 * 
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TransientObjectBuilder<T> extends AbstractXmlObjectBuilder<T>
{
	/**
	 * The descriptor of the child element to pass through.
	 */
	private final XmlElementDescriptor<T> mChildDescriptor;


	public TransientObjectBuilder(XmlElementDescriptor<T> childDescriptor)
	{
		mChildDescriptor = childDescriptor;
	}


	@SuppressWarnings("unchecked")
	@Override
	public <V> T update(XmlElementDescriptor<T> descriptor, T object, XmlElementDescriptor<V> childDescriptor, V child, ParserContext context)
		throws XmlObjectPullParserException
	{
		if (childDescriptor == mChildDescriptor)
		{
			return (T) child;
		}
		return object;
	}

}
