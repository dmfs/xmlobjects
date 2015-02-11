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

import org.dmfs.xmlobjects.builder.IObjectBuilder;


/**
 * A description of an XML element. This descriptor can return an {@link IObjectBuilder} for a specific XML element.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ElementDescriptor<T>
{
	public final static XmlContext DEFAULT_CONTEXT = new XmlContext()
	{
	};

	/**
	 * The {@link QualifiedName} of this element.
	 */
	public final QualifiedName qualifiedName;

	/**
	 * An {@link IObjectBuilder} for elements of this type.
	 */
	public final IObjectBuilder<T> builder;

	/**
	 * A {@link WeakReference} to the context this element was registered in.
	 */
	private final WeakReference<XmlContext> context;


	public static ElementDescriptor<?> get(QualifiedName qname)
	{
		synchronized (DEFAULT_CONTEXT)
		{
			return DEFAULT_CONTEXT.DESCRIPTOR_MAP.get(qname);
		}
	}


	public static ElementDescriptor<?> get(QualifiedName qname, XmlContext context)
	{
		if (context == null)
		{
			context = DEFAULT_CONTEXT;
		}

		synchronized (context)
		{
			final Map<QualifiedName, ElementDescriptor<?>> contextMap = context.DESCRIPTOR_MAP;
			ElementDescriptor<?> result = contextMap.get(qname);
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


	/**
	 * Register an element with the given name and no name space using the given {@link IObjectBuilder} in the default {@link XmlContext}.
	 * 
	 * @param name
	 *            The name of the element.
	 * @param builder
	 *            The {@link IObjectBuilder} to build and serialize this element.
	 * @return The {@link ElementDescriptor}.
	 */
	public static <T> ElementDescriptor<T> register(String name, IObjectBuilder<T> builder)
	{
		return register(QualifiedName.get(name), builder, DEFAULT_CONTEXT);
	}


	/**
	 * Register an element with the given {@link QualifiedName} using the given {@link IObjectBuilder} in the default {@link XmlContext}.
	 * 
	 * @param qname
	 *            The {@link QualifiedName} of the element.
	 * @param builder
	 *            The {@link IObjectBuilder} to build and serialize this element.
	 * @return The {@link ElementDescriptor}.
	 */
	public static <T> ElementDescriptor<T> register(QualifiedName qname, IObjectBuilder<T> builder)
	{
		return register(qname, builder, DEFAULT_CONTEXT);
	}


	/**
	 * Register an element with the given name and no name space using the given {@link IObjectBuilder} in the given {@link XmlContext}.
	 * 
	 * @param name
	 *            The name of the element.
	 * @param builder
	 *            The {@link IObjectBuilder} to build and serialize this element.
	 * @param context
	 *            An {@link XmlContext}.
	 * @return The {@link ElementDescriptor}.
	 */
	public static <T> ElementDescriptor<T> register(String name, IObjectBuilder<T> builder, XmlContext context)
	{
		return register(QualifiedName.get(name), builder, context);
	}


	/**
	 * Register an element with the given {@link QualifiedName} using the given {@link IObjectBuilder} in the given {@link XmlContext}.
	 * 
	 * @param qname
	 *            The {@link QualifiedName} of the element.
	 * @param builder
	 *            The {@link IObjectBuilder} to build and serialize this element.
	 * @param context
	 *            An {@link XmlContext} or <code>null</code> to use the default {@link XmlContext}.
	 * @return The {@link ElementDescriptor}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ElementDescriptor<T> register(QualifiedName qname, IObjectBuilder<T> builder, XmlContext context)
	{
		if (context == null)
		{
			context = DEFAULT_CONTEXT;
		}

		synchronized (context)
		{
			Map<QualifiedName, ElementDescriptor<?>> descriptorMap = context.DESCRIPTOR_MAP;

			ElementDescriptor<?> descriptor = descriptorMap.get(qname);
			if (descriptor != null)
			{
				throw new IllegalStateException("descriptor for " + qname + " already exists, use 'overload' to override the definition");
			}

			descriptor = new ElementDescriptor<T>(qname, builder, context);
			descriptorMap.put(qname, descriptor);
			return (ElementDescriptor<T>) descriptor;
		}
	}


	public static <T> ElementDescriptor<T> overload(ElementDescriptor<? super T> oldDescriptor, IObjectBuilder<T> builder)
	{
		XmlContext context = oldDescriptor.context.get();
		if (context == null)
		{
			throw new IllegalStateException("can not overload element in gc'ed context");
		}

		synchronized (context)
		{
			Map<QualifiedName, ElementDescriptor<?>> descriptorMap = context.DESCRIPTOR_MAP;
			QualifiedName qname = oldDescriptor.qualifiedName;
			ElementDescriptor<T> descriptor = new ElementDescriptor<T>(qname, builder, context);
			descriptorMap.put(qname, descriptor);
			return descriptor;
		}
	}


	private ElementDescriptor(QualifiedName qname, IObjectBuilder<T> builder, XmlContext context)
	{
		if (qname == null)
		{
			throw new IllegalArgumentException("qname must not be null");
		}

		this.qualifiedName = qname;
		this.builder = builder;
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
