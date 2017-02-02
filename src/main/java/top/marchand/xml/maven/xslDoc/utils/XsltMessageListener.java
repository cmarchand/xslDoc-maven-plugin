/**
 * This Source Code Form is subject to the terms of 
 * the Mozilla Public License, v. 2.0. If a copy of 
 * the MPL was not distributed with this file, You 
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.maven.xslDoc.utils;

import javax.xml.transform.SourceLocator;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.XdmNode;

/**
 * A XsltMessageListener
 * @author cmarchand
 */
public class XsltMessageListener implements MessageListener {

    @Override
    public void message(XdmNode xn, boolean bln, SourceLocator sl) {
        System.out.println("[xsl:message] "+xn.toString());
    }
    
}
