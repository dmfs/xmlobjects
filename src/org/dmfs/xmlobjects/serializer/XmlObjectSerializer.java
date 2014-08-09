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
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlContext;
import org.dmfs.xmlobjects.XmlElementDescriptor;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;


/**
 * High-level XML serializer.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class XmlObjectSerializer
{
	/**
	 * Characters we use to build prefixes.
	 */
	private final static char[] PREFIX_CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
		'W', 'X', 'Y', 'Z' };

	/**
	 * The actual serializer.
	 */
	private final XmlSerializer mSerializer;

	/**
	 * The current {@link SerializerContext}.
	 */
	private final SerializerContext mSerializerContext;

	/**
	 * A set of known name spaces.
	 */
	private Set<String> mKnownNamespaces;

	/**
	 * A writer for attributes.
	 */
	public interface IXmlAttributeWriter
	{
		/**
		 * Adds the given attribute to the current element.
		 * 
		 * @param name
		 *            The name of the attribute.
		 * @param value
		 *            The value of the attribute.
		 * @throws SerializerException
		 * @throws IOException
		 */
		public void writeAttribute(QualifiedName name, String value) throws SerializerException, IOException;
	}

	/**
	 * A writer for child elements, including text nodes.
	 */
	public interface IXmlChildWriter
	{
		/**
		 * Add the given child element to the current element.
		 * 
		 * @param descriptor
		 *            The descriptor of the child to add.
		 * @param child
		 *            The child object.
		 * @throws SerializerException
		 * @throws IOException
		 */
		public <T> void writeChild(XmlElementDescriptor<T> descriptor, T child) throws SerializerException, IOException;


		public void writeText(String text) throws SerializerException, IOException;
	}

	/**
	 * The actual instance of {@link IXmlAttributeWriter}.
	 */
	private final IXmlAttributeWriter mAttributeWriter = new IXmlAttributeWriter()
	{

		@Override
		public void writeAttribute(QualifiedName name, String value) throws SerializerException, IOException
		{
			try
			{
				mSerializer.attribute(name.namespace, name.name, value);
			}
			catch (IllegalArgumentException e)
			{
				throw new SerializerException("can not serialize attribute " + name, e);
			}
			catch (IllegalStateException e)
			{
				throw new SerializerException("can not serialize attribute " + name, e);
			}
		}
	};

	/**
	 * The actual instance if {@link IXmlChildWriter}.
	 */
	private final IXmlChildWriter mChildWriter = new IXmlChildWriter()
	{

		@Override
		public <T> void writeChild(XmlElementDescriptor<T> descriptor, T child) throws SerializerException, IOException
		{
			if (descriptor.serializer != null)
			{
				QualifiedName name = descriptor.qualifiedName;
				try
				{
					mSerializer.startTag(name.namespace, name.name);
					descriptor.serializer.writeAttributes(descriptor, child, mAttributeWriter, mSerializerContext);
					descriptor.serializer.writeChildren(descriptor, child, mChildWriter, mSerializerContext);
					mSerializer.endTag(name.namespace, name.name);
				}
				catch (IllegalArgumentException e)
				{
					throw new SerializerException("can not serialize element " + name, e);
				}
				catch (IllegalStateException e)
				{
					throw new SerializerException("can not serialize element " + name, e);
				}
			}
		}


		@Override
		public void writeText(String text) throws SerializerException, IOException
		{
			if (text != null)
			{
				try
				{
					mSerializer.text(text);
				}
				catch (IllegalArgumentException e)
				{
					throw new SerializerException("can not serialize text '" + text + "'", e);
				}
				catch (IllegalStateException e)
				{
					throw new SerializerException("can not serialize text '" + text + "'", e);
				}
			}
		}

	};


	/**
	 * Create a new high-level XML serializer using the given low-level {@link XmlSerializer}.
	 * 
	 * @param serializer
	 *            The low-level serializer.
	 * @param xmlContext
	 *            The {@link XmlContext} to use.
	 */
	public XmlObjectSerializer(XmlSerializer serializer, XmlContext xmlContext)
	{
		mSerializer = serializer;
		mSerializerContext = new SerializerContext();
		mSerializerContext.setXmlContext(xmlContext);
	}


	/**
	 * Create a new high-level XML serializer that writes to the given {@link Writer}.
	 * 
	 * @param out
	 *            The {@link Writer} to write to.
	 * @param xmlContext
	 *            The {@link XmlContext} to use.
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public XmlObjectSerializer(Writer out, XmlContext xmlContext) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException
	{
		mSerializer = XmlPullParserFactory.newInstance().newSerializer();
		mSerializer.setOutput(out);
		mSerializerContext = new SerializerContext();
		mSerializerContext.setXmlContext(xmlContext);
	}


	/**
	 * Create a new high-level XML serializer that writes to the given {@link OutputStream} using the given charset.
	 * 
	 * @param out
	 *            The {@link OutputStream} to write to.
	 * @param charset
	 *            The charset. The serializer will use "UTF-8" if this is <code>null</code>.
	 * @param xmlContext
	 *            The {@link XmlContext} to use.
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public XmlObjectSerializer(OutputStream out, String charset, XmlContext xmlContext) throws IllegalArgumentException, IllegalStateException, IOException,
		XmlPullParserException
	{
		mSerializer = XmlPullParserFactory.newInstance().newSerializer();
		mSerializer.setOutput(out, charset == null ? "UTF-8" : charset);
		mSerializerContext = new SerializerContext();
		mSerializerContext.setXmlContext(xmlContext);
	}


	/**
	 * Inform the serializer that the given namespace will be used. This allows the serializer to bind a prefix early.
	 * 
	 * @param namespace
	 *            The namespace that will be used.
	 */
	public void useNamespace(String namespace)
	{
		if (namespace != null && namespace.length() > 0)
		{
			if (mKnownNamespaces == null)
			{
				mKnownNamespaces = new HashSet<String>(8);
			}
			mKnownNamespaces.add(namespace);
		}
	}


	/**
	 * Inform the serializer that the given {@link QualifiedName} will be used. This allows the serializer to bind a prefix early.
	 * 
	 * @param name
	 *            The {@link QualifiedName} that will be used.
	 */
	public void useNamespace(QualifiedName name)
	{
		useNamespace(name.namespace);
	}


	/**
	 * Inform the serializer that the given {@link XmlElementDescriptor} will be used. This allows the serializer to bind a prefix early.
	 * 
	 * @param elementDescriptor
	 *            The {@link XmlElementDescriptor} that will be used.
	 */
	public void useNamespace(XmlElementDescriptor<?> elementDescriptor)
	{
		useNamespace(elementDescriptor.qualifiedName.namespace);
	}


	/**
	 * Serialize the given root object with the given {@link XmlElementDescriptor}.
	 * 
	 * @param descriptor
	 *            The descriptor of the object.
	 * @param rootObject
	 *            The root object itself.
	 * @throws SerializerException
	 * @throws IOException
	 */
	public <T> void serialize(XmlElementDescriptor<T> descriptor, T rootObject) throws SerializerException, IOException
	{
		mSerializer.startDocument(null, null);
		useNamespace(descriptor.qualifiedName);
		bindNamespaces();
		mChildWriter.writeChild(descriptor, rootObject);
		mSerializer.endDocument();
	}


	/**
	 * Ensure all known namespaces have been bound to a prefix.
	 * 
	 * @throws IOException
	 */
	private void bindNamespaces() throws IOException
	{
		if (mKnownNamespaces == null)
		{
			return;
		}

		StringBuilder nsBuilder = new StringBuilder(8);
		int count = 0;

		for (String ns : mKnownNamespaces)
		{
			int num = count;
			do
			{
				nsBuilder.append(PREFIX_CHARS[num % PREFIX_CHARS.length]);
				num /= PREFIX_CHARS.length;
			} while (num > 0);

			mSerializer.setPrefix(nsBuilder.toString(), ns);
			nsBuilder.setLength(0);
			++count;
		}
	}
}
