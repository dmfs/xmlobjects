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

package org.dmfs.xmlobjects.pull;

import java.util.HashMap;
import java.util.Map;

import org.dmfs.xmlobjects.ElementDescriptor;
import org.dmfs.xmlobjects.android.pull.AndroidParserContext;
import org.dmfs.xmlobjects.builder.IObjectBuilder;
import org.xmlpull.v1.XmlPullParser;


/**
 * Represents the current context of the parser. Subclass this to provide additional information to {@link IObjectBuilder} instances. On Android use
 * {@link AndroidParserContext} to parse xml files with resource references.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ParserContext
{

	/**
	 * A cache of recycled objects. We cache one object per {@link ElementDescriptor}.
	 */
	private final Map<ElementDescriptor<?>, Object> mRecycledObjects = new HashMap<ElementDescriptor<?>, Object>(32);

	/**
	 * The current {@link XmlPullParser} instance.
	 */
	private XmlPullParser mParser;


	/**
	 * Set the current {@link XmlPullParser} instance.
	 * 
	 * @param parser
	 *            The current {@link XmlPullParser}.
	 */
	void setXmlPullParser(XmlPullParser parser)
	{
		mParser = parser;
	}


	/**
	 * Returns the current {@link XmlPullParser} instance. You MUST NOT change the state of the parser, so don't call {@link XmlPullParser#next()},
	 * {@link XmlPullParser#nextTag()}, {@link XmlPullParser#nextText()} or {@link XmlPullParser#nextToken()}.
	 * 
	 * @return The current {@link XmlPullParser} instance.
	 */
	public XmlPullParser getXmlPullParser()
	{
		return mParser;
	}


	/**
	 * Recycle the given object. The basic implementation will store only one recycled object instance per {@link ElementDescriptor} in the recycler.
	 * 
	 * @param descriptor
	 *            The {@link ElementDescriptor} of the XML element
	 * @param object
	 *            The object to recycle.
	 */
	public <T> void recycle(ElementDescriptor<T> descriptor, T object)
	{
		if (object != null)
		{
			mRecycledObjects.put(descriptor, object);
		}
	}


	/**
	 * Get a recycled instance. If there was no instance in the recycler this method returns <code>null</code>.
	 * <p>
	 * <stong>Note:</strong> The object is returned exactly as passed to {@link #recycle(ElementDescriptor, Object)}, so you need to ensure you reset the
	 * state yourself.
	 * </p>
	 * 
	 * @param descriptor
	 *            The {@link ElementDescriptor} for which to return a recycled object.
	 * @return a recycled object or <code>null</code> if there is none.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRecycled(ElementDescriptor<T> descriptor)
	{
		// we can safely cast here, because we know that recycle always puts the right type
		return (T) mRecycledObjects.remove(descriptor);
	}
}
