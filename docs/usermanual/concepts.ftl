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
<para>These expressions are still very
limited. There is no support for recurring elements with the same
name, for selecting attributes or for selecting all children without
the surrounding element.</para>

<para>There is support for namespaces. First
one must map a prefix to the correct namespace using the xmlns
command (see <xref linkend="xmlns"/>). Next one can use this prefix within the
xml expression.</para>

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
</sect2>
</sect1>

</chapter>
