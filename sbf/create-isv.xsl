<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
>

<xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes" omit-xml-declaration="yes" xalan:indent-amount="4"/>

<xsl:param name="ORG"></xsl:param>
<xsl:param name="PROJECT"></xsl:param>
<xsl:param name="VERSION"></xsl:param>
<xsl:param name="CLASSFILE"></xsl:param>
<xsl:param name="BUILDNUMBER"></xsl:param>


<xsl:template match="/">

<ISVPackage>
  <description>
    <owner><xsl:value-of select="$ORG"/></owner>
    <name><xsl:value-of select="$PROJECT"/></name>
    <version><xsl:value-of select="$VERSION"/> Build <xsl:value-of select="$VERSION"/>.<xsl:value-of select="$BUILDNUMBER"/></version>
    <cn><xsl:value-of select="$ORG"/><xsl:text> </xsl:text><xsl:value-of select="$PROJECT"/><xsl:text> </xsl:text><xsl:value-of select="$VERSION"/></cn>
    <wcpversion>4.2</wcpversion>
    <eula source="" />
    <sidebar source="" />
  </description>
  <content>

    <filesystem loader="com.eibus.contentmanagement.ISVFileSystemManager" description="File System Objects">
      <xsl:for-each select="project/jarfiles/pathelement">
        <file destination="/{$ORG}/{$PROJECT}-{$VERSION}" dir="cordys_install_dir">
      	   <xsl:attribute name="source">
  		<!--	  <xsl:value-of select="@location" />-->
	        <xsl:call-template name="basename">
    	       <xsl:with-param name="path" select="@location"/>
	        </xsl:call-template>
  			  
			</xsl:attribute> 
		</file>	
      </xsl:for-each>
      <xsl:apply-templates select="isv-input/jarfiles/file"/>
      <file destination="/{$ORG}/{$PROJECT}-{$VERSION}" dir="cordys_install_dir" source="{$PROJECT}-{$VERSION}.jar"/>
      <file source="web" destination="/Web/{$ORG}/{$PROJECT}-{$VERSION}" dir="cordys_install_dir" />
    </filesystem>

    <menus loader="com.eibus.contentmanagement.ISVSOAPManager" description="Menus" />
    <toolbars loader="com.eibus.contentmanagement.ISVSOAPManager" description="Toolbars" />

    <busorganizationalroles loader="com.eibus.contentmanagement.ISVRoleManager" description="Roles">
      <busorganizationalrole>
        <entry>
          <cn>
            <string>everyoneIn<xsl:value-of select="$ORG"/><xsl:text> </xsl:text><xsl:value-of select="$PROJECT"/><xsl:text> </xsl:text><xsl:value-of select="$VERSION"/></string>
          </cn>
          <description>
            <string>Everyone In <xsl:value-of select="$ORG"/><xsl:text> </xsl:text><xsl:value-of select="$PROJECT"/><xsl:text> </xsl:text><xsl:value-of select="$VERSION"/></string>
          </description>
          <objectclass>
            <string>top</string>
            <string>busorganizationalrole</string>
            <string>busorganizationalobject</string>
          </objectclass>
        </entry>
      </busorganizationalrole>
    </busorganizationalroles>

    <applicationconnector loader="com.eibus.contentmanagement.ISVSOAPManager" description="Application Connectors">
      <SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
        <SOAP:Body>
          <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
            <tuple version="isv" unconditional="true" key="/Cordys/WCP/Application Connector/{$CLASSFILE}">
              <new>
                <applicationconnector>
                  <step>
                    <url>/cordys/<xsl:value-of select="$ORG"/>/<xsl:value-of select="$PROJECT"/>-<xsl:value-of select="$VERSION"/>/config.html</url>
                    <description><xsl:value-of select="$PROJECT"/></description>
                    <caption>Provide details for <xsl:value-of select="$PROJECT"/> Service</caption>
                    <isv><xsl:value-of select="$ORG"/></isv>
                    <product><xsl:value-of select="$PROJECT"/></product>
                    <image>/cordys/wcp/images/admin/configurationconnector.png</image>
                    <implementation><xsl:value-of select="$CLASSFILE"/>.<xsl:value-of select="$PROJECT"/></implementation>
                    <sharejvm>true</sharejvm>
                    <spycategory />
                    <loggercategory />
                    <classpath>
                      <xsl:for-each select="project/jarfiles/pathelement">
                        <location>/<xsl:value-of select="$ORG"/>/<xsl:value-of select="$PROJECT"/>-<xsl:value-of select="$VERSION"/>/<xsl:call-template name="basename"><xsl:with-param name="path" select="@location"/></xsl:call-template></location>
                      </xsl:for-each>
                      <location>/<xsl:value-of select="$ORG"/>/<xsl:value-of select="$PROJECT"/>-<xsl:value-of select="$VERSION"/>/<xsl:value-of select="$PROJECT"/>-<xsl:value-of select="$VERSION"/>.jar</location>
                    </classpath>
                  </step>
                </applicationconnector>
              </new>
            </tuple>
          </UpdateXMLObject>
        </SOAP:Body>
      </SOAP:Envelope>
    </applicationconnector>
    <styles loader="com.eibus.contentmanagement.ISVSOAPManager" description="XMLStore" />
    <xforms loader="com.eibus.contentmanagement.ISVSOAPManager" description="XForms" />
    <xmlstore loader="com.eibus.contentmanagement.ISVSOAPManager" description="XMLStore" />
    <busmethodsets loader="com.eibus.contentmanagement.ISVMethodSetManager" description="Method Sets" />
    <wsappserver-content loader="com.eibus.contentmanagement.ISVSOAPManager" description="Ws-AppServer" />
  </content>
  <promptset/>
</ISVPackage>

</xsl:template>


<xsl:template name="basename">
  <xsl:param name="path"/>
  <xsl:choose>
     <xsl:when test="contains($path, '/')">
        <xsl:call-template name="basename">
           <xsl:with-param name="path" select="substring-after($path, '/')"/>
        </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
        <xsl:value-of select="$path"/>
     </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
