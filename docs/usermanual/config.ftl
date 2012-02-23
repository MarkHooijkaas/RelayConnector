<chapter id="configuration"><title>Configuration</title>
<para>
The RelayConnector has a powerful configuration system. 
It is possible to modify all kinds of settings, and one can even do this for individual
methods or for groups of methods.
</para>

<sect1><title>The properties "file"</title>
<para>
The configuration of the RelayConnector and HttpConnector are stored in a properties file.
For a SOAP processor one can specify the location of this file in the properties screen of the SOAP processor.
One can specify a normal file path, but one can also specify a path in the XMLStore. 
</para>

<para>
	Examples for windows systems:
	<itemizedlist>
	  <listitem><para><filename>D:/config/RelayConnector.properties</filename></para></listitem>
	  <listitem><para><filename>xmlstore:administrator@/config/RelayConnector.properties</filename></para></listitem>
	  <listitem><para><filename>xmlstore:cn=administrator,cn=organizational users,o=system,cn=cordys,o=kisst.org@/config/@PROJECT@.properties</filename></para></listitem>
	</itemizedlist>
	Examples for Linux systems:
	<itemizedlist>
	  <listitem><para><filename>/opt/config/RelayConnector.properties</filename></para></listitem>
	  <listitem><para><filename>xmlstore:root@/config/RelayConnector.properties</filename></para></listitem>
	  <listitem><para><filename>xmlstore:cn=root,cn=organizational users,o=system,cn=cordys,o=kisst.org@/config/RelayConnector.properties</filename></para></listitem>
	</itemizedlist>
	When referring to the XMLStore, a special xmlstore url is used.
	In this syntax the user before the @ sign will be used to retrieve the data from the XMLStore.
	This must be a user that has the authorization to retrieve objects from the XMLStore.
	The examples with the administrator or root user, are often a good choice, if this user exists.
</para>
<para>
	In the short format the user with that name from the organization of the SOAP processor will be used.
	Instead one may also user the LDAP dn of a user from a different organization (the third example).
</para>

<para>
Note: If one changes the location of the properties file on must restart the SOAP processor.
If one just change the contents of the properties file itself, one can just reset the SOAP processor.
</para>
</sect1>


<sect1><title>Script variables</title>
<para>Any variable defined in the properties file, can be used in the
script, using the ${dollar}{..} notation.  For example if the properties file
would contain a line like:</para>
<@xmlcode>
crm.queuename=SOMEQUEUE.AQ
</@xmlcode>

<para>this can be used in a method implementation as follows:</para>
<@xmlcode>
<implementation type="RelayCall">
  <call method="sendMessage" namespace="...">
    <element name="destination" text="<emphasis>${dollar}{crm.queuename}</emphasis>"/>
    ....
  </call>
</implementation>
</@xmlcode>

</sect1>

<sect1><title>logging and tracing settings</title>
<para>
There are several settings which influence the tracing/logging behavior of the RelayConnector. 
The idea of a trace is that it remembers a trace of all actions performed during the execution of one method.
With the default settings this trace is never shown, and is discarded after successful completion of the method.
In case of errors or for general troubleshooting one can tune this behavior, both in what is traced
and when and how it is shown.
The following settings are of interest
<variablelist>
<varlistentry><term><varname>relay.trace</varname></term>
<listitem><para>The detail level for tracing: can be DEBUG, INFO, WARN, ERROR. The default level is WARN.</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.logTrace</varname></term>
<listitem><para>If true all trace commands are send to the logging system.
The default value is true. 
Note that the logging settings still need to be set at a high enough level to be actually logged.</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.showTrace</varname></term>
<listitem><para>If this value is set to true, a SOAP fault will display the trace information in the details element.
For security reasons, this setting default to false, but it can be useful to set it to true in development or testing environments.</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.showStacktrace</varname></term>
<listitem><para>If this value is set to true, a SOAP fault will display a standard java stacktrace  in the details element.
For security reasons, this setting default to false, but it can be useful to set it to true in development or testing environments.</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.traceShowEnvelope</varname></term>
<listitem><para>If this value is set to true, several logging and tracing items will also show the Soap Envelope including the Soap header.
This value defaults to false, to give shorter, more functional, logging.</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.logRequestOnError</varname></term>
<listitem><para>If this value is set to true, the original request is logged when an error occurs.
The original request may give additional information about the problem.
The default setting is true.</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.logRelayedSoapFaults</varname></term>
<listitem><para>
This setting can be DEBUG, INFO, WARN or ERROR.
When a RelayConnector method calls some other SOAP method (with the <varname>call</varname> or the <varname>http-relay</varname> command)
and this call returns a SOAP fault, this setting determines at which level it will be logged.
The default setting is WARN. 
The philosophy is that these errors are external and should be logged and analyzed somewhere else.  
</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.timer</varname></term>
<listitem><para>
If this setting is set to true, a special INFO item will be logged after each call
logging if the call was succesful or not, and how many milliseconds have passed.
The default setting is false.
</para>
<para>
Note: The logging configuration of the RelayTimer loglevel must be set to INFO for this timing
log to be really seen.
</para></listitem>
</varlistentry>





</variablelist>
</para>  

</sect1>

<sect1><title>Other settings</title>
<variablelist>

<varlistentry><term><varname>relay.cacheScript</varname></term>
<listitem><para>
If this variable is set to true,
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
</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.timeout</varname></term>
<listitem><para>
This value is used to determine the default timeout in
milliseconds when calling a method. The default value is 20000 (20
seconds).
</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.emergencyBreak</varname></term>
<listitem><para>
If this setting is true, the method will not execute, but instead will always return a SOAP Fault.
The default setting obviously is false.
This setting should be used when a certain method has severe problems and should not be executed.
For example, the method may result in undesired functional mutation of data,
or the method may hang a certain system (e.g. by consuming threads that will never be released).
By using the emergencyBreak on a specific method one can crudely disable this method.
</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.sleepAfterCall</varname></term>
<listitem><para>
This setting is a crude mechanism to provide some primitive throttling.
It forces that after a call is made, the thread will sleep for some milliseconds.
This might be used if a backend system is overloaded.
The default value is 0 milliseconds.
</para></listitem>
</varlistentry>

<varlistentry><term><varname>relay.soapFaultcodePrefix</varname></term>
<listitem><para>
This setting determines the faultcode when an internal error occurs.
The default value is "TECHERR.ESB".
</para></listitem>
</varlistentry>

<varlistentry><term><varname>http.ignoreReturnCode</varname></term>
<listitem><para>
If this setting is true, a returncode >= 300 will not be
considered an error. The default value is false. This setting may be
removed in the future. It was inspired by the problems when HTTP 202
was considered an error, which caused major problems, but it seems
very unlikely that this will be needed in the future.
</para></listitem>
</varlistentry>

<varlistentry><term><varname>http.timeout</varname></term>
<listitem><para>
This setting is the default timeout (in millisecs) to be used in HTTP calls.
The default setting is 30.000 millisecs. 
Note that there are in fact more than 1 timeout value involved in HTTP calls.
Another timeout is how long will be waited for a TCP connection is accepted.
This timeout seems to be 5 seconds, and currently can not be influenced.
The <varname>http.timeout</varname> setting will influence how long the HTTP call will wait 
on an opende TCP socker for an answer.
</para></listitem>
</varlistentry>


</variablelist>

</sect1>


<sect1><title>caching configuration</title>
<para> 
The caching configuration is meant to be simple if you just want to add one or a few caches.
However it is also possible to use all the power of the ehcache caching framework.
The following main configuration variables are available:
<itemizedlist>
	<listitem><para><varname>relay.caches</varname> a list af simple defined caches</para></listitem>
	<listitem><para><varname>relay.cachemanager.file</varname> The filename containing a special ehcache config file</para></listitem>
	<listitem><para><varname>relay.cachemanager.url</varname> The url containing a special ehcache config file</para></listitem>
</itemizedlist>
The variable <varname>relay.caches</varname> should be a comma separated list (whitespaces do not matter).
For each cachename defined in <varname>relay.caches</varname>, one should define the following extra variables:
<itemizedlist>
	<listitem><para><varname>relay.cache.<replaceable>name</replaceable>.timeToLiveSeconds</varname></para></listitem>
	<listitem><para><varname>relay.cache.<replaceable>name</replaceable>.size</varname></para></listitem>
</itemizedlist>
Additionaly one may define caches in either <varname>relay.cachemanager.file</varname> or <varname>relay.cachemanager.url</varname>
using a standard ehcache configuration file. (if both variables are defined, the file variable is used).
</para>

<para>
For all caches one needs to define the following extra variables:
<itemizedlist>
	<listitem><para><varname>relay.cache.<replaceable>name</replaceable>.method</varname> a universal name with name space between { and } followed by the name of the method to be cached</para></listitem>
	<listitem><para><varname>relay.cache.<replaceable>name</replaceable>.keypath</varname> the path where the key can be found, relative to the method element</para></listitem>
</itemizedlist>
</para>

</sect1>

<sect1><title>Setting properties for individual methods</title>
<para>
One of the most powerful features of the configuration framework is that it is possible
to override some specific setting for a specific method or group of methods.
This is best explained by the following example of a configuration file:
<@xmlcode>
relay.timeout=20000
@method:{urn:some.namespace}VerySlowMethod {
	relay.timeout=600000
}

#disable a specific method
@method:{urn:some.other.namespace}IllBehavingMethod {
	relay.emergencyBreak=true
}

#log trace details only of one specific method
relay.trace=WARN
relay.logTrace=false
@method:{urn:some.other.namespace}IllBehavingMethod {
	relay.trace=DEBUG
	relay.logTrace=true
}
</@xmlcode>
Using the @method: prefix, one can specify a specific method, using 
the universal name. See http://www.jclark.com/xml/xmlns.htm
</para>
<para>
It is also possible to specify an override for all methods with a certain namespace.
This is done using the <varname>@namespace:</varname> prefix.
<@xmlcode>
relay.timeout=20000
@namespace:http://schemas.example.com/comsumer/of/slowservices {
	relay.timeout=600000
	http.timeout=600000
}
</@xmlcode></para>
<para>
If there is a method with a matching @namespace section and a mathcing @method section,
the @method section will take precedence.
At this moment if there are multiple sections for the same method or for the same namespace, 
the latest version will overwrite the old one. 
This might be fixed in the future, so do not rely on this behaviour.
It is possible to nest sections, but this will have no real effect, and might be disallowed in the future.
Additional selection criteria might be added in the future.  
</para>

<para>
Note: This override mechanism works for almost any configuration setting,
descibed in this chapter, including user defined variables.
There are some exceptions, mostly settings that have a more global function.
Examples are the <varname>relay.modules</varname> setting (not yet documented), 
and the various caching settings, which have a more global character, and need 
some more research.
</para>

</sect1>

<sect1><title>Configuration of AS/400 parameters</title>
<sect2><title>Working with mulitple machines</title>
<para>
Het is mogelijk om meerdere AS/400 systemen met één As400Connector te benaderen. De voornaamste reden om dit te doen is het gebruik maken van een ander systeem voor uitwijk (b.v. buiten kantoortijden). In principe kunnen er zo veel systemen geconfigureerd worden als men wil. Voor ieder systeem dient men een aparte connectionpool te definiëren. Als men geen password definieert, wordt het password uit het configuratiescherm gebruikt. Aangenomen wordt dat alle systemen hetzelfde password gebruiken. Zo niet, dan moet men een of meer passwords opnemen in de configuratiefile. 
Hieronder staat een voorbeeld
</para>
<@xmlcode>
# door komma's gescheiden lijst aan namen, spaties worden genegeerd
as400.pools = main, fallback

as400.pool.main.system = 192.168.1.1
as400.pool.main.user = USER1
#as400.pool.main.password = secret
as400.pool.main.maxSize = 10
as400.pool.main.maxConnectionLifetime = 1800

as400.pool.fallback.system = 192.168.1.2
as400.pool.fallback.user = USER1
#as400.pool.fallback.password = secret
as400.pool.fallback.maxSize = 10
as400.pool.fallback.maxConnectionLifetime = 1800	
</@xmlcode>
</sect2>

<sect2><title>Overzicht van settings</title>
<para>
Hieronder staan alle settings opgesomd met een korte uitleg. Indien er een default waarde is, wordt deze gemeld.
</para>
<@xmlcode>
# door komma's gescheiden lijst aan namen, spaties worden genegeerd
as400.pools = main

# De naam of het IP nummer van de AS/400 machine
as400.pool.<poolnaam>.system

# De username waarmee wordt ingelogd
as400.pool.<poolnaam>.user

# Het password waarmee wordt ingelogd
as400.pool.<poolnaam>.password

# Het maximaal aantal connecties in de pool
as400.pool.<poolnaam>.maxSize = 10

# De tijd in millisecs, waarna een connectie niet meer gebruikt wordt.
# Default een half uur. N.B. De connectie wordt niet automatisch opgeruimd
# Zie daarvoor de volgende setting
as400.pool.<poolnaam>.maxConnectionLifetimeMillis = 1800000

# De tijd in millisec, waarna een ongebruikte connectie afgesloten wordt.
# default 5 minuten. Zie documentatie apache commons ObjectPool
as400.pool.<poolnaam>.minEvictableIdleTimeMillis= 300000

# De tijd in millisecs, hoevaak gecheckt wordt op ongebruikte connecties.
# default 1 minuut. Zie documentatie apache commons ObjectPool
as400.pool.<poolnaam>.timeBetweenEvictionRunsMillis= 60000


# Als dit de waarde true heeft, worden geen calls naar de AS/400 gedaan
as400.pool.<poolnaam>.simulationFlag = false

# Een timeout mechanisme wat niet goed lijkt te werken. 0 is geen timeout
as400.pool.<poolnaam>.socketTimeout = 0


# De te gebruiken CCSID
as400.ccsid =  1140

# De timeout in milliseconden waarna een call wordt afgebroken
as400.defaultTimeout = 20000
</@xmlcode>
</sect2>
</sect1>

</chapter>
