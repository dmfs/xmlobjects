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

import java.net.URI;
import java.net.URISyntaxException;

import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;


/**
 * A builder for elements that enclose a URI value like so:
 * 
 * <pre>
 * &lt;href>http://dmfs.org/&lt;/href>
 * </pre>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class UriObjectBuilder extends AbstractXmlObjectBuilder<URI>
{
	/**
	 * A strict {@link UriObjectBuilder}. A strict builder will throw an {@link XmlObjectPullParserException} when the value is not a valid URI.
	 */
	public final static UriObjectBuilder INSTANCE_STRICT = new UriObjectBuilder(true);

	/**
	 * A lax {@link UriObjectBuilder}. A lax builder will return null if the value is not a valid URI.
	 */
	public final static UriObjectBuilder INSTANCE = new UriObjectBuilder(false);

	/**
	 * Indicates if this {@link UriObjectBuilder} is strict.
	 */
	private final boolean mStrict;


	/**
	 * Create a {@link UriObjectBuilder} instance.
	 * 
	 * @param strict
	 *            <code>true</code> to get a strict {@link UriObjectBuilder}, <code>false</code> otherwise.
	 */
	private UriObjectBuilder(boolean strict)
	{
		mStrict = strict;
	}


	@Override
	public URI update(XmlElementDescriptor<URI> descriptor, URI object, String text, ParserContext context) throws XmlObjectPullParserException
	{
		try
		{
			return new URI(mStrict ? text : text.trim());
		}
		catch (URISyntaxException e)
		{
			if (!mStrict)
			{
				return null;
			}

			throw new XmlObjectPullParserException("could not parse URI in '" + text + "'", e);
		}
	}
}