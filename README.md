# xmlobjects

__A lightweight high level XML pull parser framework__

This library is meant to build an XML stream parser that allows memory efficient operation while still beeing convenient. With this library you pull the objects you're interested in one by one. You can process one object before you parse the next one, allowing you to recycle the returned object.

## Requirements

This code builds on top of an existing parser that implements the XmlPull interface, see http://www.xmlpull.org.

To use the Android specific builders you need the Android SDK (any recent release should do).

## Builders

This library uses the concept of builders to create objects from the XML elements. For each XML element there must be at least one builder that knows how to transform it into an object. To do so, each builder has six methods to 

* get a new object for a given XML Element
* update an object with an attribute
* update an object with a text value
* update an object with a child element
* update an object with an anonymous child (that's stil under development and not used in the current version)
* finish the object

Each time a start tag is parsed, the `get` method is called on the respective builder, which returns an object for this element. It's perfectly valid to return `null` if the start tag itself doesn't carry enough information to make up a new object. After the call to `get` the respective methods of the builder are called for each attribute, text value and child element of the current XML element. Finally the `finish` method is called for each object to do some final processing.

Each of the above methods returns the object that is beeing built, which allows the builder to return a completely different object if necessary.

### Recycling of instances

The `get` method takes an additional parameter to pass an old instance that is not used anymore. If possible, a builder can just reset and reuse that object instead of creating a new one. However, each builder is free to create new instances as it likes.

## Examples

The code snippets below should give you an idea how this library works and how to use it. This example parses an XML file that looks like this:

		<library>
			<book>
				<author>Moby-Dick; or, The Whale</author>
				<title>Herman Melville</title>
				<published>1851</published>
			</book>
			<book>
				<author>Alice’s Adventures in Wonderland</author>
				<title>Lewis Carroll</title>
				<published>1865</published>
			</book>
		</library>

### Defining the Java model

First you define the model that stores your data.

		public class Book
		{
			public String title;
			public String author;
			public int published;

			@Override
			public String toString()
			{
				return title + " by " + author + ", published in " + published ;
			}
		}

		public class Library
		{
			// nothing, since we don't store the books in here
		}

### Defining builders

There are a couple of predefined builders for simple types you can use, but for complex elements you'll have to create your own builder like so:


		private static class BookBuilder extends AbstractXmlObjectBuilder<Book>
		{

			@Override
			public Book get(XmlElementDescriptor<Book> descriptor, Book recycle, ParserContext context)
			{
				if (recycle != null)
				{
					// recycle the book
					recycle.author = null;
					recycle.title = null;
					recycle.released = -1;
					return recycle;
				}
				// return a new book
				return new Book();
			}


			@Override
			public <V> Book update(XmlElementDescriptor<Book> descriptor, Book object, XmlElementDescriptor<V> childType,
					V child, ParserContext context)
			{
				if (childType == TITLE)
				{
					object.title = (String) child;
				}
				else if (childType == AUTHOR)
				{
					object.author = (String) child;
				}
				else if (childType == PUBLISHED)
				{
					object.published = (Integer) child;
				}
				return object;
			}
		}


### Defining the XML model

Next, you define the XML model and assign builders.

		// author is just a String
		private final static XmlElementDescriptor<String> AUTHOR =
			XmlElementDescriptor.register(QualifiedName.get("author"), StringObjectBuilder.INSTANCE);

		// title is just a String as well
		private final static XmlElementDescriptor<String> TITLE =
			XmlElementDescriptor.register(QualifiedName.get("title"), StringObjectBuilder.INSTANCE);

		// published is an integer
		private final static XmlElementDescriptor<Integer> PUBLISHED =
			XmlElementDescriptor.register(QualifiedName.get("published"), IntegerObjectBuilder.INSTANCE);

		// book is built by a BookBuilder
		private final static XmlElementDescriptor<Book> BOOK =
			XmlElementDescriptor.register(QualifiedName.get("book"), new BookBuilder());

		// Library doesn't store any fields, so use the default builder which builds and returns nothing.
		private final static XmlElementDescriptor<Library> LIBRARY =
			XmlElementDescriptor.register(QualifiedName.get("library"), new AbstractXmlObjectBuilder<Library>());

### Pulling objects

Now we're set up to pull books from the XML file.

		// get an XmlPullParser
		XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = pullParserFactory.newPullParser();
		parser.setInput(new StringReader(inputXml) /* pass the reader that reads the xml stream */);

		// get an XmlObjectPull instancefor this parser
		XmlObjectPull objectPull = new XmlObjectPull(parser);

		// the path to pull from, i.e. only elements in <library>
		XmlPath libraryPath = new XmlPath(LIBRARY);

		Book book = null;
		while (objectPull.hasNext(BOOK, libraryPath))
		{
			book = op.pull(BOOK /* pull elements that are Books */,
				book /* recycle the previous book */,
				libraryPath /* pull from this path */);

			// do something with book
		}


### Reflection

This framework doesn't rely on Reflection. However, there is some proof-of-concept state code that can make it easier to populate objects from XML elemtents.
Using the Reflection builder for the class `Book` above the models would look like:

		private static class Book
		{
			@Element(name = "title")
			public String title;

			@Element(name = "author")
			public String author;

			@Element(name = "published")
			public int published;


			@Override
			public String toString()
			{
				return title + " by " + author + ", published in " + published ;
			}
		}

		// book is built by Reflection 
		private final static XmlElementDescriptor<Book> BOOK =
			XmlElementDescriptor.register(QualifiedName.get("book"), new ReflectionObjectBuilder<Book>(Book.class));



## TODO:

* improve code, finialize interfaces
* support anonymous elements, i.e. elements that don't have an XmlElementDescriptor
* add a serializer that works the same way for serializing objects to XML
* publish test suite

## License

Copyright (c) Marten Gajda 2014, licensed under Apache 2 (see `LICENSE`).

