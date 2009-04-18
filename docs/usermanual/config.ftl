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