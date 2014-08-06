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

import java.util.ArrayList;
import java.util.List;

import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.IXmlObjectBuilder;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;


/**
 * A builder for lists of elements. All child elements must be of the same type and have the same {@link XmlElementDescriptor} or the same
 * {@link IXmlObjectBuilder}. Child elements with different types will be ignored.
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 * &lt;list>
 *    &lt;element>element 1&lt;/element>
 *    &lt;element>element 2&lt;/element>
 *    &lt;element>element 3&lt;/element>
 * &lt;/list>
 * </pre>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ListObjectBuilder<T> extends AbstractXmlObjectBuilder<List<T>>
{
	private final static int DEFAULT_INITIAL_CAPACITY = 16;

	private final XmlElementDescriptor<T> mListElementDescriptor;
	private final IXmlObjectBuilder<T> mListElementBuilder;

	private final int mInitialCapacity;


	public ListObjectBuilder(XmlElementDescriptor<T> listElementDescriptor)
	{
		mListElementDescriptor = listElementDescriptor;
		mListElementBuilder = null;
		mInitialCapacity = DEFAULT_INITIAL_CAPACITY;
	}


	public ListObjectBuilder(XmlElementDescriptor<T> listElementDescriptor, int initialCapacity)
	{
		mListElementDescriptor = listElementDescriptor;
		mListElementBuilder = null;
		mInitialCapacity = initialCapacity;
	}


	public ListObjectBuilder(IXmlObjectBuilder<T> listElementBuilder)
	{
		mListElementDescriptor = null;
		mListElementBuilder = listElementBuilder;
		mInitialCapacity = DEFAULT_INITIAL_CAPACITY;
	}


	public ListObjectBuilder(IXmlObjectBuilder<T> listElementBuilder, int initialCapacity)
	{
		mListElementDescriptor = null;
		mListElementBuilder = listElementBuilder;
		mInitialCapacity = initialCapacity;
	}


	@Override
	public List<T> get(XmlElementDescriptor<List<T>> descriptor, List<T> recycle, ParserContext context)
	{
		if (recycle != null)
		{
			// we have a list that we can recycle
			recycle.clear();
			return recycle;
		}
		else
		{
			// return a new list
			return new ArrayList<T>(mInitialCapacity);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public <V> List<T> update(XmlElementDescriptor<List<T>> descriptor, List<T> object, XmlElementDescriptor<V> childDescriptor, V child, ParserContext context)
		throws XmlObjectPullParserException
	{
		if (childDescriptor == mListElementDescriptor || mListElementDescriptor == null && childDescriptor != null
			&& mListElementBuilder == childDescriptor.builder)
		{
			object.add((T) child);
		}
		return object;
	}

}
