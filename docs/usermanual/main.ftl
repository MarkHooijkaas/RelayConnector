<?xml version='1.0'?>
<!DOCTYPE book PUBLIC "-//OASIS//DTD DocBook XML V4.4//EN"
"/usr/share/xml/docbook/schema/dtd/4.4/docbookx.dtd">

<#assign dollar="$">

<#macro xmlcode>
<programlisting>
<#nested>
</programlisting>
</#macro>

<!-- <book xmlns="http://docbook.org/ns/docbook">-->
<book>
<title>UserManual for ${projectname}</title>

<subtitle>buildnumber ${buildnumber}</subtitle>


<#include "all.ftl">

</book>
