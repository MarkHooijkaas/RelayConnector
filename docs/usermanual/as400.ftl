<chapter><title>As400RelayConnector specific commands</title>
<para>The As400RelayConnector is a basically the RelayConnector with some
extra AS/400 related commands added. If you do not need AS/400
functionality it is recommended that you use the RelayConnector
instead. This chapter describe the commands that are specific to the
HttpConnector.</para>


<sect1><title>Infrastructure requirements</title>
<para>
The As400RelayConnector should be able to connect to the desired AS/400 system.  The following issues should be taken care of: 
</para>
<itemizedlist>
<listitem><para>
The AS/400 system should have as-rmtcmd service running, and accept connections from a correctly authenticated user and permissions for this user to execute programs and commands.
</para></listitem>
<listitem><para>
The network should allow TCP connections between the Cordys machines and the  AS/400 machine. Any firewall,  ACL or IPSec policies should be tuned to allow traffic on a few specific TCP ports. In general this will be ports 449, 8470, 8475 and 8476. When SSL is used, ports 449, 9470, 9475 and 9476 will be used. This port numbers are based on the following document:  http://www-03.ibm.com/systems/i/software/toolbox/faqports.html
</para></listitem>
</itemizedlist>
</sect1>

<sect1><title>as400cmd command</title>
<@xmlcode>
<as400cmd command="<replaceable>command-expression</replaceable>" />
</@xmlcode>

<para>This command will execute the provided command on the AS/400 host. 
There is currently no way to handle the output of the command, but this might be added in the future.
</para>

</sect1>



<sect1><title>as400prog command</title>
<@xmlcode>
<as400prog
  [input="<replaceable>xml-expression</replaceable>"]
  [output="<replaceable>xml-expression</replaceable>"]
>
  <pcml>
    <program name="..." path="...">
       <data name="..." type="..."  length="..." usage="..."/>
       <struct name="..." usage="...">
           <data name="..." type="..."  length="..." usage="..."/>
       </struct>
        .....
    </program>
  </pcml>
</as400prog>
</@xmlcode>

<para>The as400prog command will map a XML structure to the input parameter of an AS/400 program. 
The resulting output parameters will be mapped back to  a XML structure.
The <command>name</command> attribute and the <command>path</command> attribute are similar to PCML.
This mapping is done by a PCML-like structure, which has two kind of elements:
</para>
<itemizedlist>

<listitem><para><command>data</command>: a single parameter (which might have a repeat count).
</para></listitem>

<listitem><para><command>struct</command>: a grouping of data elements.
</para></listitem>
</itemizedlist>
<para>
According to the PCML spec, it is possible to define (named) structs, that are referred to in other places in the PCML. This is not supported, and such structs should be copied to the elements where they are referred from. 
In theory it might be possible to nest structs multiple levels, but this is currently not supported, since there aren't any AS/400 programs that use such a convention, as far as is known at this time. 
</para>

 
<sect2> <title><command>data</command> element </title>
<para>
A <command>data</command> element doesn't have any subelements but has a large number of attributes:
</para>
<itemizedlist>

<listitem><para><command>name</command>: The name of the parameter. 
</para></listitem>

<listitem><para><command>prefix</command>: A XML namespace prefix that will be used for an output parameter.
</para></listitem>

<listitem><para><command>namespace</command>: The namespace that will be used for an output parameter.
</para></listitem>

<listitem><para><command>xmlInput</command>: For input parameters this will be the tagname (or path) to find data in the  XML input message. If the "xmlInput" attribute is ommitted, the "name" attribute will be used instead. 
</para></listitem>

<listitem><para><command>xmlOutput</command>: For output parameters this will be the tagname (or path) to place data in the XML output message. If the "xmlOutput" attribute is ommitted, the "name" attribute will be used instead. 
</para></listitem>

<listitem><para><command>type</command>: Het type van de parameter, zie 2.4.1.1
</para></listitem>

<listitem><para><command>length</command>: de lengte van de parameter, voor de numerieke types int en float zijn slechts bepaalde lengtes toegestaan, zie 2.4.1.1
</para></listitem>

<listitem><para><command>precision</command>: de preciesie van de parameter, voor de numerieke types int en float zijn slechts bepaalde lengtes toegestaan, zie 2.4.1.1
</para></listitem>

<listitem><para><command>init</command>: een default waarde, zie 2.4.1.2
</para></listitem>

<listitem><para><command>emptyValue</command>: een synoniem voor het attribuut init. Er mag slechts één van beide attributen gedefinieerd zijn.
</para></listitem>

<listitem><para><command>usage</command>: geeft aan wat voor soort parameter het is, mag de waardes "input", "output" of "inputoutput" hebben.
</para></listitem>

<listitem><para><command>count</command>: geeft aan hoe vaak deze parameter herhaald wordt. De PCML standaard staat toe hier een dynamische waarde te gebruiken maar in de As400Connector worden alleen vaste numerieke waarden ondersteund. Er wordt een fout gegenereerd als dit type gebruikt wordt met een waarde anders dan een geheel getal. Binnen Plex en 2E schijnt het niet mogelijk te zijn een variabel aantal elementen te gebruiken, dus is dit afdoende.
</para></listitem>

<listitem><para><command>optional</command>: mag waardes "true"of "false" hebben, als dit attribuut niet aanwezig is, geldt de waarde "false" Zie 2.4.1.1.
</para></listitem>

<listitem><para><command>trim</command>: kan waardes "true" of "false" hebben. Als dit attribuut niet aanwezig is, geldt de waarde true. Bij een waarde true wordt XML input ontdaan van leading en trailing whitespace.
</para></listitem>

<listitem><para><command>convert</command>: bevat de classname van een convertor die string waardes converteert. Zie Hoofdstuk 3 voor verdere uitleg.
</para></listitem>
</itemizedlist>

<para>
The attributes namespace and prefix are optional, and follow the following conventions:
</para>
<itemizedlist>
<listitem><para>
Als een namespace wordt opgegeven zonder prefix, wordt de default namespace op het betreffende element gezet.
</para></listitem>

<listitem><para>
Als een namespace wordt opgegeven en een prefix, wordt de prefix op het betreffende element gedefinieerd (naar die namespace) en krijgt het element deze prefix.
</para></listitem>

<listitem><para>
Als alleen een prefix is gegeven zonder namespace, dan krijgt de elementnaam de betreffende prefix. De bouwer dient er voor te zorgen dat op een hoger liggende element de prefix al gedefinieerd is.
datatypes en bij behorende lengtes
</para></listitem>
</itemizedlist>

<sect3><title>datatypes and lengths</title>
<para>The following types can be used. 
The length and precision requirements are conform the PCML specification.
</para>
<table  frame='all'>
    <title>data element types and lengths</title>
   <tgroup cols="4" align="left" colsep="1" rowsep="1">
    <colspec colname="type" colwidth="2*"/>
    <colspec colname="length" colwidth="2*"/>
    <colspec colname="precision" colwidth="3*"/>
    <colspec colname="comment" colwidth="8*"/>
    <thead>
 <row>
      <entry>type</entry>
      <entry>length</entry>
      <entry>precision</entry>
      <entry>comment</entry>
    </row>
 </thead>
 <tbody>
    <row>
      <entry>char</entry>
      <entry>mandatory</entry>
      <entry>n/a</entry>
      <entry>a text field</entry>
    </row>
    <row>
      <entry>xml</entry>
      <entry>mandatory</entry>
      <entry>n/a</entry>
      <entry>This is like a char field but is supposed to contain XML data, which is handled as thus, instead of escaping the special XML characters. This type is not a normal PCML type.</entry>
    </row>
    <row>
      <entry>int</entry>
      <entry>2</entry>
      <entry>15 (default) or 16</entry>
      <entry>corresponds with a signed and unsigned short (16 bits integer)</entry>
    </row>
    <row>
      <entry>int</entry>
      <entry>4</entry>
      <entry>31 (default) or 32</entry>
      <entry>corresponds with a signed and unsigned int (32 bits integer)</entry>
    </row>
    <row>
      <entry>int</entry>
      <entry>8</entry>
      <entry>63 (default)</entry>
      <entry>corresponds with a signed long (64 bits integer). Unsigned long is not supported</entry>
    </row>
    <row>
      <entry>float</entry>
      <entry>4</entry>
      <entry>N/A</entry>
      <entry></entry>
    </row>
    <row>
      <entry>float</entry>
      <entry>8</entry>
      <entry>N/A</entry>
      <entry></entry>
    </row>
    <row>
      <entry>packed</entry>
      <entry>mandatory</entry>
      <entry>mandatory</entry>
      <entry>A packed decimal</entry>
    </row>
    <row>
      <entry>zoned</entry>
      <entry>mandatory</entry>
      <entry>mandatory</entry>
      <entry>a zoned decimal</entry>
    </row>
    <row>
      <entry>struct</entry>
      <entry></entry>
      <entry></entry>
      <entry>This is not supported (yet). In PCML this would refer to a struct definition elsewhere in the PCML.</entry>
    </row>
    <row>
      <entry>byte</entry>
      <entry></entry>
      <entry></entry>
      <entry>Not supported</entry>
    </row>
 </tbody>
    </tgroup>
 </table>
</sect3>

<sect3><title>omgaan met default values en lege waardes</title>
<para>
Als het attribuut init gevuld is, geeft dit de "initial value" oftewel de default waarde als het element weggelaten wordt. Dit is conform de PCML standaard. In de As400Connector zijn echter een aantal de attributen emptyValue en optional toegevoegd. Het attribuut emptyValue is slechts een synoniem voor init, en vooral bedoeld voor output velden, waar de naam init verwarrend kan zijn. Het attribuut optional geeft aan of het hele element in de XML weggelaten mag worden. 
</para>
<para>
De precieze regels voor het gebruik van optional en emptyValue zijn enigszins complex, omdat AS/400 programma's geen optionele parameters of NULL waardes kennen. Vooral in combinatie met output parameters, structs en arrays wordt dit belangrijk. De regels zijn ruwweg als volgt:
</para>
<itemizedlist>
<listitem><para>
Als een input parameter niet aanwezig is in de XML, en optional is false, levert dit een fout op.
</para></listitem>
<listitem><para>
Als een input parameter niet aanwezig is in de XML, en optional is true:
bij een data element wordt de emptyValue/init waarde gebruikt
bij een struct element wordt voor ieder sub element de emptyValue/init waarde gebruikt. Als niet alle subelementen een default waarde hebben, geldt dit als fout.
</para></listitem>
<listitem><para>
Als een output data parameter gelijk is aan de emptyValue, en optional="false" dan wordt in de output XML gewoon het element toegevoegd.
</para></listitem>
<listitem><para>
Als een output data parameter gelijk is aan de emptyValue, en optional="true" dan wordt in de output XML geen element toegevoegd.
</para></listitem>
<listitem><para>
Als een output struct parameter optional="true" heeft, en alle subelementen (en subsubelementen etc) zijn gelijk aan hun emptyValue, dan wordt de hele struct weggelaten.
</para></listitem>
</itemizedlist>

<para>
Met name de laatste bullet heeft een interessant detail. De subelementen hoeven zelf niet optional te zijn, voor de hele struct om weggelaten te worden. Als b.v. alle velden niet optional zijn, dan zijn altijd alle velden in de struct aanwezig. Als alle elementen echter de emptyValue bevatten wordt de hele struct weggelaten. Als echter b.v. slechts 9 van de 10 velden "empty" zijn, dan is de hele struct met alle 10 velden aanwezig.
</para>

<para>
Tot slot nog wat over het verschil tussen het ontbreken van een veld en het leeg zijn van een veld in XML. Als het XML element wel aanwezig is maar leeg, wordt dit beschouwd als een lege string.
</para>

<@xmlcode>
<data name="city" usage="input" type="char"/>	
<data name="country" usage="input" type="char" length="50" init="Netherlands" />	
</@xmlcode>

<@xmlcode>
<input>
  <city>Assen</city> <!-- results in the string "Assen" -->
  <country>Germany</country> <!-- results in the string "Germany" -->
</input>

<input>
  <city> </city>     <!-- results in the string " " -->
  <country> </country>         <!-- results in the string " " -->
</input>

<input>
  <city></city>      <!-- results in the string "" -->
  <country></country>          <!-- results in the string "" -->
</input>

<input>
  <city/>              <!-- results in the string "" -->
  <country/>                <!-- results in the string "" -->
</input>

<input>
  <city null="true"/>  <!-- results in the null string  -->
  <country null="true"/>    <!-- results in the null string  -->
</input>

<input>
                         <!-- results in an error -->
  <country></country>          <!-- results in the string "" -->
</input>

<input>
  <city></city>      <!-- results in the string "" -->
                         <!--results in the string "Netherlands" for country -->
</input>
</@xmlcode>

<para>
Alleen als de tag "land" geheel ontbreekt wordt de init waarde gebruikt, mits deze gedefinieerd is. Als er geen init waarde gedefinieerd is, wordt er een fout gegenereerd, aan
 </para>
</sect3>


</sect2>


<sect2> <title>struct element</title>
<para>
Een struct element kan gebruikt worden om data elementen te groeperen. Een struct kan naast data elementen ook weer geneste struct's bevatten. De name van de struct wordt weer gebruikt voor de input XML en de output XML.
</para>
</sect2>


<sect2><title> Mapping of the XML</title>
<sect3><title> Eenvoudige mapping</title> 
<para>
De meest eenvoudige manier hoe PCML parameters op het input en output bericht worden afgebeeld is door eenvoudig gebruik van het name attribuut uit PCML. 
</para>

<@xmlcode>
    <program name="...">
      <struct name="header" usage="input"/>
        <data name="userid" type="char" length="8" />
      </struct>
      <struct name="person" usage="input"/>
        <data name="firstName" type="char" length="20" />
        <data name="lastName" type="char" length="20" />
      </struct>
      <struct name="address" usage="output"/>
        <data name="street" type="char" length="20" />
        <data name="city" type="char" length="20" />
      </struct>
      <struct name="job" usage="output"/>
        <data name="company" type="char" length="20" />
        <data name="title" type="char" length="20" />
      </struct>
    </program>
</@xmlcode>

<para>Hierbij horen de volgende eenvoudige input en output berichten</para>

<@xmlcode>
    <getPerson>
      <header>
        <userid>user1</userid>
      </header>
      <person>
        <firstName>Marilyn</firstName>
        <lastName>Monroe</lastName>
      </person>
    </getPerson>
</@xmlcode>

<para>and</para>

<@xmlcode>
    <getPersonResponse>
      <address>
         <street>Sunset Blvd.</street>
         <city>Hollywood</city>
      </address>
      <job>
         <company>SomeStudio</company>
         <title>actress</title>
      </job>
    </getPersonResponse>
</@xmlcode>
</sect3>

<sect3><title> Output bericht anders structureren</title>
<para>
Ook de structuur van output parameters is niet altijd één-op-één te vertalen naar XML. Hiervoor zijn een aantal technieken beschikbaar:
</para>

<sect4><title>velden weglaten</title>
<para>
Als een output parameter niet gewenst is in de XML, kan men het eenvoudig niet terug laten komen door geen of een leeg xmlOutput veld te hebben. Omdat de default waarde voor het xmlOutput attribuut het name attribuut is, moet men ook het name attribuut weglaten als men het xmlOutput attribuut weglaat. Meestal is het beter leesbaar als het name attribuut wordt gevuld met de betekenis van het veld, en dat het xmlOutput veld expliciet leeg gemaakt wordt.
</para>
<@xmlcode>
      <data type="char" length="5"  name="ignoreCountry" xmlOutput=""/>
</@xmlcode>

</sect4>
<sect4><title>structs weglaten</title>
<para>
Voor structs zijn er twee mogelijkheden tot het weglaten. Ten eerste kan het zijn dat men de volledige struct en al zijn onderliggende elementen niet wil terugvinden in de XML. Dit kan op precies dezelfde manier als het weglaten van datavelden in de vorige paragraaf. Wel is het belangrijk dat men nog steeds alle onderliggende elementen wel opneemt, aangezien van belang is voor de communicatie met de AS/400. Het maakt daarbij dan niet meer uit wat het name of xmlOutput veld van die onderliggende velden is, aangezien hier geen output voor gegenereerd wordt.
</para>
<@xmlcode>
      <struct name="techdata" usage="output" xmlOutput=""/>
</@xmlcode>

<para>
Een heel ander geval is dat de onderliggende data wel terug moet komen in de XML, maar niet in een aparte subelement. Soms zijn er bijvoorbeeld technische redenen om de parameters te verdelen over meerdere structs, maar zou men dit functioneel niet terug willen zien in het XML bericht. Dit kan men relatief eenvoudig bereiken door voor xmlOutput de waarde "." (punt) te geven, voor de huidige locatie.
</para>
<para>
In het onderstaande voorbeeld 
</para>
<@xmlcode>
      <struct name="groep1" usage="output" xmlOutput="."/>
        <data name="item1" type="char" length="20" />
        <data name="item2" type="char" length="20" />
      </struct>
      <struct name="groep2" usage="output" xmlOutput="."/>
        <data name="item3" type="char" length="20" />
        <data name="item4" type="char" length="20" />
      </struct>
</@xmlcode>
<para>Worden alle items tesamen gegroepeerd als volgt</para>
<@xmlcode>
        <item1>....</item1>
        <item2>....</item2>
        <item3>....</item3>
        <item4>....</item4>
</@xmlcode>

<para>
N.B. Er is geen eis of reden dat xmlOutput namen unieke namen dienen te zijn. In bovenstaand voorbeeld hadden alle elementen ook "item" kunnen heten, en dus een repeterend element kunnen vormen. 
</para>

</sect4>
<sect4><title>XML lagen toevoegen</title>
<para>
Het is niet toegestaan om zo maar structs toe te voegen, aangezien de structuur van struct en data elementen exact moet corresponderen met de input en output parameters van het aan te roepen programma. 
</para>
<para>
Het is echter wel eenvoudig mogelijk om een extra laag in de XML output op te nemen door deze als pad op te geven. Een eenvoudig voorbeeld Ziet er al volg uit:
</para>
<@xmlcode>
      <struct name="job" usage="output"/>
        <data name="company"      type="char" length="20" />
        <data name="title/titleCode" type="char" length="20" />
      </struct>
</@xmlcode>

<para>Dit zal output genereren in de volgende vorm</para>
<@xmlcode>
      <job>
        <company>hitc</company>
        <title>
          <titleCode>PROF</titleCode>
        </title>
      </job>
</@xmlcode>

<para>
Het is wel heel belangrijk om te beseffen dat voor iedere naam in een dergelijk pad een nieuw element in de XML wordt aangemaakt, ook als er al in de output al een element bestaat met de betreffende naam. Dit kan tot verrassende resultaten leiden, zoals in onderstaand voorbeeld.
</para>
<@xmlcode>
        <data name="vak/code" type="char" length="20" />
        <data name="vak/naam" type="char" length="20" />
</@xmlcode>

<para>Dit zal leiden tot onderstaande output</para>
<@xmlcode>
        <vak>
          <code>NL</code>
        </vak>
        <vak>
          <naam>Nederlands</naam>
        </vak>
</@xmlcode>

<para>
Om beide elementen samen te voegen onder hetzelfde vak element is een andere syntax nodig. Als een naam in de xmlOutput eindigt op $, dan wordt gezocht naar een bestaand element met die naam. Als er reeds meerdere elementen zijn met die naam, dan wordt het laatste element gekozen. Er wordt voor de laatste gekozen, omdat dit meestal wenselijk is als men meerdere elementen toevoegt met dezelfde naam. De syntax met een $ op het eind is geïnspireerd door de anchor constructie in regular expressions, wat ruwweg een end-of-line betekent.
</para>
<para>Bovenstaand voorbeeld zou dan als volgt worden</para>
<@xmlcode>
        <data name="vak/code"  type="char" length="20" />
        <data name="vak$/naam" type="char" length="20" />
</@xmlcode>

<para>Dit zal leiden tot onderstaande output</para>
<@xmlcode>
        <vak>
          <code>NL</code>
          <naam>Nederlands</naam>
        </vak>
</@xmlcode>
<para>
In een wat uitgebreider voorbeeld willen we meerdere groepen toevoegen. Dit voorbeeld is gebaseerd op een geval waarbij een programma technisch geen Array kon opleveren, en in plaats daarvan de array elementen meerdere keren herhaalde. In onderstaand voorbeeld wordt op de vetgedrukte regels steeds een nieuw vak element aangemaakt, en gebruiken de andere data elementen dit net aangemaakte vak element (omdat het net aangemaakt is, is het het laatste element).
</para>
<@xmlcode>
        <data name="vak/code"    type="char" length="20" />
        <data name="vak$/naam"   type="char" length="20" />
        <data name="vak$/omschr" type="char" length="80" />
        <data name="vak/code"    type="char" length="20" />
        <data name="vak$/naam"   type="char" length="20" />
        <data name="vak$/omschr" type="char" length="80" />
        <data name="vak/code"    type="char" length="20" />
        <data name="vak$/naam"   type="char" length="20" />
        <data name="vak$/omschr" type="char" length="80" />
</@xmlcode>

<para>levert als output</para>
<@xmlcode>
        <vak>
          <code>NL</code>
          <naam>Nederlands</naam>
          <omschr>....</omschr>
        </vak>
        <vak>
          <code>EN</code>
          <naam>Engels</naam>
          <omschr>....</omschr>
        </vak>
        <vak>
          <code>WI</code>
          <naam>Wiskunde</naam>
          <omschr>....</omschr>
        </vak>
</@xmlcode>

<para>
Er is ook een syntax om het eerste element met een bepaalde naam te selecteren, te weten een naam voorafgegaan door het "^" symbool. Het is echter lastig om een realistische case te bedenken waarbij men bij meerdere elementen met dezelfde naam per se het eerste element zou willen hebben. De syntax met een "^" symbool is gebaseerd op de begin-of-line bij regular expressions. 
</para>

</sect4>
<sect4><title>complexe output paden</title>
<para>
In principe kan men veel complexere paden aanmaken, door het combineren van elementnamen en .., al dan niet in combinatie met $, ^, etc. Wel dient men hierbij goed op te letten of een nieuw element moet worden aangemaakt, of dat een bestaand element gekozen moet worden.
</para>

</sect4>
</sect3>

<sect3><title>Gebruik van namespaces</title>
<para>
Bij het opzoeken van elementen in het input XML bericht worden namespaces genegeerd. 
Bij het genereren van de output XML wordt standaard geen prefix gebruikt en ook geen default namespace gezet. Hierdoor erven in principe alle elementen de default namespace van hun parent. Per element zijn er 2 attributen die dit gedrag kunnen beïnvloeden:
Het attribuut namespace bepaalt welke namespace gebruikt wordt.
Het attribuut prefix bepaalt welke prefix gebruikt wordt.

Hierbij gelden de volgende regels:
Als een namespace gedefinieerd is en er is geen prefix gedefinieerd, dan wordt de default namespace op het betreffende element gezet. Deze geldt dan dus ook automatisch voor alle subelementen.
Als een namespace gedefinieerd is en er is ook een prefix gedefinieerd, dan wordt er een prefix definitie toegevoegd, en krijgt de elementnaam die prefix. Het kan zijn dat de prefix definitie niet nodig is, omdat deze ook op een hoger element al staat. Toch zal in deze gevallen de prefix nogmaals gedefinieerd worden.
Als er alleen een prefix is opgegeven zonder namespace, dan krijgt de elementnaam deze prefix, maar wordt deze prefix niet gedefinieerd. De ontwikkelaar dient er dan voor te zorgen dat de prefix op een hoger niveau reeds gedefinieerd is.
</para>

</sect3>

<sect3><title>Conversie van formaten</title>
<para>
Soms is de tekst representatie in XML anders dan aan AS/400 kant wordt verwacht. De As400Connector biedt een zeer basaal maar eenvoudig uitbreidbaar conversie mechanisme. Dit mechanisme kan gebruikt worden voor alle data elementen, behalve voor type="xml". 
</para>
<para>
Het mechanisme werkt heel simpel. 
</para>
<para>
Men voegt aan een data element een extra attribuut toe genaamd convert, die aangeeft welke conversie men wens uit te voeren.
Bij een input element wordt de tekst uit de XML door de convertor gehaald en daarna vertaald naar AS/400 formaat (b.v. packed decimal).
Bij een output element wordt de output gegevens eerst vertaald van AS/400 formaat (b.v. packed decimal) naar een String en daarna door de convertor gehaald.
Een convertor vertaalt een string in een andere string.
</para>
<para>
Op dit moment zijn er 4 standaard convertors beschikbaar. Dit kan in de toekomst eenvoudig uitgebreid worden. Ook kan men eenvoudig eigen convertors schrijven. Het is mogelijk om parameters mee te geven aan Convertors. De volgende convertors zijn momenteel beschikbaar:
XsdDateToYyyymmdd, vertaalt een datum in xsd:date formaat (2009-12-31) naar het formaat 20091231
YyyymmddToXsdDate, vertaalt een datum in YYYYMMDD formaat (20091231) naar het xsd:date formaat 2009-12-31
TextToXsdBoolean, vertaalt een tekst (b.v. J of N), naar xsd:boolean (true, false)
XsdBooleanToText, vertaalt een xsd:boolean (0, 1, true, false) naar een tekst (b.v. J of N)
</para>
<para>
In het convert attribuut kan men de naam van de betreffende convertor plaatsen. De As400Connector probeert dan een class te laden met de betreffende naam. Als er geen package opgegeven is, a default package will be used. Op deze manier kanmen eenvoudig andere convertors gebruiken.
</para>
<para>
Hieronder staan enige voorbeelden van het gebruik van convertors:
</para>
<@xmlcode>
<struct name="input" usage="input">
  <data name="datum" type="int4" convert="XsdDateToYyyymmdd"/>
  <data name="getrouwd" type="char" length="1"
     convert="XsdBooleanToText" trueValue="J" falseValue="N"/>
</struct>

<struct name="output" usage="output">
  <data name="datum" type="int4" convert="YyyymmddToXsdDate"/>
  <data name="getrouwd" type="char" length="1"
     convert="TextToXsdBoolean" trueValue="J" falseValue="N"/>
</struct>
</@xmlcode>



</sect3>


</sect2>


</sect1>



</chapter>
