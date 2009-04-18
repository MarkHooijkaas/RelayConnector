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
