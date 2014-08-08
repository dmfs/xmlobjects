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

import java.io.IOException;
import java.util.LinkedList;

import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlContext;
import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * XML Parser that allows to pull parsed objects from an XML stream.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class XmlObjectPull
{

	/**
	 * The current {@link XmlPullParser}.
	 */
	private final XmlPullParser mParser;

	private final XmlPath mCurrentElementDescriptorPath = new XmlPath();
	private final LinkedList<Object> mObjectStack = new LinkedList<Object>();

	private IXmlObjectBuilder<?> mCurrentBuilder;
	private XmlContext mContext;
	private ParserContext mParserContext;


	public XmlObjectPull(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		this(parser, new ParserContext());
	}


	public XmlObjectPull(XmlPullParser parser, ParserContext parserContext) throws XmlPullParserException, IOException
	{
		mParser = parser;
		mParserContext = parserContext;
		mParserContext.setXmlPullParser(parser);
		parser.next();
	}


	public void setContext(XmlContext context)
	{
		mContext = context;
	}


	/**
	 * Moves forward to the start of the next element that matches the given type and path.
	 * 
	 * @return <code>true</code> if there is such an element, false otherwise.
	 * @throws XmlPullParserException
	 * @throws XmlObjectPullParserException
	 * @throws IOException
	 */
	public <T> boolean moveToNext(XmlElementDescriptor<T> type, XmlPath path) throws XmlPullParserException, XmlObjectPullParserException, IOException
	{
		pullInternal(type, null, path, true, false);
		return !isEndOfDocument();
	}


	/**
	 * Moves forward to the start of the next element that matches the given type and path without leaving the current sub-tree. If there is no other element of
	 * that type in the current sub-tree, this mehtod will stop at the closing tab current sub-tree. Calling this methods with the same parameters won't get you
	 * any further.
	 * 
	 * @return <code>true</code> if there is such an element, false otherwise.
	 * @throws XmlPullParserException
	 * @throws XmlObjectPullParserException
	 * @throws IOException
	 */
	public <T> boolean moveToNextSibling(XmlElementDescriptor<T> type, XmlPath path) throws XmlPullParserException, XmlObjectPullParserException, IOException
	{
		return pullInternal(type, null, path, true, true) != null || mParser.getDepth() == path.length() + 1;
	}


	/**
	 * Return whether the end of the document has been reached.
	 * 
	 * @return <code>true</code> if the end of the document has been reached.
	 * @throws XmlPullParserException
	 */
	public boolean isEndOfDocument() throws XmlPullParserException
	{
		return mParser.getEventType() == XmlPullParser.END_DOCUMENT;
	}


	/**
	 * Returns the qualified name of the current element.
	 * 
	 * @return
	 */
	public QualifiedName getCurrentElementQualifiedName()
	{
		XmlPullParser parser = mParser;
		return QualifiedName.get(parser.getNamespace(), parser.getName());
	}


	/**
	 * Returns an {@link XmlElementDescriptor} for the current element.
	 * 
	 * @return The {@link XmlElementDescriptor} that will be used to build an object for this element.
	 */
	public XmlElementDescriptor<?> getCurrentElementDescriptor()
	{
		return XmlElementDescriptor.get(getCurrentElementQualifiedName(), mContext);
	}


	/**
	 * Pull the next object of the given type from the XML stream. If the current position is within such an object the current object is returned.
	 * 
	 * @param type
	 * @param recycle
	 * @param path
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws XmlObjectPullParserException
	 */
	public <T> T pull(XmlElementDescriptor<T> type, T recycle, XmlPath path) throws XmlPullParserException, IOException, XmlObjectPullParserException
	{
		return pullInternal(type, recycle, path, false, false);
	}


	@SuppressWarnings("unchecked")
	private <T, U, V> T pullInternal(XmlElementDescriptor<T> type, T recycle, XmlPath path, boolean stopOnStartTag, boolean stopOnLeaveSubTree)
		throws XmlPullParserException, IOException, XmlObjectPullParserException
	{
		if (type.getContext() != mContext && type.getContext() != XmlElementDescriptor.DEFAULT_CONTEXT)
		{
			throw new IllegalArgumentException("type is from an invalid context");
		}

		// cache some fields locally
		ParserContext parserContext = mParserContext;
		XmlPullParser parser = mParser;
		IXmlObjectBuilder<?> currentBuilder = mCurrentBuilder;
		XmlPath currentPath = mCurrentElementDescriptorPath;
		LinkedList<Object> objectStack = mObjectStack;
		XmlElementDescriptor<?> currentElementDescriptor = mCurrentElementDescriptorPath.peek();

		// we ignore all elements below this depth
		int ignoreDepth = Integer.MAX_VALUE;

		// the object we're going to recycle
		Object recycled = recycle;

		// the current object we're working with
		Object currentObject = objectStack.peek();

		while (true)
		{
			int next = parser.getEventType();
			int currentDepth = parser.getDepth();
			switch (next)
			{
				case XmlPullParser.START_TAG:
				{
					if (currentDepth < ignoreDepth)
					{
						XmlElementDescriptor<?> nextClass = XmlElementDescriptor.get(QualifiedName.get(parser.getNamespace(), parser.getName()), mContext);
						if (nextClass != null)
						{
							currentElementDescriptor = nextClass;
							if (stopOnStartTag && type == currentElementDescriptor && path.matches(currentPath))
							{
								return null;
							}

							if (recycled == null)
							{
								// try to get an object to recycle from the parser context
								recycled = parserContext.getRecycled(currentElementDescriptor);
							}

							currentBuilder = mCurrentBuilder = currentElementDescriptor.builder;
							currentObject = ((IXmlObjectBuilder<V>) currentBuilder).get((XmlElementDescriptor<V>) currentElementDescriptor, (V) recycled,
								parserContext);

							recycled = null;

							// pass all attributes to the builder
							for (int i = 0, count = parser.getAttributeCount(); i < count; ++i)
							{
								currentObject = ((IXmlObjectBuilder<V>) currentBuilder).update((XmlElementDescriptor<V>) currentElementDescriptor,
									(V) currentObject, QualifiedName.get(parser.getAttributeNamespace(i), parser.getAttributeName(i)),
									parser.getAttributeValue(i), parserContext);
							}

							objectStack.push(currentObject);
							currentPath.append(currentElementDescriptor);
						}
						else
						{
							ignoreDepth = currentDepth;
						}
					}
					break;
				}
				case XmlPullParser.END_TAG:
				{
					if (currentDepth < ignoreDepth)
					{
						if (stopOnLeaveSubTree && currentDepth - 1 < path.length())
						{
							// we're about to leave the current sub-tree, stop right now
							return null;
						}

						// finalize the current object, which now becomes the child object of it's parent
						V childObject = ((IXmlObjectBuilder<V>) currentBuilder).finish((XmlElementDescriptor<V>) currentElementDescriptor, (V) currentObject,
							parserContext);
						XmlElementDescriptor<V> childClass = (XmlElementDescriptor<V>) currentElementDescriptor;

						// remove child from the stack
						currentPath.pop();
						objectStack.pop();

						// get parent object
						currentElementDescriptor = currentPath.peek();
						currentObject = objectStack.peek();

						if (currentElementDescriptor != null)
						{
							currentBuilder = mCurrentBuilder = currentElementDescriptor.builder;
						}

						if (type == childClass && !stopOnStartTag && currentPath.matches(path))
						{
							parser.next();
							return (T) childObject;
						}
						else
						{
							currentObject = ((IXmlObjectBuilder<U>) currentBuilder).update((XmlElementDescriptor<U>) currentElementDescriptor,
								(U) currentObject, childClass, childObject, parserContext);
						}
					}
					else if (currentDepth == ignoreDepth)
					{
						// we're back at the depth of the current object
						ignoreDepth = Integer.MAX_VALUE;
					}
					break;
				}
				case XmlPullParser.TEXT:
				{
					if (currentDepth < ignoreDepth)
					{
						// update current object with text value
						currentObject = ((IXmlObjectBuilder<V>) currentBuilder).update((XmlElementDescriptor<V>) currentElementDescriptor, (V) currentObject,
							parser.getText(), parserContext);
					}
					break;
				}
				case XmlPullParser.END_DOCUMENT:
				{
					return null;
				}
			}
			parser.next();
		}
	}
}
