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
 * A builder for elements that enclose a Double value like so:
 * 
 * <pre>
 * &lt;some-double>123.456&lt;/some-double>
 * </pre>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DoubleObjectBuilder extends AbstractXmlObjectBuilder<Double>
{

	/**
	 * A strict {@link DoubleObjectBuilder}. If the value can not be parsed as a Double it will throw an {@link XmlObjectPullParserException}.
	 */
	public final static DoubleObjectBuilder INSTANCE_STRICT = new DoubleObjectBuilder(true);

	/**
	 * A tolerant {@link DoubleObjectBuilder}. Instead of throwing an exception it will return a <code>null</code> value.
	 */
	public final static DoubleObjectBuilder INSTANCE = new DoubleObjectBuilder(false);

	private final boolean mStrict;


	private DoubleObjectBuilder(boolean strict)
	{
		mStrict = strict;
	}


	@Override
	public Double update(XmlElementDescriptor<Double> descriptor, Double object, String text, ParserContext context) throws XmlObjectPullParserException
	{
		try
		{
			return Double.parseDouble(mStrict ? text : text.trim());
		}
		catch (NumberFormatException e)
		{
			if (!mStrict)
			{
				return null;
			}

			throw new XmlObjectPullParserException("could not parse double in '" + text + "'", e);
		}
	}

}
