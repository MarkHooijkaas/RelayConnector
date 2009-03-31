<chapter><title>Introduction</title>
<para>The basic function of the RelayConnector is to relay method calls
to one or more other methods, with some simple transformations. 
</para>
<para>The RelayConnector can be used for a variety of purposes:</para>
<itemizedlist>
	<listitem><para>To make a method that calls a number of different methods (a
	composite service).</para></listitem>
	<listitem><para>To wrap an existing method with some simple transformations.</para></listitem>
	<listitem><para>To provide a set of methods in the same namespace, which have
	different underlying technical implementations, and are thus
	implemented on different application connectors (and thus with
	different namespaces).</para></listitem>
	<listitem><para>For all kind of simple testing and prototyping purposes, such
	as a HelloWorld or a Echo service.</para></listitem>
	<listitem><para>(Planned) To relay method calls to different Cordys
	organisations 
	</para></listitem>
</itemizedlist>

<para>In order to use the RelayConnector, one
must define methods by adding a method to a method set, and entering
a small XML script in the "implementation" section of this
method. The RelayConnector is not meant to be a full scripting
language. It is intended that a script should only be 10 or 20 lines
long. In fact a macro facility has been removed from the
RelayConnector, because this lead to too complex scripts. Instead it
is possible to easily define new command using java, which of course
is a full blown programming language.</para>

</chapter><chapter><title>Fundamental concepts</title>
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

</chapter><chapter><title>XML construction commands</title>
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
>
  <!-- any of the following elements in any order, as many times as needed -->
  <element 
	[name="<replaceable>element-name</replaceable>"] 
	[prefix="prefix<replaceable>-name</replaceable>" [scriptXmlns="false"]] 
	[namespace="<replaceable>namespace</replaceable>" [reduceXmlns="false"]] 
	[text="<replaceable>text-expression</replaceable>"]
	[xml="<replaceable>xml-expression</replaceable>"] 
	[childrenOf="<replaceable>xml-expression</replaceable>"]
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
<para>Note that t</para>


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

<para>The element,
text and cdata
elements also have optional prefix
and namespace attributes. The
exact behavior is quite complex, but should be intuitive for the
common cases.</para>
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


<chapter><title>Command reference RelayConnector</title>
<sect1><title>import command</title>
<@xmlcode>
<import name="<replaceable>commandname</replaceable>" class="<replaceable>classname</replaceable>"/>
</@xmlcode>

<para>This command defines a new command with
the name <replaceable>commandname</replaceable>. The command is implemented in a Java
class given by classname. The exact specifications of such a Java
class will be described elsewhere.</para>
</sect1>



<sect1><title>default command</title>
<@xmlcode>
<default attribute="<replaceable>attribute-name</replaceable>"
value="<replaceable>fixed-text</replaceable>"/>
</@xmlcode>
<para>This is a convenience function, that
allows to define a default value for certain attributes. Currently
the following attributes support this mechanism:</para>
<itemizedlist>
	<listitem><para>namespace</para></listitem>
	<listitem><para>async</para></listitem>
	<listitem><para>showSoap</para></listitem>
</itemizedlist>
</sect1>



<sect1 id="xmlns"><title>xmlns command</title>
<@xmlcode>
<xmlns prefix="<replaceable>prefix</replaceable>" namespace="<replaceable>namespace</replaceable>"/></para>
</@xmlcode>
<para>This function defines a certain prefix
to be used to relate to a certain namespace. These prefixes can then
be used in subsequent XML expressions, see <xref linkend="xml-expressions"/>.</para>
<para>It is considered to be an error to
define the same prefix twice.</para>
<para>In the example below the prefix "ns"
is defined and subsequently used.</para>
<@xmlcode>
<xmlns prefix="<emphasis><replaceable>ns</replaceable></emphasis>"
namespace="<replaceable>http://kisst.org/test</replaceable>"/>
<output xml="/input/<emphasis>ns</emphasis>:node"/>
</@xmlcode>
</sect1>



<sect1><title>append command</title>
<@xmlcode>
<append to="<replaceable>xml-expression</replaceable>"
    [name="<replaceable>name</replaceable>"]
    [xml="<replaceable>xml-expression</replaceable>"] 
    [text="<replaceable>text-expression</replaceable>"]>
  <!-- see chapter 3 -->
  <element ..../>
  <attribute..../>
  <text ..../>
  <cdata..../>
  <include..../>
</append>
</@xmlcode>

<para>This command appends XML to an existing
XML structure. 
</para>

<@xmlcode>
<append to="/output" name="city" text="New York"/>

<append to="/output">
  <element name="city" text="New York"/>
</append>
</@xmlcode>
</sect1>



<sect1><title>output command</title>
<@xmlcode>
<output
	[name="<replaceable>name</replaceable>"]
	[rename="<replaceable>name</replaceable>"]
	[xml="<replaceable>xml-expression</replaceable>"] 

	[text="<replaceable>text-expression</replaceable>"]>
  <!-- see chapter 3 -->
  <element ..../>
  <attribute..../>
  <text ..../>
  <cdata..../>
  <include..../>
</output>
</@xmlcode>
<para>This is a convenience function that is a synonym for</para> 
<@xmlcode>
<append to="/output" .....
</@xmlcode>
<para>It has one special attribute: rename.
If this attribute is present, the output element will be renamed.
This option is provided, because by default Cordys will create an
output message with as name, the name of the input message, with the
postfix "Response".</para>
</sect1>



<sect1  id="call"><title>call command</title>
<@xmlcode>
<call method="<replaceable>method-name"|</replaceable>methodExpression<replaceable>="expr"
</replaceable>
  [namespace="<replaceable>namespace"]</replaceable>*
  [async="true|false"]*
  [showSoap="true|false"]*
  [ignoreSoapFault="true|false"]*
  [appendMessagesTo="<replaceable>xml-expression</replaceable>"]*
  [resultVar="<replaceable>result-var-name</replaceable>"]
  [timeout="<replaceable>millisec</replaceable>"]
  [name<replaceable>="element-name"</replaceable>]
  [xml="<replaceable>xml-expression</replaceable>"] 
  [text="<replaceable>text-expression</replaceable>"]
>
  <!-- see chapter 3 -->
  <element ..../>
  <attribute..../>
  <text ..../>
  <cdata..../>
  <include..../>
</call>
</@xmlcode>

<para>This
command will execute a method call <replaceable>method-name </replaceable>in
<replaceable>namespace</replaceable>. The
response of this call will be stored in a variable <replaceable>result-var-name</replaceable>.
If the resultVar
attribute is not set,
this will default to <replaceable>method-name</replaceable>.
 Instead of specifying a fixed method-name, one can also specify a
dynamic methodExpression, in order to call a different method
depending on some content. In this case the resultVar attribute is
mandatory.</para>

<sect2><title>async attribute</title>
<para>If the async flag is true, the script sends the request message and will not wait
for the response, but will continue processing the next steps of the
script. If the script evaluates an expression that refers to
<replaceable>result-var-name</replaceable>, and
this variable is not yet filled, the script will then block until a
response message is received. The default value for the async
attribute is false.</para>

</sect2>

<sect2><title>showSoap attribute</title>
<para>If the showSoap
flag is true, the resultVar will be filled with the entire SOAP
message, including Envelope, Header and Body elements. Because this
info is usually not needed, the default behaviour is to set this
variable to false, which will discard the SOAP elements and store the
first child of the body in the ResultVar.</para>

</sect2>

<sect2><title>ignoreSoapFault
attribute</title>
<para>If the
ignoreSoapFault attribute is set
to true, a SOAP:Fault response will not result in an error, but the
SOAP:Fault message will be stored in the resultVar. If this attribute
is set to false (the default behavior), the processing of the script
will finish and a SOAP:Fault will be returned as result of the
script.</para>
</sect2>

<sect2><title>logSoapFault
attribute</title>
<para>If the logSoapFault
attribute is set, a SOAP:Fault response from a call will be logged.
The log level that it will be logged to is determined by the value of
this attribute which should be DEBUG, INFO, WARN. ERROR or FATAL. In
general it is advised not to set the level to ERROR. The function of
the RelayConnector is just to relay the response to the original
caller. The source of the error should log this error clearly, and
one should not want to see this error being logged in intermediate
(RelayConnector) logs.</para>

<para>If this attribute
is not specified, a setting from the configuration file will be used.
In this way one can globally configure the RelayConnector to log any
received SoapFaults at a certain level. 
</para>
</sect2>

<sect2><title>timeout attribute</title>
<para>The timeout attribute sets an timeout
value in milliseconds. If the method call does not answer within this
timeout period, the script will stop with a SOAP Fault. If no timeout
attribute is specified a system wide default timeout is used. This
value is 20 seconds by default, but a different value may be
configured (see <xref linkend="configuration"/>). 
</para>
</sect2>

<sect2><title>appendMessagesTo
attribute</title>
<para>The
appendMessagesTo attribute is mainly meant for debugging purposes. It
will append all request and response message to a XML element.
Usually you would set this using the default command to "/output",
during development, to see what is happening, and remove that default
command, once the function works. 
</para>

<@xmlcode>
<!-- remove the next statement after debugging is finished -->
<default attribute="appendMessagesTo" value="/output"/>

<!-- The input and output of these calls will be shown in the output -->
<call ...>
<call ...>
</@xmlcode>

<para>You might also set
it to something like "/output/log", but the one should
first append a "log" element to the output, e.g.</para>
<@xmlcode>
<!-- remove the next two statements after debugging is finished -->
<output name="log">
<default attribute="appendMessagesTo" value="/output/log"/>
</@xmlcode>

<para>Note that when
using async calls, the order of the response messages is not defined.</para>

</sect2>

<sect2><title>Using the default mechanism</title>
<para>The async, showSoap,  namespace, ignoreSoapFault and appendMessagesTo
attributes will use a default attribute if set with the default
command. 
</para>
<para>If no async
attribute is set directly or using the default command, it will
default to false.</para>
<para>If no showSOAP
attribute is set directly or using the default command, it will
default to false.</para>
<para>If no ignoreSoapFault
attribute is set directly or using the default command, it will
default to false.</para>
<para>If no appendMessagesTo
attribute is set directly or using the default command, it will
default to none, which means that nothing is appended. It is
currently not possible to unset this attribute once set using the
default mechanism.</para>
<para>If no namespace
attribute is set directly or using the default command this is an
error.</para>

</sect2>
</sect1>


<sect1><title>delete
command</title>
<para>This command delete a specific node in a XML expression.</para>
<@xmlcode>
<delete node="<replaceable>xmlExpression</replaceable>"/>
</@xmlcode>

</sect1>



<sect1><title>fault
command</title>
<para>This command will abort further execution of the script, and
return a SOAP:Fault to the caller. Currently it is only possible to
specify a faultcode and a faultstring (called the message). In future
it might be possible to add additional fields (such as faultactor and
details).</para>
<@xmlcode>
<fault code="<replaceable>fixedCode</replaceable>" message="<replaceable>textExpression</replaceable>"/>
</@xmlcode>
</sect1>



<sect1><title>sleep
command</title>
<para>This is a very simple command, that just pause the execution of a
script for a given interval in milliseconds. This is mainly useful
for testing and debugging purposes.</para>
<@xmlcode>
<sleep millis="<replaceable>milliseconds</replaceable>"/>
</@xmlcode>
</sect1>



<sect1 id="createXmlVar"><title>createXmlVar command</title>
<@xmlcode>
<createXmlVar var="<replaceable>varname</replaceable>" value="<replaceable>xml-expression</replaceable>"/>
</@xmlcode>

<para>This command defines a new XML variable
that can be used in XML expressions. In an older version this command
was called xml. This could be confusing, and the new name seems more
precise in what it does. It is expected that this command does not
need to be used very often, so the slightly longer name is not a
problem.</para>
</sect1>




<sect1><title>stripPrefixes
command</title>
<@xmlcode>
<stripPrefixes xml="<replaceable>xml-expression</replaceable>" | childrenOf="<replaceable>xml-expression</replaceable>"
	[recursive="true|false"]
/>
</@xmlcode>
<para>This command strips a XML expression of
the namespace prefixes. The xml-expression is either indicated by the
xml or the childrenOf attribute. Exactly one of these attributes
should be present:</para>
<itemizedlist>
	<listitem><para>When the xml attribute is used,
	this xml node is stripped of the prefix and if the recursive flag is
	present all it's direct and indirect children are stripped as well
	recursively.</para></listitem>
	<listitem><para>When the childrenOf attribute is
	used, this xml node itself is <emphasis>not </emphasis>stripped of the prefix, but
	it's direct children are stripped. If the recursive flag is present
	all their direct and children are stripped as well recursively.</para></listitem>
</itemizedlist>
<para>Typically one would strip the input xml
of all prefixes and possibly leave the toplevel node unchanged (since
one usually doesn't reference this by name).</para>

<@xmlcode>
<stripPrefixes childrenOf="/input"/>
</@xmlcode>
</sect1>



<sect1><title>switch command</title>
<@xmlcode>
<switch expression="<replaceable>text-expression</replaceable>"/>
  <case value="<replaceable>value</replaceable>">
    <!-- any script -->
  </case>
  <!-- any more cases -->
  [<otherwise>
    <!-- any script -->
  </otherwise>
</@xmlcode>

<para>This construct is similar to the
switch/case statement found in many programming languages.</para>
<para>The example below shows a simple usage.
</para>
<@xmlcode>
<implementation type="RelayCall"> 
  <switch expression="/input"> 
    <case value="1"><output text="one"/></case>
    <case value="2"><output text="two"/></case>
    <case value="3"><output text="three"/></case>
    <case value="4"><output text="four"/></case>
    <case value="5"><output text="five"/></case>
    <case value="6"><output text="sixe"/></case>
    <case value="7"><output text="seven"/></case>
    <case value="8"><output text="eight"/></case>
    <case value="9"><output text="nine"/></case>
    <case value="10"><output text="ten"/></case>
    <otherwise><output text="unknown number"/></otherwise>
  </switch> 
</implementation>
</@xmlcode>
<para>In the example
above, each case just contains a simple output statement, but a case
element (and the otherwise element), may contain any script as
described in this manual. A more complicated example is shown below,
where switch statements are nested:</para>
<@xmlcode>
<implementation type="RelayCall"> 
  <switch expression="/input/language"> 
    <case value="en"> 
      <output name="language" text="english"/> 
      <switch expression="/input/value"> 
        <case value="1"><output name="translation" text="one"/></case> 
        <case value="2"><output name="translation" text="two"/></case> 
        <case value="3"><output name="translation" text="three"/></case> 
        <otherwise><output name="translation" text="unknown"/></otherwise> 
      </switch>
    </case> 
    <case value="nl"> 
      <output name="language" text="dutch"/> 
      <switch expression="/input/value"> 
        <case value="1"><output name="translation" text="een"/></case> 
        <case value="2"><output name="translation" text="twee"/></case> 
        <case value="3"><output name="translation" text="drie"/></case> 
       <otherwise><output name="translation" text="onbekend"/></otherwise> 
      </switch>
    </case> 
    <otherwise>
      <output name="language" text="unknown"/> 
      <output name="translation" text="unknown"/> 
    </otherwise>
  </switch> 
</implementation>
</@xmlcode>
</sect1>



<sect1><title>var
command</title>
<@xmlcode>
<var name="<replaceable>varname</replaceable>" value="<replaceable>text-expression</replaceable>"/>
</@xmlcode>

<para>This command defines a text variable,
similar to the xml command that
defines a XML variable. 
</para>
</sect1>
</chapter>


<chapter><title>HttpConnector specific commands</title>
<para>The HttpConnector is a basically the RelayConnector with some
extra HTTP related commands added. If you do not need HTTP
functionality it is recommended that you use the RelayConnector
instead. This chapter describe the commands that are specific to the
HttpConnector.</para>


<sect1><title>http
command</title>
<@xmlcode>
<http application="<replaceable>host-config-expression</replaceable>" 
  url="<replaceable>url-expression</replaceable>"
  [resultVar="<replaceable>var</replaceable>"] 
  [prettyPrint="true|false"]
  [timeout="<replaceable>millisecs</replaceable>"]
  [body="<replaceable>xml-expression</replaceable>"]
>
  [<header key="" value=""> ...]
</http>
</@xmlcode>

<para>This command http will make a HTTP call to web server. The web
server url is made up of two parts. The first part is a host
configuration that is configured in the properties file<emphasis>. </emphasis>The
second part is the url attribute in this command. In this way, one
can configure the details (hostname, port, autenticattion
credentials, etc) of a certain webservice application in the
properties file only once, and use this in many method
implementations with different url's.</para>
<para>The application attribute is an
expression, instead of a fixed text. This makes it possible to use an
application like "JF+${dollar}{omg}", which would translate into,
e.g.  "JFFAT" or "JFPRD", depending on a FAT or
PRD environment.</para>
<para>The result of the call is stored in a
xml variable and can be used in next steps of the script. If now
resultVar is specified, the application name will be used. If you
just want to relay this response back as output the http-relay
command will be better, since it has more sophisticated merging of
the response in the methods output.</para>
<para>The prettyPrint variable will control
if the XML is prettified using whitespace. The default is false,
because this will be more efficient, and easier to handle if one uses
httpclient.wire logging.</para>
<para>The timeout variable will control how long the call will wait for an answer.
If no timeout is specified this will default to 30000 (i.e. 30 seconds). 
A timeout of zero means waiting indefinitely.</para>
<para>The body is the xml that is to be
sent. If no body is specified the expression "/input/../.."
is used. This will send the original input, including the
SOAP:Envelope.</para>
<para>Optionally, one can specify one or
more http-headers, using simple key value pairs. 
</para>
<para>The following example will send the entire SOAP message (including
envelope, header and body tags) to the application identified by TST.
 The response will be stored in the XML variable TST, and send as
body in the output. This output will currently be wrapped with
generated Envelope, Header and body from Cordys. 
</para>
<@xmlcode>
<http application="TST" url="demo/echo.php" 
  body="<replaceable>/input/../..</replaceable>"/>
<output xml="/TST"/>
</@xmlcode>

<para>In the HttpConnector.properties file
one should enter something like:</para>

<@xmlcode>
http.host.TST.url=http://10.10.10.103/test/
</@xmlcode>

</sect1>



<sect1><title>http-relay
command</title>
<@xmlcode>
<http-relay
  application="<replaceable>host-config-expression</replaceable>"
  url="<replaceable>url-expression</replaceable>"
  [prettyPrint="true|false"]
  [timeout="<replaceable>millisecs</replaceable>"]
  [body="<replaceable>xml-expression</replaceable>"]
  [wsa="true" replyTo="<replaceable>url-expression</replaceable>" 
    [faultTo="<replaceable>url-expression</replaceable>"]
    [wrapperName="<replaceable>element-name</replaceable>"]
    [wrapperNamespabe="<replaceable>namespace</replaceable>"]
  ]
>
  [<header key="" value=""> ...]
</http-relay>
</@xmlcode>

<para>This http-relay command is identical to the http command with two
exceptions.</para>
<itemizedlist>
	<listitem><para>The response of the http call is not stored in a xml-var but
	is merged with the response of the method call.</para></listitem>
	<listitem><para>There are additional parameters to support the asynchronous
	WS-A addressing ReplyTo mechanism.</para></listitem>
</itemizedlist>
<para>If the attribute wsa is set to true, one must also specify a
replyTo attribute. The call is the expected to an asynchronous
webservice call where the response will be send to the WS-A ReplyTo
endpoint. The http-relay will wrap the original ReplyTo endpoint, and
substitute a new ReplyTo endpoint as indicated by the replyTo
attribute, which should be configured to the address of the ESB.  See
the example in Chapter 6.3.</para>
<para>One can change the qualified name of the wrapper element if one
really wants to, but usually one can leave these at the default
settings, which are also the defaults of the http-callback command.</para>
</sect1>



<sect1><title>http-callback
command</title>
<@xmlcode>
<http-callback
  [timeout="<replaceable>millisecs</replaceable>"]
  [wrapperName="<replaceable>element-name</replaceable>"]
  [wrapperNamespabe="<replaceable>namespace</replaceable>"]/>
</@xmlcode>


<para>The http-callback command is
extremely simple. See the example in Chapter 6.3,
how it should be used.</para>
<para>One can change the qualified name of
the wrapper element if one really wants to, but usually one can leave
these at the default settings, which are also the defaults of the
http-callback command.</para>

</sect1>
</chapter>


<chapter><title>Examples</title>
<sect1><title>Hello World</title>
<@xmlcode>
<implementation type="RelayCall">
  <output text="Hello World!"/>
</implementation>
</@xmlcode>
</sect1>



<sect1><title>Echo</title>
<@xmlcode>
<implementation type="RelayCall">
  <output xml="/input"/>
</implementation>
</@xmlcode>
</sect1>



<sect1><title>WS-A asynchronous call</title>
<para>An asynchronous webservice call can be implemented using the
http-relay command with the wsa attribute set to op true in
combination of a http-callback command for handling the response.</para>
<para>For the call one would use something like</para>
<@xmlcode>
<implementation type="RelayCall">
  <http-relay application="APP"
url="..." <emphasis>wsa="true" replyTo="${dollar}{esbUrl}"</emphasis>/>
</implementation>
</@xmlcode>
<para>If one would send a message to this service it should have a WS-A
ReplyTo element. (It should have othe WS-A mandatory elements (like
MessageId) as well, but these are just passed though by the
http-relay command).</para>
<@xmlcode>
<SOAP:Envelope
xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP:Header>
    <emphasis><wsa:ReplyTo xmlns:wsa="http://www.w3.org/2005/08/addressing"></emphasis>
    <emphasis>http://192.168.10.11/someService</emphasis>
    <emphasis></wsa:ReplyTo></emphasis>
  </SOAP:Header>
  <SOAP:Body>
    <AsyncRequest xmlns="urn:test">....</AsyncRequest>
  </SOAP:Body>
</SOAP:Envelope>
</@xmlcode>

<para>The http-relay command will wrap this ReplyTo element in a manner
that it will be passed back in the response, and will add a new
ReplyTo element as specified by the replyTo attribute.</para>
<para>The wrapping of the original ReplyTo element is doen using the
WS-A  element ReferenceParameters, of which the contents will be
passed back in the response, according to the WS-A standard.</para>

<@xmlcode>
<SOAP:Envelope xmlns:wsa="http://www.w3.org/2005/08/addressing"
 xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP:Header >
    <!-- cordys specific headers removed -->
    <<emphasis>wsa:ReferenceParameters </emphasis>xmlns:wsa="http://www.w3.org/2005/08/addressing">
	<<emphasis>kisst:CallbackWrapper</emphasis> xmlns:kisst="http://kisst.org/cordys/http">
	  <ReplyTo xmlns="http://www.w3.org/2005/08/addressing">
		<emphasis>http://192.168.10.11/someService</emphasis>
	  </ReplyTo>
	</<emphasis>kisst:CallbackWrapper</emphasis>>
    </<emphasis>wsa:ReferenceParameters</emphasis>>
     <wsa:ReplyTo xmlns:wsa="http://www.w3.org/2005/08/addressing">
      <emphasis>http://esb.company.com/cordys/wcpgateway?org=...</emphasis>
    </wsa:ReplyTo>
  </SOAP:Header>
  <SOAP:Body>
    <AsyncRequest xmlns="urn:test">....</AsyncRequest>
  </SOAP:Body>
</SOAP:Envelope>
</@xmlcode>

<para>When the webservice sends back the response (which may be days
later), it will do so to the new ReplyTo address, which is the Cordys
ESB machine. For this response to be handled a second method needs to
be created, which will know how to handle this response. The method
can be very simple and will look as follows:</para>
<@xmlcode>
<implementation type="RelayCall">
  <http-callback />
</implementation>
</@xmlcode>

<para>The response should include the contents of the
ReferenceParameters of the original request and should  thus look
something like this:</para>
<@xmlcode>
<SOAP:Envelope
xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP:Header>
    <CallbackWrapper xmlns="http://kisst.org/cordys/http">
        <ReplyTo xmlns="http://www.w3.org/2005/08/addressing">
		<emphasis>http://192.168.10.11/someService</emphasis>
        </ReplyTo>
    </CallbackWrapper>
  </SOAP:Header>
  <SOAP:Body>
    <AsyncResponse xmlns="urn:test">....</AsyncResponse>
  </SOAP:Body>
</SOAP:Envelope>
</@xmlcode>

<para>The http-callback command will then make a http call to
http://192.168.10.11/someService,
and pass through the response. No specification of a website url is
needed since this is all contained in the ReplyTo elelement of the
response.</para>

</sect1>
</chapter>



<chapter id="configuration"><title>Configuration</title>
<para>Currently only some simple configuration is supported. In the
future the entire configuration system might be changed to a more
powerful system. 
</para>




<sect1><title>Script variables</title>
<para>Any variable defined in the properties file, can be used in the
script, using the ${dollar}{..} notation.  For example if the properties file
would contain a line like:</para>
<para>esbUrl=http://192.168.10.10/cordys/wcpgateway.wcp?....</para>
<para>this can be used in a method implementation as ${dollar}{esbUrl}.</para>
</sect1>



<sect1><title>relay.cacheScript
configuration</title>
<para>If this variable is set to true,
implementation scripts are compiled to an internal representation on
the first execution of a script, and this compiled representation is
cached for future invocations. In theory this could give a better
performance, but some simple tests did not show any noticeable
performance improvements. The cache is cleared if the processor is
reset. 
</para>

<para>The default value of this configuration
variable is false. In this case for each invocation the script is
compiled again. The big advantage of this setting is that during the
development one does not need to restart or reset the SOAP processor
each time the script has been edited (even though Cordys always warns
that this is necessary). 
</para>
</sect1>



<sect1><title>relay.timeout
configuration</title>
<para>This value is used to determine the default timeout in
milliseconds when calling a method. The default value is 20000 (20
seconds).</para>
</sect1>



<sect1><title>relay.logSoapFaults
configuration</title>
<para>This value is used to determine if soap faults that are received
need to be logged. The default value is false, because these are
faults on some external system, and should be logged there, and
probably at the client as well. However for troubleshooting this
setting might be useful.</para>
</sect1>



<sect1><title>http.ignoreReturnCode
configuration</title>
<para>If this setting is true, a returncode >= 300 will not be
considered an error. The default value is false. This setting may be
removed in the future. It was inspired by the problems when HTTP 202
was considered an error, which caused major problems, but it seems
very unlikely that this will be needed in the future.</para>
</sect1>



<sect1><title>http.wireLogging
configuration</title>
<para>This setting can be used to very easily set some debugging
logging, while not getting clobbered with irrelevant debugging
logging. It can be set to various logLevels. The two most important
are DEBUG and INFO. 
</para>
<para>If the setting is set to DEBUG all httpclient.wire logging is
shown. This will show the exact HTTP traffic which can help a lot
during trouble shooting.</para>
<para>If the setting is set to INFO the request and response XML of the
method are logged. This should be configured in a different way, but
was a quick hack and will probably be changed in future.</para>
<para>The entire logging will be changed in sometime to allow more
flexible log4j configuration.</para>
<para>Note: One can change this logging dynamically by resetting the
SOAP processor. However, if one was to remove this variable after it
had been set before, the log4j will still use the old setting.</para>
</sect1>
</chapter>




<chapter><title>Change history</title>
<para>There is currently no very strict release or versioning mechanism.
This is partly due to the fact that there are two different products
(RelayConnector and HttpConnector) built from the same source, and in
part due to the fact that the most important user of both connectors
currently uses an internal versioning repository with two separate
trees for the HttpConnector and the RelayConnector.</para>
<para>For the time being, the buildnumber is considered the most
accurate version number.  When the buildnumber is changed this is
commited in git with a clear comments stating such. Any changes after
that commit should be considered a new release</para>


<sect1><title>Changes since build 16</title>
<itemizedlist>
	<listitem><para>The prettyPrint attribute in the http commands, now defaults
	to false. When using httpclient wire logging, each newline was
	logged in a separate command, which made it very difficult to handle
	(e.g. cut and paste) such XML logging. Furthermore this should be a
	bit more efficient.</para></listitem>
	<listitem><para>The application attribute in the http commands is made an
	expression, instead of a fixed text. This makes it possible to use
	an application like "JF+${dollar}{omg}", which would translate
	into, e.g.  "JFFAT" or "JFPRD", depending on a
	FAT or PRD environment.</para></listitem>
	<listitem><para>Added predefined variable ${dollar}{plus} which is defined as the
	string "+", for escaping this special character.</para></listitem>
</itemizedlist>
</sect1>
</chapter>
