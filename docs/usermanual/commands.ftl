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
<call method="<replaceable>method-name"|</replaceable>methodExpression="<replaceable>expr</replaceable>"
  [namespace="<replaceable>namespace"]</replaceable>|namespaceExpression="<replaceable>expr</replaceable>"]
  [async="true|false"]*
  [resultVar="<replaceable>result-var-name</replaceable>"]
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
If the resultVar attribute is not set,
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


<sect2><title>Using the default mechanism</title>
<para>The async and namespace attributes will use a default attribute 
if set with the default command. 
</para>
<para>If no async
attribute is set directly or using the default command, it will
default to false.</para>
<para>If no namespace attribute is set directly or using the default command this is an
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



<sect1><title>var command</title>
<@xmlcode>
<var name="<replaceable>varname</replaceable>" value="<replaceable>text-expression</replaceable>"/>
</@xmlcode>

<para>This command defines a text variable, similar to the xml command that defines a XML variable. 
</para>
</sect1>

<sect1><title>replaceText command</title>
<@xmlcode>
<replaceText 
  start="<replaceable>xmlexpression</replaceable>" 
  [elementsNamed="<replaceable>elementName</replaceable>"]
  expression="<replaceable>expr</replaceable>"/>
</@xmlcode>

<para>
This command replaces the text content of one element or of all (child) elements with a certain name.
This command is a special solution for changing a recurring element, because there is currently no iteration possibility.
</para>

<para>
The start attribute denotes some xml node to start with. 
If the attribute elementsNamed is ommited only the text of this element is changed.
If the attribute elementsNamed is specified, all direct and indirect children are searched. 
If any element is found with the specified name (with or without any prefix), it will be changed.
The new text of any changed element will be determined by the expression attribute. 
In this expression, one can use the special variable ${r"${it}"}, which contains the old text of the element.
</para>

<para>
A simple script shows how this might work
<@xmlcode>
<implementation type="RelayCall">
  <output xml="/input"/>
  <replaceText start="/output" elementsNamed="name" expression="Hello + ${r"${it}"}	"/>
</implementation>
</@xmlcode>
If one would the above method with the following input
<@xmlcode>
<ReplaceTest xmlns="http://schemas.kisst.org/cordys/relay/test">
  <name>Mark</name>
  <e>
    <name>Joost</name>
    <name>Edde</name>
    <name>Mark</name>
  </e>
</ReplaceTest>
</@xmlcode>

the method will return 
<@xmlcode>
<data>
  <ReplaceTestResponse xmlns="http://schemas.kisst.org/cordys/relay/test">
    <ReplaceTest xmlns="http://schemas.kisst.org/cordys/relay/test">
      <name>HelloMark</name>
      <e>
        <name>HelloJoost</name>
        <name>HelloEdde</name>
        <name>HelloMark</name>
      </e>
    </ReplaceTest>
  </ReplaceTestResponse>
</data>
</@xmlcode>
Note that elements at various depths are replaced.
</para>
</sect1>

<sect1><title>getConfigValue command</title>
<@xmlcode>
<getConfigValue 
  key="<replaceable>expression</replaceable>" 
  resultVar="<replaceable>varName</replaceable>"
  [default="<replaceable>expression</replaceable>"]
/>
</@xmlcode>
<para>
This command can be used to retrieve a value from the configuration file. 
If you use a fixed name as key that this command is essentialy the same as:
<@xmlcode>
  <var name="<replaceable>varName</replaceable>" value="${dollar}{<replaceable>key</replaceable>}" />
</@xmlcode>
The big difference is that in the getConfigValue, the key can be any expression.
This means one can use this command to get a dynamically determined key from the config file.
One of the main purposes of this command is to use it as a very simple Map mechanism.
For example with the following step:
<@xmlcode>
<getConfigValue key="config.greeting. + /input/languauge" resultVar="greeting"/>
</@xmlcode>
One could define different greetings in the configuration file as follows:
<@xmlcode>
config.greeting.nl = Goedendag
config.greeting.en = Hello
config.greeting.de = Guten Tag
</@xmlcode>
This way one can easily add new languages in the configuration file.
</para>
<para>
The default attribute can be used to specify a default value to be used if the key can not be found.
If no default attribute is present, and the key can not be found, an error will be thrown.
</para>
</sect1>


<sect1><title>soapMerge command</title>
<@xmlcode>
<soapMerge
  src="<replaceable>xmlexpression</replaceable>" 
  dest="<replaceable>xmlexpression</replaceable>" 
/>
</@xmlcode>
<para>
Merges two soap messages, meant to merge a response into the predefined response that the Cordys framework
already has available (i.e. the /output/../.. variable).
This merges the SOAP:Header fields from the src xml into the destination.
It discards the entire body, and replaces this with the body of the src. 
Otherwise there would be two element under the body.
</para>
</sect1>

<sect1><title>wsaTransformReplyTo command</title>
<@xmlcode>
<wsaTransformReplyTo
  xml="<replaceable>xmlexpression</replaceable>" 
  replyTo="<replaceable>expression</replaceable>" 
  [faultTo="<replaceable>expression</replaceable>"]
/>
</@xmlcode>
<para>
This commands changes the provided XML, in a way that it wraps the original WSA ReplyTo SOAP:Header,
and replaces it with the provided replyTo address.
This is done in a way that the HttpRelayCallback command can extract the original ReplyTo Address.
Typically the replyTo attribute should contain the url of the ESB machine. 
Of course it would be best to use a configuration variable for this, e.g. 
<@xmlcode>
    <wsaTransformReplyTo  xml="/input/../.."  replyTo="${EsbUrl}" />
</@xmlcode>
The faultTo attribute works similarily. A problem is that Cordys can not define methods with the
qulisfied name SOAP:Fault.
</para>
</sect1>

</chapter>
