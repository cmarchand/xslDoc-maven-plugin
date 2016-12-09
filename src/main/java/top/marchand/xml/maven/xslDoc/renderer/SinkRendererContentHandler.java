/**
 * This Source Code Form is subject to the terms of 
 * the Mozilla Public License, v. 2.0. If a copy of 
 * the MPL was not distributed with this file, You 
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.maven.xslDoc.renderer;

import java.util.Stack;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This class transform SAX event from a XHTML parsing into Sink events
 * @author cmarchand
 */
public class SinkRendererContentHandler implements ContentHandler {
    private final Stack<_SinkEvent> stack;
    private final Log log;
    private final SiteRendererSink sink;
    private Locator locator;

    public SinkRendererContentHandler(Sink sink, Log log) {
        super();
        this.sink=(SiteRendererSink)sink;
        this.log=log;
        stack = new Stack<>();
    }

    @Override
    public void setDocumentLocator(Locator locator) { 
        this.locator=locator;
    }

    @Override
    public void startDocument() throws SAXException { 
    }

    @Override
    public void endDocument() throws SAXException { }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException { }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException { }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException { 
        switch(localName) {
            case "head": sink.head(); break;
            case "body": sink.body(); sink.section1(); break;
            case "h3": sink.sectionTitle1(); break;
            case "ul": sink.list(); break;
            case "li": sink.listItem(); break;
            case "img": {
                SinkEventAttributeSet attrs = new SinkEventAttributeSet();
                attrs.addAttribute(SinkEventAttributes.SRC, atts.getValue("src"));
                attrs.addAttribute(SinkEventAttributes.ALT, atts.getValue("alt"));
                sink.figure(attrs);
                break;
            }
            case "a": {
                SinkEventAttributeSet attrs = new SinkEventAttributeSet();
                attrs.addAttribute(SinkEventAttributes.HREF, atts.getValue("href"));
                stack.push(new _SinkEvent("a", attrs));
                break;
            }
            case "title": sink.title(); break;
            case "html":
            case "style": break;
            default: log.error("unexpected html element : "+localName);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException { 
        switch(localName) {
            case "head": sink.head_(); break;
            case "body": sink.section1_(); sink.body_(); break;
            case "h3": sink.sectionTitle1_(); break;
            case "ul": sink.list_(); break;
            case "li": sink.listItem_(); break;
            case "img": sink.figure_(); break;
            case "a": {
                if(stack.empty()) {
                    log.error("try to close a <a> with an empty stack at "+locator.getLineNumber()+":"+locator.getColumnNumber());
                } else {
                    _SinkEvent evt = stack.pop();
                    sink.link(evt.content.toString(), evt.attributes);
                    sink.link_();
                }
                break;
            }
            case "title": sink.title_(); break;
            case "html":
            case "style": break;
            default: log.error("unexpected html element : "+localName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException { 
        if(!stack.empty()) {
            _SinkEvent evt = stack.peek();
            evt.content.append(ch, start, length);
        } else {
            sink.text(new String(ch, start, start));
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { }

    @Override
    public void processingInstruction(String target, String data) throws SAXException { }

    @Override
    public void skippedEntity(String name) throws SAXException { }
    
    private class _SinkEvent {
        String name;
        SinkEventAttributes attributes;
        StringBuilder content;

        public _SinkEvent(String name, SinkEventAttributes attributes) {
            this.name = name;
            this.attributes = attributes;
            content= new StringBuilder();
        }
        
    }
}
