
<chapter id="overview">
<title>Overview</title>

<para>
The basic function of the RelayConnector is to relay method calls to one or more other methods, with some simple transformations. 
</para>

<para>The RelayConnector can be used for a variety of purposes:
<itemizedlist>
<listitem><para>To make a method that calls a number of different methods (a composite service).</para></listitem>
<listitem><para>To wrap an existing method with some simple transformations.</para></listitem>
<listitem><para>To provide a set of methods in the same namespace, which have different underlying technical implementations, and are thus implemented on different application connectors (and thus with different namespaces).</para></listitem>
</itemizedlist>
For all kind of simple testing and prototyping purposes, such as a HelloWorld or a Echo service.
(Planned) To relay method calls to different Cordys organisations 
</para>

<para>
In order to use the RelayConnector, one must define methods by adding a method to a method set, and entering a small XML script in the "implementation" section of this method. The RelayConnector is not meant to be a full scripting language. It is intended that a script should only be 10 or 20 lines long. In fact a macro facility has been removed from the RelayConnector, because this lead to too complex scripts. Instead it is possible to easily define new command using java, which of course is a full blown programming language.
</para>

</chapter>