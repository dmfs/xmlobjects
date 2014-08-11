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

package org.dmfs.xmlobjects.android.pull;

import org.dmfs.xmlobjects.pull.ParserContext;

import android.content.res.Resources;


/**
 * A {@link ParserContext} that also stores and returns a {@link Resources} instace to provide it to the builders in order to resolve resource references.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class AndroidParserContext extends ParserContext
{
	private final Resources mResources;


	public AndroidParserContext(Resources resources)
	{
		mResources = resources;
	}


	/**
	 * Get a {@link Resources} instance.
	 * 
	 * @return An instance of {@link Resources}.
	 */
	public Resources getResources()
	{
		return mResources;
	}
}
