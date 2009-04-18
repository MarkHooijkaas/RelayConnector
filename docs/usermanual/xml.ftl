<chapter><title>XML construction commands</title>
<para>There are several commands that all use the same syntax for
building XML:</para>
<itemizedlist>
	<listitem><para>The append command appends data to existing XML variables.</para></listitem>
	<listitem><para>The output command appends data to the output XML variable.</para></listitem>
	<listitem><para>The call command creates a new SOAP XML request message and appends data to this XML before
	sending this request.</para></listitem>
	<listitem><para>The http command can be used to send any XML using HTTP-SOAP.</para></listitem>
	<listitem><para>The createXmlVar command
	creates a new XML variable and appends data to this variable.</para></listitem>
</itemizedlist>
<para>All these commands use the same syntax,
and can be used to build complete XML data.  The syntax mainly
consists of nested elements that help build the XML. There are 5
different elements:</para>
<itemizedlist>
	<listitem><para>element, adds a new element</para></listitem>
	<listitem><para>attribute, adds a new attribute</para></listitem>
	<listitem><para>text, adds new text</para></listitem>
	<listitem><para>cdata, adds new cdata section</para></listitem>
	<listitem><para>include, adds the contents of a
	xml expression (this name might be changed in future)</para></listitem>
</itemizedlist>

<para>A complete overview of all possible
syntax is quite complicated. This is because the syntax is very
powerful but also meant to be very intuitive to read.  The syntax in
semi formal notation is shown below. It is shown for the output
command, but the call, append
and createXmlVar command all
share this syntax.</para>

<@xmlcode>
<output 
	[name="<replaceable>element-name</replaceable>"] 
	[prefix="prefix<replaceable>-name</replaceable>" [scriptXmlns="false"]] 
	[namespace="<replaceable>namespace</replaceable>" [reduceXmlns="false"]] 
	[text="<replaceable>text-expression</replaceable>"]
	[xml="<replaceable>xml-expression</replaceable>"] 
	[childrenOf="<replaceable>xml-expression</replaceable>"]
	[target="<replaceable>xml-expression</replaceable>"] 
>
  <!-- any of the following elements in any order, as many times as needed -->
  <element 
	[name="<replaceable>element-name</replaceable>"] 
	[prefix="prefix<replaceable>-name</replaceable>" [scriptXmlns="false"]] 
	[namespace="<replaceable>namespace</replaceable>" [reduceXmlns="false"]] 
	[text="<replaceable>text-expression</replaceable>"]
	[xml="<replaceable>xml-expression</replaceable>"] 
	[childrenOf="<replaceable>xml-expression</replaceable>"]
	[target="<replaceable>xml-expression</replaceable>"] 
  >
    <!-- nested elements, attributes, etc if needed -->
  </element>

  <attribute name="<replaceable>attribute-name</replaceable>" text="<replaceable>text-expression</replaceable>" />
  <text 
	[name="<replaceable>element-name</replaceable>"]
	[prefix="prefix<replaceable>-name</replaceable>" [scriptXmlns="false"]] 
	[namespace="<replaceable>namespace</replaceable>" [reduceXmlns="false"]] 
	[text="<replaceable>text-expression</replaceable>"]>	[<replaceable>fixed-text</replaceable>]</text>
  <cdata 

	[name="<replaceable>element-name</replaceable>"]
	[prefix="prefix<replaceable>-name</replaceable>" [scriptXmlns="false"]] 
	[namespace="<replaceable>namespace</replaceable>" [reduceXmlns="false"]] 

	[text="<replaceable>text-expression</replaceable>"]>[<replaceable>fixed-text</replaceable>]</cdata>
  <include ..../> <!-- synonym for element -->
</output>
</@xmlcode>



<sect1><title>Attribute shortcuts</title>
<para>There are several attributes possible. Some of these attributes
are all shorthand notations, which can also be accomplished by using
children elements. The rules are as follows:</para>
<itemizedlist>
	<listitem><para>If the name attribute is present,
	a child element with that name is created, and all content is added
	under this element.</para></listitem>
	<listitem><para>If a text attribute is present, the text of that expression
	is added to the element</para></listitem>
</itemizedlist>

<para>An example without a name for a new element</para>
<@xmlcode>
<output text="hello world!"/>

<!-- is shorthand for -->

<output>
  <text text="hello world!"/>
</output>

<output xml="/input/field1"/>

<!-- is shorthand for -->

<output>
  <include xml="/input/field1"/>
</output>


<output childrenOf="/input/field1"/>

<!-- is shorthand for -->

<output>
  <include childrenOf="/input/field1"/>
</output>


<output name="data" .....>
  <!-- possibly extra elements -->
</output>

<!-- is shorthand for -->

<output>
  <element name="" ....>
    <!-- possibly extra elements -->
  </element>
</output>
</@xmlcode>
<para>And the same example with a new element name.</para>
<@xmlcode>
<output name="data" text="hello world!"/>

<!-- is shorthand for -->

<output>
  <element name="data" text="hello world!"/>
</output>

<!-- is shorthand for -->

<output>
  <element name="data">
    <text text="hello world!"/>
  </element>
</output>
</@xmlcode>

</sect1>



<sect1><title>examples</title>

<para>The example below shows some the main
elements</para>
<@xmlcode>
  <output name="demo">
    <attribute name="optional" text="true"/>
    <text text="Here one can use a text expression"/>
    <element name="bold" text="New York"/>
    <text>Static text can also be added in the element</text>
  </output>
</@xmlcode>


<para>This construct will create a new
element with the name "demo". In this element a new
attribute named "optional" is created with  as value the
text "true". Then some text is added, a new subelement
named "bold" is created, and finally some more text is
appended. 
</para>

<@xmlcode>
    <demo optional="true">
      Here one can use a text expression
      <bold>New York</bold>
      Static text can also be added in the element
    </demo>
</@xmlcode>


<para>The element,
text and cdata
elements all have an optional name
attribute. If this name attribute is provided, a new element is
created and the content is appended to the new element. otherwise the
content is appended to the current element. It might seem a bit
strange for an element statement to have no name, because it's sole
purpose would be to add a new element. However it can be very useful
to add the content of an XML variable to an existing element, without
adding a new element.</para>

<para>The element, and include elements also have an optional target attribute. 
This can be used to add XML to entirely different parts of the XML tree.
The special case for which this attribute has been created is to access the SOAP:Header
in a call command. See the following example:
</para>
<@xmlcode>
    <call namespace="..." method="...">
      <element name="MySoapHeader" target="../../Header" text="somedata"/>
    </call>
</@xmlcode>


<para>The element, text and cdata elements also have optional prefix and namespace attributes. 
The exact behavior is quite complex, but should be intuitive for the common cases.</para>
<itemizedlist>
	<listitem><para>If a prefix
	attribute is present, the name of the node is prefixed with this
	prefix. This will also be the case when no name
	attribute is present, and thus no new element is created. In this
	case the name of the existing node is changed to use the given
	prefix. You do not need to also define a namespace, and can use only
	a prefix. 
	</para></listitem>
	<listitem><para>The default behaviour is the to
	use the namespace as defined in one of the previous xmlns commands. 
	This can be overridden by setting the attribute scriptXmlns to
	false. 
	</para></listitem>
	<listitem><para>If scriptXmlns is false, or the
	script contains no xmlns command for this prefix, the namespace will
	be unknown. In this case one must make sure that the prefix is
	defined in some higher level, otherwise  this will result in an XML
	error.</para></listitem>
	<listitem><para>If a  namespace
	attribute is present, or this is found as an xmlns command earlier
	in the script, the element will be ensured to get this namespace.
	The mechanism will not add a xmlns attribute, if the correct
	namespace is already defined in a parent element. This behavior can
	be changed by setting the reduceXmlns attribute to false.</para></listitem>
	<listitem><para>If a prefix is given, the element
	name will have this prefix, and a "xmnls:<replaceable>prefix</replaceable>"
	attribute will be added (if needed).</para></listitem>
	<listitem><para>If no prefix is given, the element
	name will have no prefix, using the default mechanism, and a "xmnls"
	attribute will be added (if needed).</para></listitem>
</itemizedlist>

</sect1>
</chapter>
