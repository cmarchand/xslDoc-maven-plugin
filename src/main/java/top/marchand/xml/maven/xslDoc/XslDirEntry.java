/**
 * This Source Code Form is subject to the terms of
 * the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.maven.xslDoc;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author cmarchand
 */
public class XslDirEntry implements Serializable {

    public String xslDirectory;
    public int levelsToKeep;
    public boolean recurse;

    public XslDirEntry() {
        super();
        levelsToKeep = 0;
        recurse = true;
    }

    public XslDirEntry(String xslDirectory, int levelsToKeep, boolean recurse) {
        this();
        this.xslDirectory = xslDirectory;
        this.levelsToKeep = levelsToKeep;
        this.recurse = recurse;
    }

    public void setXslDirectory(String xslDirectory) {
        this.xslDirectory = xslDirectory;
    }

    public void setLevelsToKeep(int levelsToKeep) {
        this.levelsToKeep = levelsToKeep;
    }

    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

    public File getXslDirectory(final File basedir) {
        File ret = new File(basedir, this.xslDirectory);
        if (ret.exists()) {
            return ret;
        }
        throw new IllegalArgumentException("in directory " + this.xslDirectory + " configuration, " + ret.getAbsolutePath() + " does not exists");
    }

}
