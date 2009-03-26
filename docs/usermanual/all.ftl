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
a small XML script in the &#34;implementation&#34; section of this
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
the script has executed (see Chapter 4.6). Additionally it is
possible to define new variables by hand (see the xml
command in Chapter 4.10), but it is not sure if this feature is
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


<sect2><title>XML expressions</title>
<para>A XML expression starts with a XML
variable name (thus with a leading slash), selecting an existing XML
variable, or a children using a path like construction.  See the
example below.</para>
<@xmlcode>
  &#60;!-- appends the entire input message to output --&#62;
  &#60;output xml=&#34;/input&#34;/&#62;
  &#60;!-- appends a part of the input message to output --&#62;
  &#60;output xml=&#34;/input/tuple/old&#34;/&#62;
</@xmlcode>
<para>These expressions are still very
limited. There is no support for recurring elements with the same
name, for selecting attributes or for selecting all children without
the surrounding element.</para>

<para>There is support for namespaces. First
one must map a prefix to the correct namespace using the xmlns
command (see chapter 4.3). Next one can use this prefix within the
xml expression.</para>

<para>If the syntax .. is used in a path, the
parent element will be returned. This can be used to access the SOAP
header of the input. The following example will return the SOAP
header from the call.</para>

<@xmlcode>
&#60;implementation type=&#34;RelayCall&#34;&#62;
  &#60;xmlns prefix=&#34;SOAP&#34; namespace=&#34;http://schemas.xmlsoap.org/soap/envelope/&#34;/&#62; 
  &#60;output xml=&#34;/input/../../SOAP:Header&#34;/&#62;
&#60;/implementation&#62;
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
predefined variable ${dollar}{plus}, e.g. to use &#34;C++&#34; in a
expression, one could encode this as &#34;C+${dollar}{plus}+${dollar}{plus}&#34;. 
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
&#60;output 
	[name=&#34;<replaceable>element-name</replaceable>&#34;] 
	[prefix=&#34;prefix<replaceable>-name</replaceable>&#34; [scriptXmlns=&#34;false&#34;]] 
	[namespace=&#34;<replaceable>namespace</replaceable>&#34; [reduceXmlns=&#34;false&#34;]] 
	[text=&#34;<replaceable>text-expression</replaceable>&#34;]
	[xml=&#34;<replaceable>xml-expression</replaceable>&#34;] 
	[childrenOf=&#34;<replaceable>xml-expression</replaceable>&#34;]
&#62;
  &#60;!-- any of the following elements in any order, as many times as needed --&#62;
  &#60;element 
	[name=&#34;<replaceable>element-name</replaceable>&#34;] 
	[prefix=&#34;prefix<replaceable>-name</replaceable>&#34; [scriptXmlns=&#34;false&#34;]] 
	[namespace=&#34;<replaceable>namespace</replaceable>&#34; [reduceXmlns=&#34;false&#34;]] 
	[text=&#34;<replaceable>text-expression</replaceable>&#34;]
	[xml=&#34;<replaceable>xml-expression</replaceable>&#34;] 
	[childrenOf=&#34;<replaceable>xml-expression</replaceable>&#34;]
  &#62;
    &#60;!-- nested elements, attributes, etc if needed --&#62;
  &#60;/element&#62;

  &#60;attribute name=&#34;<replaceable>attribute-name</replaceable>&#34; text=&#34;<replaceable>text-expression</replaceable>&#34; /&#62;
  &#60;text 
	[name=&#34;<replaceable>element-name</replaceable>&#34;]
	[prefix=&#34;prefix<replaceable>-name</replaceable>&#34; [scriptXmlns=&#34;false&#34;]] 
	[namespace=&#34;<replaceable>namespace</replaceable>&#34; [reduceXmlns=&#34;false&#34;]] 
	[text=&#34;<replaceable>text-expression</replaceable>&#34;]&#62;	[<replaceable>fixed-text</replaceable>]&#60;/text&#62;
  &#60;cdata 

	[name=&#34;<replaceable>element-name</replaceable>&#34;]
	[prefix=&#34;prefix<replaceable>-name</replaceable>&#34; [scriptXmlns=&#34;false&#34;]] 
	[namespace=&#34;<replaceable>namespace</replaceable>&#34; [reduceXmlns=&#34;false&#34;]] 

	[text=&#34;<replaceable>text-expression</replaceable>&#34;]&#62;[<replaceable>fixed-text</replaceable>]&#60;/cdata&#62;
  &#60;include ..../&#62; &#60;!-- synonym for element --&#62;
&#60;/output&#62;
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
&#60;output text=&#34;hello world!&#34;/&#62;

&#60;!-- is shorthand for --&#62;

&#60;output&#62;
  &#60;text text=&#34;hello world!&#34;/&#62;
&#60;/output&#62;

&#60;output xml=&#34;/input/field1&#34;/&#62;

&#60;!-- is shorthand for --&#62;

&#60;output&#62;
  &#60;include xml=&#34;/input/field1&#34;/&#62;
&#60;/output&#62;


&#60;output childrenOf=&#34;/input/field1&#34;/&#62;

&#60;!-- is shorthand for --&#62;

&#60;output&#62;
  &#60;include childrenOf=&#34;/input/field1&#34;/&#62;
&#60;/output&#62;


&#60;output name=&#34;data&#34; .....&#62;
  &#60;!-- possibly extra elements --&#62;
&#60;/output&#62;

&#60;!-- is shorthand for --&#62;

&#60;output&#62;
  &#60;element name=&#34;&#34; ....&#62;
    &#60;!-- possibly extra elements --&#62;
  &#60;/element&#62;
&#60;/output&#62;
</@xmlcode>
<para>And the same example with a new element name.</para>
<@xmlcode>
&#60;output name=&#34;data&#34; text=&#34;hello world!&#34;/&#62;

&#60;!-- is shorthand for --&#62;

&#60;output&#62;
  &#60;element name=&#34;data&#34; text=&#34;hello world!&#34;/&#62;
&#60;/output&#62;

&#60;!-- is shorthand for --&#62;

&#60;output&#62;
  &#60;element name=&#34;data&#34;&#62;
    &#60;text text=&#34;hello world!&#34;/&#62;
  &#60;/element&#62;
&#60;/output&#62;
</@xmlcode>

</sect1>



<sect1><title>examples</title>

<para>The example below shows some the main
elements</para>
<@xmlcode>
  &#60;output name=&#34;demo&#34;&#62;
    &#60;attribute name=&#34;optional&#34; text=&#34;true&#34;/&#62;
    &#60;text text=&#34;Here one can use a text expression&#34;/&#62;
    &#60;element name=&#34;bold&#34; text=&#34;New York&#34;/&#62;
    &#60;text&#62;Static text can also be added in the element&#60;/text&#62;
  &#60;/output&#62;
</@xmlcode>


<para>This construct will create a new
element with the name &#34;demo&#34;. In this element a new
attribute named &#34;optional&#34; is created with  as value the
text &#34;true&#34;. Then some text is added, a new subelement
named &#34;bold&#34; is created, and finally some more text is
appended. 
</para>

<@xmlcode>
    &#60;demo optional=&#34;true&#34;&#62;
      Here one can use a text expression
      &#60;bold&#62;New York&#60;/bold&#62;
      Static text can also be added in the element
    &#60;/demo&#62;
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
	name will have this prefix, and a &#34;xmnls:<replaceable>prefix</replaceable>&#34;
	attribute will be added (if needed).</para></listitem>
	<listitem><para>If no prefix is given, the element
	name will have no prefix, using the default mechanism, and a &#34;xmnls&#34;
	attribute will be added (if needed).</para></listitem>
</itemizedlist>

</sect1>
</chapter>


<chapter><title>Command reference RelayConnector</title>
<sect1><title>import command</title>
<para>&#60;import name=&#34;<replaceable>commandname</replaceable>&#34;
class=&#34;<replaceable>classname</replaceable>&#34;/&#62;</para>
<para>This command defines a new command with
the name <replaceable>commandname</replaceable>. The command is implemented in a Java
class given by classname. The exact specifications of such a Java
class will be described elsewhere.</para>
</sect1>



<sect1><title>default command</title>
<para>&#60;default attribute=&#34;<replaceable>attribute-name</replaceable>&#34;
value=&#34;<replaceable>fixed-text</replaceable>&#34;/&#62;</para>
<para>This is a convenience function, that
allows to define a default value for certain attributes. Currently
the following attributes support this mechanism:</para>
<itemizedlist>
	<listitem><para>namespace</para></listitem>
	<listitem><para>async</para></listitem>
	<listitem><para>showSoap</para></listitem>
</itemizedlist>
</sect1>



<sect1><title>xmlns command</title>
<para>&#60;xmlns prefix=&#34;<replaceable>prefix</replaceable>&#34;
namespace=&#34;<replaceable>namespace</replaceable>&#34;/&#62;</para>
<para>This function defines a certain prefix
to be used to relate to a certain namespace. These prefixes can then
be used in subsequent XML expressions, see chapter 2.2.1.</para>
<para>It is considered to be an error to
define the same prefix twice.</para>
<para>In the example below the prefix &#34;ns&#34;
is defined and subsequently used.</para>
<@xmlcode>
&#60;xmlns prefix=&#34;<emphasis><replaceable>ns</replaceable></emphasis>&#34;
namespace=&#34;<replaceable>http://kisst.org/test</replaceable>&#34;/&#62;
&#60;output xml=&#34;/input/<emphasis>ns</emphasis>:node&#34;/&#62;
</@xmlcode>
</sect1>



<sect1><title>append
command</title>
<@xmlcode>
&#60;append to=&#34;<replaceable>xml-expression</replaceable>&#34;
    [name=&#34;<replaceable>name</replaceable>&#34;]
    [xml=&#34;<replaceable>xml-expression</replaceable>&#34;] 
    [text=&#34;<replaceable>text-expression</replaceable>&#34;]&#62;
  &#60;!-- see chapter 3 --&#62;
  &#60;element ..../&#62;
  &#60;attribute..../&#62;
  &#60;text ..../&#62;
  &#60;cdata..../&#62;
  &#60;include..../&#62;
&#60;/append&#62;
</@xmlcode>

<para>This command appends XML to an existing
XML structure. 
</para>

<@xmlcode>
&#60;append to=&#34;/output&#34;
name=&#34;city&#34;
text=&#34;New York&#34;/&#62;

&#60;append to=&#34;/output&#34;&#62;
  &#60;element name=&#34;city&#34; text=&#34;New York&#34;/&#62;
&#60;/append&#62;
</@xmlcode>
</sect1>



<sect1><title>output command</title>
<@xmlcode>
&#60;output
	[name=&#34;<replaceable>name</replaceable>&#34;]
	[rename=&#34;<replaceable>name</replaceable>&#34;]
	[xml=&#34;<replaceable>xml-expression</replaceable>&#34;] 

	[text=&#34;<replaceable>text-expression</replaceable>&#34;]&#62;
  &#60;!-- see chapter 3 --&#62;
  &#60;element ..../&#62;
  &#60;attribute..../&#62;
  &#60;text ..../&#62;
  &#60;cdata..../&#62;
  &#60;include..../&#62;
&#60;/output&#62;
</@xmlcode>
<para>This is a convenience function that is
a synonym for &#60;append to=&#34;/output&#34; .....</para>
<para>It has one special attribute: rename.
If this attribute is present, the output element will be renamed.
This option is provided, because by default Cordys will create an
output message with as name, the name of the input message, with the
postfix &#34;Response&#34;.</para>
</sect1>



<sect1><title>call command</title>
<@xmlcode>
&#60;call
method=&#34;<replaceable>method-name&#34;|</replaceable>methodExpression<replaceable>=&#34;expr&#34;
</replaceable>
  [namespace=&#34;<replaceable>namespace&#34;]</replaceable>*
  [async=&#34;true|false&#34;]*
  [showSoap=&#34;true|false&#34;]*
  [ignoreSoapFault=&#34;true|false&#34;]*
  [appendMessagesTo=&#34;<replaceable>xml-expression</replaceable>&#34;]*
  [resultVar=&#34;<replaceable>result-var-name</replaceable>&#34;]
  [timeout=&#34;<replaceable>millisec</replaceable>&#34;]

  [name<replaceable>=&#34;element-name&#34;</replaceable>]
  [xml=&#34;<replaceable>xml-expression</replaceable>&#34;] 
  [text=&#34;<replaceable>text-expression</replaceable>&#34;]
&#62;
  &#60;!-- see chapter 3 --&#62;
  &#60;element ..../&#62;
  &#60;attribute..../&#62;
  &#60;text ..../&#62;
  &#60;cdata..../&#62;
  &#60;include..../&#62;
&#60;/call&#62;
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
<para>If the
async
flag is true, the script sends the request message and will not wait
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
configured (see Chapter 7). 
</para>
</sect2>

<sect2><title>appendMessagesTo
attribute</title>
<para>The
appendMessagesTo attribute is mainly meant for debugging purposes. It
will append all request and response message to a XML element.
Usually you would set this using the default command to &#34;/output&#34;,
during development, to see what is happening, and remove that default
command, once the function works. 
</para>

<@xmlcode>
&#60;!-- remove the next statement after debugging is finished --&#62;
&#60;default attribute=&#34;appendMessagesTo&#34; value=&#34;/output&#34;/&#62;

&#60;!-- The input and output of these calls will be shown in the output --&#62;
&#60;call ...&#62;
&#60;call ...&#62;
</@xmlcode>

<para>You might also set
it to something like &#34;/output/log&#34;, but the one should
first append a &#34;log&#34; element to the output, e.g.</para>
<@xmlcode>
&#60;!-- remove the next two statements after debugging is finished --&#62;
&#60;output name=&#34;log&#34;&#62;
&#60;default attribute=&#34;appendMessagesTo&#34; value=&#34;/output/log&#34;/&#62;
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
<para>&#60;delete node=&#34;<replaceable>xmlExpression</replaceable>&#34;/&#62;</para>

</sect1>



<sect1><title>fault
command</title>
<para>This command will abort further execution of the script, and
return a SOAP:Fault to the caller. Currently it is only possible to
specify a faultcode and a faultstring (called the message). In future
it might be possible to add additional fields (such as faultactor and
details).</para>
<para>&#60;fault code=&#34;<replaceable>fixedCode</replaceable>&#34;
message=&#34;<replaceable>textExpression</replaceable>&#34;/&#62;</para>
</sect1>



<sect1><title>sleep
command</title>
<para>This is a very simple command, that just pause the execution of a
script for a given interval in milliseconds. This is mainly useful
for testing and debugging purposes.</para>
<para>&#60;sleep millis=&#34;<replaceable>milliseconds</replaceable>&#34;/&#62;</para>
</sect1>



<sect1><title>createXmlVar
command</title>
<para>&#60;createXmlVar var=&#34;<replaceable>varname</replaceable>&#34;
value=&#34;<replaceable>xml-expression</replaceable>&#34;/&#62;</para>
<para>This command defines a new XML variable
that can be used in XML expressions. In an older version this command
was called xml. This could be confusing, and the new name seems more
precise in what it does. It is expected that this command does not
need to be used very often, so the slightly longer name is not a
problem.</para>
</sect1>




<sect1><title>stripPrefixes
command</title>
<para>&#60;stripPrefixes xml=&#34;<replaceable>xml-expression</replaceable>&#34;
| childrenOf=&#34;<replaceable>xml-expression</replaceable>&#34;</para>
<para>	[recursive=&#34;true|false&#34;]</para>
<para>/&#62;</para>
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

<para>&#60;stripPrefixes childrenOf=&#34;/input&#34;/&#62;</para>
</sect1>



<sect1><title>switch
command</title>
<@xmlcode>
&#60;switch expression=&#34;<replaceable>text-expression</replaceable>&#34;/&#62;
  &#60;case value=&#34;<replaceable>value</replaceable>&#34;&#62;
    &#60;!-- any script --&#62;
  &#60;/case&#62;
  &#60;!-- any more cases --&#62;
  [&#60;otherwise&#62;
    &#60;!-- any script --&#62;
  &#60;/otherwise&#62;
</@xmlcode>

<para>This construct is similar to the
switch/case statement found in many programming languages.</para>
<para>The example below shows a simple usage.
</para>
<@xmlcode>
&#60;implementation type=&#34;RelayCall&#34;&#62; 
  &#60;switch expression=&#34;/input&#34;&#62; 
    &#60;case value=&#34;1&#34;&#62;&#60;output text=&#34;one&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;2&#34;&#62;&#60;output text=&#34;two&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;3&#34;&#62;&#60;output text=&#34;three&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;4&#34;&#62;&#60;output text=&#34;four&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;5&#34;&#62;&#60;output text=&#34;five&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;6&#34;&#62;&#60;output text=&#34;sixe&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;7&#34;&#62;&#60;output text=&#34;seven&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;8&#34;&#62;&#60;output text=&#34;eight&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;9&#34;&#62;&#60;output text=&#34;nine&#34;/&#62;&#60;/case&#62;
    &#60;case value=&#34;10&#34;&#62;&#60;output text=&#34;ten&#34;/&#62;&#60;/case&#62;
 
&#60;otherwise&#62;&#60;output text=&#34;unknown number&#34;/&#62;&#60;/otherwise&#62;
  &#60;/switch&#62; 
&#60;/implementation&#62;
</@xmlcode>
<para>In the example
above, each case just contains a simple output statement, but a case
element (and the otherwise element), may contain any script as
described in this manual. A more complicated example is shown below,
where switch statements are nested:</para>
<@xmlcode>
&#60;implementation type=&#34;RelayCall&#34;&#62; 
  &#60;switch expression=&#34;/input/language&#34;&#62; 
    &#60;case value=&#34;en&#34;&#62; 
      &#60;output name=&#34;language&#34; text=&#34;english&#34;/&#62; 
      &#60;switch expression=&#34;/input/value&#34;&#62; 
        &#60;case value=&#34;1&#34;&#62;&#60;output name=&#34;translation&#34; text=&#34;one&#34;/&#62;&#60;/case&#62; 
        &#60;case value=&#34;2&#34;&#62;&#60;output name=&#34;translation&#34; text=&#34;two&#34;/&#62;&#60;/case&#62; 
        &#60;case value=&#34;3&#34;&#62;&#60;output name=&#34;translation&#34; text=&#34;three&#34;/&#62;&#60;/case&#62; 
    &#60;otherwise&#62;&#60;output name=&#34;translation&#34; text=&#34;unknown&#34;/&#62;&#60;/otherwise&#62; 
      &#60;/switch&#62;
    &#60;/case&#62; 

    &#60;case
value=&#34;nl&#34;&#62; 
      &#60;output name=&#34;language&#34; text=&#34;dutch&#34;/&#62; 
      &#60;switch expression=&#34;/input/value&#34;&#62; 
        &#60;case value=&#34;1&#34;&#62;&#60;output name=&#34;translation&#34; text=&#34;een&#34;/&#62;&#60;/case&#62; 
        &#60;case value=&#34;2&#34;&#62;&#60;output name=&#34;translation&#34; text=&#34;twee&#34;/&#62;&#60;/case&#62; 
        &#60;case value=&#34;3&#34;&#62;&#60;output name=&#34;translation&#34; text=&#34;drie&#34;/&#62;&#60;/case&#62; 
       &#60;otherwise&#62;&#60;output name=&#34;translation&#34; text=&#34;onbekend&#34;/&#62;&#60;/otherwise&#62; 
      &#60;/switch&#62;
    &#60;/case&#62; 
    &#60;otherwise&#62;
      &#60;output name=&#34;language&#34; text=&#34;unknown&#34;/&#62; 
      &#60;output name=&#34;translation&#34; text=&#34;unknown&#34;/&#62; 
    &#60;/otherwise&#62;
  &#60;/switch&#62; 
&#60;/implementation&#62;
</@xmlcode>
</sect1>



<sect1><title>var
command</title>
<para>&#60;var name=&#34;<replaceable>varname</replaceable>&#34;
value=&#34;<replaceable>text-expression</replaceable>&#34;/&#62;</para>
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
&#60;http application=&#34;<replaceable>host-config-expression</replaceable>&#34; url=&#34;<replaceable>url-expression</replaceable>&#34;
  [resultVar=&#34;<replaceable>var</replaceable>&#34;] 
  [prettyPrint=&#34;true|false&#34;]
  [body=&#34;<replaceable>xml-expression</replaceable>&#34;]
&#62;
  [&#60;header key=&#34;&#34; value=&#34;&#34;&#62; ...]
&#60;/http&#62;
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
application like &#34;JF+${dollar}{omg}&#34;, which would translate into,
e.g.  &#34;JFFAT&#34; or &#34;JFPRD&#34;, depending on a FAT or
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
<para>The body is the xml that is to be
sent. If no body is specified the expression &#34;/input/../..&#34;
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
&#60;http application=&#34;TST&#34; url=&#34;demo/echo.php&#34; body=&#34;<replaceable>/input/../..</replaceable>&#34;/&#62;
&#60;output xml=&#34;/TST&#34;/&#62;
</@xmlcode>

<para>In the HttpConnector.properties file
one should enter something like:</para>

<para>http.host.TST.url=http://10.10.10.103/test/</para>

</sect1>



<sect1><title>http-relay
command</title>
<@xmlcode>
&#60;http-relay
application=&#34;<replaceable>host-config-expression</replaceable>&#34;
url=&#34;<replaceable>url-expression</replaceable>&#34;
  [prettyPrint=&#34;true|false&#34;]
  [body=&#34;<replaceable>xml-expression</replaceable>&#34;]
  [wsa=&#34;true&#34; replyTo=&#34;<replaceable>url-expression</replaceable>&#34; [faultTo=&#34;<replaceable>url-expression</replaceable>&#34;]
     [wrapperName=&#34;<replaceable>element-name</replaceable>&#34;] [wrapperNamespabe=&#34;<replaceable>namespace</replaceable>&#34;]
  ]
&#62;
  [&#60;header key=&#34;&#34; value=&#34;&#34;&#62; ...]
&#60;/http-relay&#62;
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
&#60;http-callback
[wrapperName=&#34;<replaceable>element-name</replaceable>&#34;]
[wrapperNamespabe=&#34;<replaceable>namespace</replaceable>&#34;]/&#62;
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
&#60;implementation type=&#34;RelayCall&#34;&#62;
  &#60;output text=&#34;Hello World!&#34;/&#62;
&#60;/implementation&#62;
</@xmlcode>
</sect1>



<sect1><title>Echo</title>
<@xmlcode>
&#60;implementation type=&#34;RelayCall&#34;&#62;
  &#60;output xml=&#34;/input&#34;/&#62;
&#60;/implementation&#62;
</@xmlcode>
</sect1>



<sect1><title>WS-A asynchronous call</title>
<para>An asynchronous webservice call can be implemented using the
http-relay command with the wsa attribute set to op true in
combination of a http-callback command for handling the response.</para>
<para>For the call one would use something like</para>
<@xmlcode>
&#60;implementation type=&#34;RelayCall&#34;&#62;
  &#60;http-relay application=&#34;APP&#34;
url=&#34;...&#34; <emphasis>wsa=&#34;true&#34; replyTo=&#34;${dollar}{esbUrl}&#34;</emphasis>/&#62;
&#60;/implementation&#62;
</@xmlcode>
<para>If one would send a message to this service it should have a WS-A
ReplyTo element. (It should have othe WS-A mandatory elements (like
MessageId) as well, but these are just passed though by the
http-relay command).</para>
<@xmlcode>
&#60;SOAP:Envelope
xmlns:SOAP=&#34;http://schemas.xmlsoap.org/soap/envelope/&#34;&#62;
  &#60;SOAP:Header&#62;
    <emphasis>&#60;wsa:ReplyTo xmlns:wsa=&#34;http://www.w3.org/2005/08/addressing&#34;&#62;</emphasis>
    <emphasis>http://192.168.10.11/someService</emphasis>
    <emphasis>&#60;/wsa:ReplyTo&#62;</emphasis>
  &#60;/SOAP:Header&#62;
  &#60;SOAP:Body&#62;
    &#60;AsyncRequest xmlns=&#34;urn:test&#34;&#62;....&#60;/AsyncRequest&#62;
  &#60;/SOAP:Body&#62;
&#60;/SOAP:Envelope&#62;
</@xmlcode>

<para>The http-relay command will wrap this ReplyTo element in a manner
that it will be passed back in the response, and will add a new
ReplyTo element as specified by the replyTo attribute.</para>
<para>The wrapping of the original ReplyTo element is doen using the
WS-A  element ReferenceParameters, of which the contents will be
passed back in the response, according to the WS-A standard.</para>

<@xmlcode>
&#60;SOAP:Envelope xmlns:wsa=&#34;http://www.w3.org/2005/08/addressing&#34;
 xmlns:SOAP=&#34;http://schemas.xmlsoap.org/soap/envelope/&#34;&#62;
  &#60;SOAP:Header &#62;
    &#60;!-- cordys specific headers removed --&#62;
    &#60;<emphasis>wsa:ReferenceParameters </emphasis>xmlns:wsa=&#34;http://www.w3.org/2005/08/addressing&#34;&#62;
	&#60;<emphasis>kisst:CallbackWrapper</emphasis> xmlns:kisst=&#34;http://kisst.org/cordys/http&#34;&#62;
	  &#60;ReplyTo xmlns=&#34;http://www.w3.org/2005/08/addressing&#34;&#62;
		<emphasis>http://192.168.10.11/someService</emphasis>
	  &#60;/ReplyTo&#62;
	&#60;/<emphasis>kisst:CallbackWrapper</emphasis>&#62;
    &#60;/<emphasis>wsa:ReferenceParameters</emphasis>&#62;
     &#60;wsa:ReplyTo xmlns:wsa=&#34;http://www.w3.org/2005/08/addressing&#34;&#62;
      <emphasis>http://esb.company.com/cordys/wcpgateway?org=...</emphasis>
    &#60;/wsa:ReplyTo&#62;
  &#60;/SOAP:Header&#62;
  &#60;SOAP:Body&#62;
    &#60;AsyncRequest xmlns=&#34;urn:test&#34;&#62;....&#60;/AsyncRequest&#62;
  &#60;/SOAP:Body&#62;
&#60;/SOAP:Envelope&#62;
</@xmlcode>

<para>When the webservice sends back the response (which may be days
later), it will do so to the new ReplyTo address, which is the Cordys
ESB machine. For this response to be handled a second method needs to
be created, which will know how to handle this response. The method
can be very simple and will look as follows:</para>
<@xmlcode>
&#60;implementation type=&#34;RelayCall&#34;&#62;
  &#60;http-callback /&#62;
&#60;/implementation&#62;
</@xmlcode>

<para>The response should include the contents of the
ReferenceParameters of the original request and should  thus look
something like this:</para>
<@xmlcode>
&#60;SOAP:Envelope
xmlns:SOAP=&#34;http://schemas.xmlsoap.org/soap/envelope/&#34;&#62;
  &#60;SOAP:Header&#62;
    &#60;CallbackWrapper xmlns=&#34;http://kisst.org/cordys/http&#34;&#62;
        &#60;ReplyTo xmlns=&#34;http://www.w3.org/2005/08/addressing&#34;&#62;
		<emphasis>http://192.168.10.11/someService</emphasis>
        &#60;/ReplyTo&#62;
    &#60;/CallbackWrapper&#62;
  &#60;/SOAP:Header&#62;
  &#60;SOAP:Body&#62;
    &#60;AsyncResponse xmlns=&#34;urn:test&#34;&#62;....&#60;/AsyncResponse&#62;
  &#60;/SOAP:Body&#62;
&#60;/SOAP:Envelope&#62;
</@xmlcode>

<para>The http-callback command will then make a http call to
http://192.168.10.11/someService,
and pass through the response. No specification of a website url is
needed since this is all contained in the ReplyTo elelement of the
response.</para>

</sect1>
</chapter>



<chapter><title>Configuration</title>
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
<para>If this setting is true, a returncode &#62;= 300 will not be
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
	an application like &#34;JF+${dollar}{omg}&#34;, which would translate
	into, e.g.  &#34;JFFAT&#34; or &#34;JFPRD&#34;, depending on a
	FAT or PRD environment.</para></listitem>
	<listitem><para>Added predefined variable ${dollar}{plus} which is defined as the
	string &#34;+&#34;, for escaping this special character.</para></listitem>
</itemizedlist>
</sect1>
</chapter>
