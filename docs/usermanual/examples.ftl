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
