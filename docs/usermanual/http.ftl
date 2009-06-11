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
  [xmlResponse="<replaceable>boolean</replaceable>"]
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

<para>If the attribute xmlResponse is false, the http response will be placed in a textvariable.
The default value is true, which means that the response will be placed in a xml variable.</para>

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
  [xmlResponse="<replaceable>boolean</replaceable>"]
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
<para>If the attribute xmlResponse is false, the http response will be ignored.
This is especially useful in combination when the response is empty which might be the case
when using wsa=true.
The default value of xmlResponse is true.</para>
</sect1>



<sect1><title>http-callback
command</title>
<@xmlcode>
<http-callback
  [timeout="<replaceable>millisecs</replaceable>"]
  [wrapperName="<replaceable>element-name</replaceable>"]
  [wrapperNamespabe="<replaceable>namespace</replaceable>"]/>
  [xmlResponse="<replaceable>boolean</replaceable>"]
</@xmlcode>


<para>The http-callback command is
extremely simple. See the example in Chapter 6.3,
how it should be used.</para>
<para>One can change the qualified name of
the wrapper element if one really wants to, but usually one can leave
these at the default settings, which are also the defaults of the
http-callback command.</para>
<para>If the attribute xmlResponse is false, the http response will be ignored.
The default value of xmlResponse is true.</para>

</sect1>
</chapter>
