<?xml version='1.0'?>
<!DOCTYPE book PUBLIC "-//OASIS//DTD DocBook XML V4.4//EN"
"/usr/share/xml/docbook/schema/dtd/4.4/docbookx.dtd">

<#assign dollar="$">

<#macro xmlcode>
<#assign txt><@xml_escape><#nested></@xml_escape></#assign>
<programlisting>
${txt?replace("&lt;emphasis&gt;","<emphasis>")?replace("&lt;/emphasis&gt;","</emphasis>")?replace("&lt;replaceable&gt;","<replaceable>")?replace("&lt;/replaceable&gt;","</replaceable>")}
</programlisting>
</#macro>

<!-- <book xmlns="http://docbook.org/ns/docbook">-->
<book>
<title>UserManual for ${projectname}</title>

<subtitle>buildnumber ${buildnumber}</subtitle>


<#include "all.ftl">

</book>
