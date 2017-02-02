<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:math="http://www.w3.org/2005/xpath-functions/math"
	xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
	xmlns:gp="http://efl.fr/chaine/saxon-pipe/config"
	xmlns:xsldoc="top:marchand:xml:maven:xslDoc"
	exclude-result-prefixes="#all"
	version="3.0">
	<xd:doc scope="stylesheet">
		<xd:desc>
			<xd:p><xd:b>Created on:</xd:b> Nov 30, 2016</xd:p>
			<xd:p><xd:b>Author:</xd:b> cmarchand</xd:p>
			<xd:p>Generates a gaulois-pipe configuration based on an existing configuration, by defining sources</xd:p>
		</xd:desc>
	</xd:doc>
	
	<xsl:param name="xsldoc:sources" as="xs:string"></xsl:param>
	
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<xsl:template match="/gp:config/gp:sources">
		<xsl:copy>
			<xsl:for-each select="tokenize($xsldoc:sources,':')">
				<xsl:sequence select="xsldoc:getFolder(.)"/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	
	<!--xsl:template match="gp:params/gp:param[@name=('sources','absoluteRootFolder')]"/-->
	<xsl:template match="gp:params"/>
	
	<xsl:template match="comment()" priority="+1"/>
	
	<xsl:function name="xsldoc:getFolder" as="element(gp:folder)">
		<xsl:param name="pEntry" as="xs:string+"/>
		<xsl:variable name="entry" as="xs:string+" select="tokenize($pEntry,'\|')"/>
		<xsl:sequence>
			<folder href="{$entry[1]}"  pattern=".*\.xsl" recurse="{if ($entry[3] eq 'true') then 'true' else 'false'}" xmlns="http://efl.fr/chaine/saxon-pipe/config">
				<param name="absoluteRootFolder" value="file:$[basedir]/{$entry[1]}"/>
			<param name="levelsToKeep" value="{$entry[2]}"/>
			</folder>
		</xsl:sequence>
	</xsl:function>
	
</xsl:stylesheet>