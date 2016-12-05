<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:math="http://www.w3.org/2005/xpath-functions/math"
	xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
	xmlns:gp="http://efl.fr/chaine/saxon-pipe/config"
	exclude-result-prefixes="xs math xd"
	version="3.0">
	<xd:doc scope="stylesheet">
		<xd:desc>
			<xd:p><xd:b>Created on:</xd:b> Nov 30, 2016</xd:p>
			<xd:p><xd:b>Author:</xd:b> cmarchand</xd:p>
			<xd:p>Generates a gaulois-pipe configuration based on an existing configuration, by defining sources</xd:p>
		</xd:desc>
	</xd:doc>
	
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<xsl:template match="gp:params/gp:param[@name='sources']"/>
	
</xsl:stylesheet>