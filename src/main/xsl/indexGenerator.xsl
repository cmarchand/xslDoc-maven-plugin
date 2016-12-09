<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    exclude-result-prefixes="xs math xd map xsldoc"
    xmlns:xsldoc="top:marchand:xml:xsl:doc"
    version="3.0">
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Created on:</xd:b> Dec 6, 2016</xd:p>
            <xd:p><xd:b>Author:</xd:b> cmarchand</xd:p>
            <xd:p></xd:p>
        </xd:desc>
    </xd:doc>
    
    <xsl:param name="xsldoc:programName" as="xs:string" select="''"/>
    <!--xsl:param name="xsldoc:absoluteRootDir" as="xs:string"/-->
    
    <xsl:output method="html" indent="yes" encoding="UTF-8"/>
    
    <xsl:template  match="entries">
        <html>
            <head>
                <title xsl:expand-text="yes">{if(string-length($xsldoc:programName)) then concat($xsldoc:programName,' ') else ''}XSL documentation</title>
                <style type="text/css" xsl:expand-text="no">
                    a:link a:hover a:active a:visited { text-decoration: none; }
                    img { margin-right: 3px; padding-right: 3px; } 
                </style>
            </head>
            <body>
                <h3 xsl:expand-text="yes">{if(string-length($xsldoc:programName)) then concat($xsldoc:programName,' ') else ''}XSL documentation</h3>
                <ul>
                    <xsl:for-each-group group-by="xsldoc:getPathOf(@label)" select="entry">
                        <xsl:choose>
                            <xsl:when test="string-length(current-grouping-key()) gt 0">
                                <li><img alt="Folder" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAGrSURBVDjLxZO7ihRBFIa/6u0ZW7GHBUV0UQQTZzd3QdhMQxOfwMRXEANBMNQX0MzAzFAwEzHwARbNFDdwEd31Mj3X7a6uOr9BtzNjYjKBJ6nicP7v3KqcJFaxhBVtZUAK8OHlld2st7Xl3DJPVONP+zEUV4HqL5UDYHr5xvuQAjgl/Qs7TzvOOVAjxjlC+ePSwe6DfbVegLVuT4r14eTr6zvA8xSAoBLzx6pvj4l+DZIezuVkG9fY2H7YRQIMZIBwycmzH1/s3F8AapfIPNF3kQk7+kw9PWBy+IZOdg5Ug3mkAATy/t0usovzGeCUWTjCz0B+Sj0ekfdvkZ3abBv+U4GaCtJ1iEm6ANQJ6fEzrG/engcKw/wXQvEKxSEKQxRGKE7Izt+DSiwBJMUSm71rguMYhQKrBygOIRStf4TiFFRBvbRGKiQLWP29yRSHKBTtfdBmHs0BUpgvtgF4yRFR+NUKi0XZcYjCeCG2smkzLAHkbRBmP0/Uk26O5YnUActBp1GsAI+S5nRJJJal5K1aAMrq0d6Tm9uI6zjyf75dAe6tx/SsWeD//o2/Ab6IH3/h25pOAAAAAElFTkSuQmCC"/><xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()"/>
                                    <ul>
                                        <xsl:for-each select="current-group()">
                                            <xsl:variable name="relativePath" as="xs:string" select="@label"/>
                                            <xsl:variable name="target" as="xs:string" select="concat('xsldoc/',@value)"/>
                                            <li><a href="{$target}"><img alt="XSL" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AwHCAggfxOf4gAAAeNJREFUOMulk71rU1EYxn8n9+K9EpQMJZQMTQatONhJSnDKoND+AxmyWgcHC03xYxI6BcEWBwlK00VLSAJZ2qXgYLa2QkoXLWq0zSq45KM3Mbnndem9Jm0qgs90znt4fud9eM9RIiLVapVarcZ5EhG01qRSKUQEpZR/pkREisUiszMziIhvEBG63S6dToder0d1b8+HDCowuPHISimUUliWhW3buK6L1ppkMkk+nz8HMGA+DTEMA4BSqQQwBDF9PyBKwUnGwazBYJBrk5M0m01s2+b74eFZwN8glmURiURot9v0+/3REZRSrH66wWJ26kwc0zR5+uYWGz9vEwqF/Ej+FABWdqIcVBQvHnwE4PHrm3R/OVgXLpK59wFESGenuJ4Q0vH60IxleXtC5jJRcRxHHMeR+XVD1nanpe6+lbXdaZlfN6Tdakmr1ZK5TFSWtyfEU8C7Obv4FcMwMAyDxrGLM7bP5tFdnLF9GscupmlimiYvFz5zUFGs7ET/RPAgrx5+A+D9l2c0Lr0jrBL8kAqXm3dIXH0EwP3nV4ZjeK14MbTWorWWpa1xWdhAlrbG/drp9uXkyUqhUBiCjNKguVwu+/Whd5CO1yE++kOtPjkaWQ/wnzIBwuEwuVzun02xWMxf/wbanEBKrN+vTQAAAABJRU5ErkJggg=="/><xsl:text> </xsl:text><xsl:value-of select="substring($relativePath, string-length(current-grouping-key())+2)"/></a></li>
                                        </xsl:for-each>
                                    </ul>
                                </li>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="current-group()">
                                    <xsl:variable name="relativePath" as="xs:string" select="@label"/>
                                    <xsl:variable name="target" as="xs:string" select="concat('xsldoc/',@value)"/>
                                    <li><a href="{$target}"><img alt="XSL" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AwHCAggfxOf4gAAAeNJREFUOMulk71rU1EYxn8n9+K9EpQMJZQMTQatONhJSnDKoND+AxmyWgcHC03xYxI6BcEWBwlK00VLSAJZ2qXgYLa2QkoXLWq0zSq45KM3Mbnndem9Jm0qgs90znt4fud9eM9RIiLVapVarcZ5EhG01qRSKUQEpZR/pkREisUiszMziIhvEBG63S6dToder0d1b8+HDCowuPHISimUUliWhW3buK6L1ppkMkk+nz8HMGA+DTEMA4BSqQQwBDF9PyBKwUnGwazBYJBrk5M0m01s2+b74eFZwN8glmURiURot9v0+/3REZRSrH66wWJ26kwc0zR5+uYWGz9vEwqF/Ej+FABWdqIcVBQvHnwE4PHrm3R/OVgXLpK59wFESGenuJ4Q0vH60IxleXtC5jJRcRxHHMeR+XVD1nanpe6+lbXdaZlfN6Tdakmr1ZK5TFSWtyfEU8C7Obv4FcMwMAyDxrGLM7bP5tFdnLF9GscupmlimiYvFz5zUFGs7ET/RPAgrx5+A+D9l2c0Lr0jrBL8kAqXm3dIXH0EwP3nV4ZjeK14MbTWorWWpa1xWdhAlrbG/drp9uXkyUqhUBiCjNKguVwu+/Whd5CO1yE++kOtPjkaWQ/wnzIBwuEwuVzun02xWMxf/wbanEBKrN+vTQAAAABJRU5ErkJggg=="/><xsl:text> </xsl:text><xsl:value-of select="$relativePath"/></a></li>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </ul>
            </body>
        </html>
    </xsl:template>
    
    <xsl:function name="xsldoc:getPathOf" as="xs:string">
        <xsl:param name="file" as="xs:string"/>
        <xsl:sequence select="string-join(tokenize($file, '/')[position() lt last()], '/')"/>
    </xsl:function>
    
</xsl:stylesheet>