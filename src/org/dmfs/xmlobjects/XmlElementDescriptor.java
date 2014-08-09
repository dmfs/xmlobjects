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

package org.dmfs.xmlobjects;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.dmfs.xmlobjects.pull.IXmlObjectBuilder;
import org.dmfs.xmlobjects.serializer.IXmlObjectSerializer;


/**
 * A description of an XML element. This descriptor can return an {@link IXmlObjectBuilder} for a specific XML element.
 * 
 * <p>
 * TODO: Add a more sophisticated way to manage anonymous child builders, like returning a different builder depening on the qualified name.
 * </p>
 * <p>
 * TODO: Add serializers that know how to write XML elements.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class XmlElementDescriptor<T>
{
	public final static XmlContext DEFAULT_CONTEXT = new XmlContext()
	{
	};

	/**
	 * The {@link QualifiedName} of this element.
	 */
	public final QualifiedName qualifiedName;

	/**
	 * An {@link IXmlObjectBuilder} for elements of this type.
	 */
	public final IXmlObjectBuilder<T> builder;

	/**
	 * An {@link IXmlObjectSerializer} for elements of this type.
	 */
	public final IXmlObjectSerializer<T> serializer;

	/**
	 * An {@link IXmlObjectBuilder} for child elements that have no {@link XmlElementDescriptor}. This may be <code>null</code> to ignore these child elements.
	 */
	public final IXmlObjectBuilder<?> anonymousChildrenBuilder;

	/**
	 * A {@link WeakReference} to the context this element was registered in.
	 */
	private final WeakReference<XmlContext> context;


	public static XmlElementDescriptor<?> get(QualifiedName qname)
	{
		synchronized (DEFAULT_CONTEXT)
		{
			return DEFAULT_CONTEXT.DESCRIPTOR_MAP.get(qname);
		}
	}


	public static XmlElementDescriptor<?> get(QualifiedName qname, XmlContext context)
	{
		if (context == null)
		{
			context = DEFAULT_CONTEXT;
		}

		synchronized (context)
		{
			final Map<QualifiedName, XmlElementDescriptor<?>> contextMap = context.DESCRIPTOR_MAP;
			XmlElementDescriptor<?> result = contextMap.get(qname);
			if (result != null)
			{
				return result;
			}
		}

		synchronized (DEFAULT_CONTEXT)
		{
			return DEFAULT_CONTEXT.DESCRIPTOR_MAP.get(qname);
		}
	}


	public static <T> XmlElementDescriptor<T> register(QualifiedName qname, IXmlObjectBuilder<T> builder)
	{
		return register(qname, builder, null, null, DEFAULT_CONTEXT);
	}


	public static <T> XmlElementDescriptor<T> register(QualifiedName qname, IXmlObjectBuilder<T> builder, IXmlObjectSerializer<T> serializer)
	{
		return register(qname, builder, null, serializer, DEFAULT_CONTEXT);
	}


	public static <T> XmlElementDescriptor<T> register(QualifiedName qname, IXmlObjectBuilder<T> builder, IXmlObjectBuilder<?> anonymousChildrenBuilder)
	{
		return register(qname, builder, anonymousChildrenBuilder, null, DEFAULT_CONTEXT);
	}


	public static <T> XmlElementDescriptor<T> register(QualifiedName qname, IXmlObjectBuilder<T> builder, XmlContext context)
	{
		return register(qname, builder, null, context);
	}


	public static <T> XmlElementDescriptor<T> register(QualifiedName qname, IXmlObjectBuilder<T> builder, IXmlObjectSerializer<T> serializer, XmlContext context)
	{
		return register(qname, builder, null, serializer, context);
	}


	@SuppressWarnings("unchecked")
	public static <T> XmlElementDescriptor<T> register(QualifiedName qname, IXmlObjectBuilder<T> builder, IXmlObjectBuilder<?> anonymousChildrenBuilder,
		IXmlObjectSerializer<T> serializer, XmlContext context)
	{
		if (context == null)
		{
			context = DEFAULT_CONTEXT;
		}

		synchronized (context)
		{
			Map<QualifiedName, XmlElementDescriptor<?>> descriptorMap = context.DESCRIPTOR_MAP;

			XmlElementDescriptor<?> descriptor = descriptorMap.get(qname);
			if (descriptor != null)
			{
				throw new IllegalStateException("descriptor for " + qname + " already exists, use 'overload' to override the definition");
			}

			descriptor = new XmlElementDescriptor<T>(qname, builder, anonymousChildrenBuilder, serializer, context);
			descriptorMap.put(qname, descriptor);
			return (XmlElementDescriptor<T>) descriptor;
		}
	}


	public static <T> XmlElementDescriptor<T> overload(XmlElementDescriptor<? super T> oldDescriptor, IXmlObjectBuilder<T> builder)
	{
		return overload(oldDescriptor, builder, null, null);
	}


	public static <T> XmlElementDescriptor<T> overload(XmlElementDescriptor<? super T> oldDescriptor, IXmlObjectBuilder<T> builder,
		IXmlObjectSerializer<T> serializer)
	{
		return overload(oldDescriptor, builder, null, serializer);
	}


	public static <T> XmlElementDescriptor<T> overload(XmlElementDescriptor<? super T> oldDescriptor, IXmlObjectBuilder<T> builder,
		IXmlObjectBuilder<?> anonymousChildrenBuilder, IXmlObjectSerializer<T> serializer)
	{
		XmlContext context = oldDescriptor.context.get();
		if (context == null)
		{
			throw new IllegalStateException("can not overload element in gc'ed context");
		}

		synchronized (context)
		{
			Map<QualifiedName, XmlElementDescriptor<?>> descriptorMap = context.DESCRIPTOR_MAP;
			QualifiedName qname = oldDescriptor.qualifiedName;
			XmlElementDescriptor<T> descriptor = new XmlElementDescriptor<T>(qname, builder, anonymousChildrenBuilder, serializer, context);
			descriptorMap.put(qname, descriptor);
			return descriptor;
		}
	}


	private XmlElementDescriptor(QualifiedName qname, IXmlObjectBuilder<T> builder, IXmlObjectBuilder<?> anonymousChildrenBuilder,
		IXmlObjectSerializer<T> serializer, XmlContext context)
	{
		if (qname == null)
		{
			throw new IllegalArgumentException("qname must not be null");
		}

		this.qualifiedName = qname;
		this.builder = builder;
		this.serializer = serializer;
		this.anonymousChildrenBuilder = anonymousChildrenBuilder;
		this.context = new WeakReference<XmlContext>(context);
	}


	public XmlContext getContext()
	{
		return context.get();
	}


	@Override
	public int hashCode()
	{
		return qualifiedName.hashCode();
	}


	@Override
	public boolean equals(Object o)
	{
		return o == this;
	}
}
