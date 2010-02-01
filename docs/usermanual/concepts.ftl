<chapter><title>Fundamental concepts</title>
<para>The fundamental flow of a RelayConnector method is very simple:</para>
<itemizedlist>
	<listitem><para>an input message is received</para></listitem>
	<listitem><para>do some stuff in the script</para></listitem>
	<listitem><para>an output message is returned</para></listitem>
</itemizedlist>
<para>The first and last bullet are handled
automatically by the Connector. It is up to the implementation script
of a method to do some interesting stuff with the input message, and
prepare the correct output message. 
</para>



<sect1><title>XML Variables</title>
<para>In a script one can use and manipulate some simple XML variables.
For each script two basic variables always exist:</para>
<itemizedlist>
	<listitem><para>/input contains the
	input XML request message</para></listitem>
	<listitem><para>/output is used to
	compose the output XML response message</para></listitem>
</itemizedlist>
<para>Additional variables will contain the result of method calls that
the script has executed (see <xref linkend="call"/>). Additionally it is
possible to define new variables by hand (see the xml
command in <xref linkend="createXmlVar"/>), but it is not sure if this feature is
really needed. 
</para>
<para>Each variable has a name and the value of the entire XML or one of
it's sub-elements can be referenced using a starting slash, and
possibly additional element selectors. When assigning or appending to
a XML variable the leading slash is not needed.</para>
</sect1>




<sect1><title>Expressions</title>
<para>The expression syntax is still very basic and the parser is very
simplistic and will not handle all cases equally well. It is expected
that a new release will make some major improvements, but these might
be not compatible with the current syntax.</para>
<para>There are two kind of expressions:</para>
<itemizedlist>
	<listitem><para>xml expressions</para></listitem>
	<listitem><para>text expressions</para></listitem>
</itemizedlist>


<sect2 id="xml-expressions"><title>XML expressions</title>
<para>A XML expression starts with a XML
variable name (thus with a leading slash), selecting an existing XML
variable, or a children using a path like construction.  See the
example below.</para>
<@xmlcode>
  <!-- appends the entire input message to output -->
  <output xml="/input"/>
  <!-- appends a part of the input message to output -->
  <output xml="/input/tuple/old"/>
</@xmlcode>
<para>
It is possible to select an attribute by using an @ symbol for the attribute name as last element of a path.
For example
<@xmlcode>
  <!-- appends a part of the input message to output -->
  <output name="street" text="/input/address/@street"/>
  <output name="city" text="/input/address/@city"/>
</@xmlcode>
The code above will work on input xml in the form of:
<@xmlcode>
  <address street="..." city="..."/>
</@xmlcode>
One can also select the element name, prefixed name or namespace of an element using 
the localname(), name() and namespace() functions at the end of the path.
For example
<@xmlcode>
  <output name="name" text="/input/localname()"/>
  <output name="namespace" text="/input/namespace()"/>
</@xmlcode>
If some element is optional, one may prefix it with a question mark, e.g.
<@xmlcode>
  <output name="country" text="/input/address/?country"/>
</@xmlcode>
Will not result in an error if there is no country element available. 
Note that the command that uses this XML path will get no XML to perform it's action upon.
</para>

<para>
When there are multiple elements with the same name, usually the first element with that name is chosen.
One may make this explicit by using the regular-expression syntax ^ to denote the top/first element.
This is not strictly necessary because the first element is chosen anyway.
On the other hand, one may also select the last element with that name, by using the 
regular-expression anchor symbol $.
<@xmlcode>
  <output name="first-country" text="/input/address/^country"/>
  <output name="last-country"  text="/input/address/country$"/>
</@xmlcode>
One interesting use of the ^ and $ constructs is to use it with no name. 
In this case it will select either the first or last child element, regardless of name.
This can be usefull if one does not know the name of an element.
<@xmlcode>
  <output name="method" text="/input/jms-message/Envelope/Body/^/name()"/>
</@xmlcode>
It is not possible to select any other specific element, except for the first, or the last.
However it is possible to select all elements (with a certain name), by prefixing them with a star.
Such a path expression might result in a list of nodes, instead of just one node, so it should
only be used in commands that can handle a list of nodes, e.g. the delete, convert or rename commands.
<@xmlcode>
  <delete node="/input/*country/*city/description"/>
</@xmlcode>
The above example will delete the description node in all city nodes in all country nodes.
</para>


<para>There is also support for namespaces. 
First one must map a prefix to the correct namespace using the xmlns
command (see <xref linkend="xmlns"/>). 
Next one can use this prefix within the xml expression.</para>

<para>If the syntax .. is used in a path, the
parent element will be returned. This can be used to access the SOAP
header of the input. The following example will return the SOAP
header from the call.</para>

<@xmlcode>
<implementation type="RelayCall">
  <xmlns prefix="SOAP" namespace="http://schemas.xmlsoap.org/soap/envelope/"/> 
  <output xml="/input/../../SOAP:Header"/>
</implementation>
</@xmlcode>

</sect2>


<sect2><title>Text expressions</title>
<para>Text expressions are more flexible, but still very limited, and
has some serious limitations of the parser:</para>
<itemizedlist>
	<listitem><para>if the expression contains one or more + signs, it is split
	into several sub expressions, which will be concatenated</para></listitem>
	<listitem><para>if an expression starts with / it is assumed to be a XML
	variable.</para></listitem>
	<listitem><para>if an expression contains :: it is assumed to be calling a
	static java function</para></listitem>
	<listitem><para>if an expression is surrounded by ${dollar}{ and } it is supposed to
	be a text variable</para></listitem>
	<listitem><para>if an expression is surrounded by square brackets [ and ] it
	is supposed to be a constant string</para></listitem>
	<listitem><para>otherwise the entire text (without +, :: ${dollar}{, etc symbols) is
	considered a constant string</para></listitem>
</itemizedlist>

<para>A limitation is, that,because the first
rule is to split around + signs, it is not possible to use a literal
+ sign, not even when embedded in a constant string, surrounded with
[ and ]. If one needs to use a literal plus sign, there is a
predefined variable ${dollar}{plus}, e.g. to use "C++" in a
expression, one could encode this as "C+${dollar}{plus}+${dollar}{plus}". 
</para>

<para>
Variables can be used using the ${dollar}{<replaceable>varname</replaceable>} syntax. 
The varname might be a variable being declared earlier in the script, 
using the &lt;var&gt; command (or possibly other commands).
If this variable does not exist, the properties file is used to look for any property with that name.
If neither exists, the scripts aborts with an error, unless a default value is specified.
A default value can be specified using ?: after the varname, e.g.:
<@xmlcode>
<implementation type="RelayCall">
  <output text="[Hello ]+ ${dollar}{config.username?:World}"/>
</implementation>
</@xmlcode>
The default value is also used if a variable has been described in the script, 
but is has a null value (I can't think of a situation where this may happen).
</para>
</sect2>
</sect1>

</chapter>
