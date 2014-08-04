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

import java.util.HashSet;
import java.util.Set;

import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;


/**
 * A builder for sets of elements. All child elements must be of the same type and have the same {@link XmlElementDescriptor}. Child elements with different
 * types will be ignored.
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 * &lt;set>
 *    &lt;element>element 1&lt;/element>
 *    &lt;element>element 2&lt;/element>
 *    &lt;element>element 3&lt;/element>
 * &lt;/set>
 * </pre>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class SetObjectBuilder<T> extends AbstractXmlObjectBuilder<Set<T>>
{
	private final static int DEFAULT_INITIAL_CAPACITY = 16;

	private final XmlElementDescriptor<T> mSetElementDescriptor;

	private final int mInitialCapacity;


	public SetObjectBuilder(XmlElementDescriptor<T> setElementDescriptor)
	{
		mSetElementDescriptor = setElementDescriptor;
		mInitialCapacity = DEFAULT_INITIAL_CAPACITY;
	}


	public SetObjectBuilder(XmlElementDescriptor<T> setElementDescriptor, int initialCapacity)
	{
		mSetElementDescriptor = setElementDescriptor;
		mInitialCapacity = initialCapacity;
	}


	@Override
	public Set<T> get(XmlElementDescriptor<Set<T>> descriptor, Set<T> recycle, ParserContext context)
	{
		if (recycle != null)
		{
			recycle.clear();
			return recycle;
		}
		else
		{
			return new HashSet<T>(mInitialCapacity);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public <V> Set<T> update(XmlElementDescriptor<Set<T>> descriptor, Set<T> object, XmlElementDescriptor<V> childDescriptor, V child, ParserContext context)
		throws XmlObjectPullParserException
	{
		if (childDescriptor == mSetElementDescriptor)
		{
			object.add((T) child);
		}
		return object;
	}

}
