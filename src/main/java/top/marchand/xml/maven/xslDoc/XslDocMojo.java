/**
 * This Source Code Form is subject to the terms of 
 * the Mozilla Public License, v. 2.0. If a copy of 
 * the MPL was not distributed with this file, You 
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.maven.xslDoc;

import fr.efl.chaine.xslt.GauloisPipe;
import fr.efl.chaine.xslt.SaxonConfigurationFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.maven.doxia.sink.render.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import top.marchand.xml.protocols.ProtocolInstaller;

/**
 * The plugin implementation
 * @author cmarchand
 */
@Mojo(name = "xsl-doc", threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class XslDocMojo extends AbstractMojo implements MavenReport {
    
    @Parameter(required = true, defaultValue = "${project.build.directory}/xsldoc")
    private File outputDirectory;
    
    @Parameter(defaultValue = "false", property = "maven.javadoc.skip")
    private boolean skip;
    
    @Parameter( alias = "xslDirectory", defaultValue = "${basedir}/src/main/xsl" )
    private File xslDirectory;
    
    @Parameter(alias = "keepConfigFile", defaultValue = "false")
    private boolean keepGeneratedConfigFile;
    
    private Processor proc;
    private DocumentBuilder builder;
    private XsltCompiler xslCompiler;
    
    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;


    @Override
    public void generate(Sink sink, Locale locale) throws MavenReportException {
        getLog().debug("generate(Sink, Locale)");
        try {
            File gauloisConfig = generateGauloisConfig();
            if(gauloisConfig==null) {
                throw new MavenReportException("Unable to build gaulois-pipe config file. See previous errors.");
            }
            // create commandLine
            String classPath = createClasspath();
            Commandline cmd = new Commandline("java");
            cmd.addArg(createArgument("-cp"));
            cmd.addArg(createArgument(classPath));
            cmd.addArg(createArgument(GauloisPipe.class.getName()));
            cmd.addArg(createArgument("--config"));
            cmd.addArg(createArgument(gauloisConfig));
            cmd.addArg(createArgument("--instance-name"));
            cmd.addArg(createArgument("XSL-DOC"));
            cmd.addArg(createArgument("PARAMS"));
            cmd.addArg(createArgument("sources="+basedir.toPath().relativize(xslDirectory.toPath())));
            cmd.addArg(createArgument("outputFolder="+outputDirectory.getAbsolutePath()));
            cmd.addArg(createArgument("basedir="+basedir.getAbsolutePath()));
            
            getLog().info("CmdLine: "+cmd.toString());
            Process process = cmd.execute();
            // redirecting standard output
            BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            do {
                s = is.readLine();
                System.out.println(s);
            } while(s!=null);
            int ret = process.waitFor();
            if(ret!=0) {
                is = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                do {
                    s = is.readLine();
                    System.out.println(s);
                } while(s!=null);
                throw new MavenReportException("gaulois-pipe exit with code "+ret);
            }
            
            if(gauloisConfig.exists()) {
                if(keepGeneratedConfigFile) {
                    getLog().debug("Omitting config file deletion : "+gauloisConfig.getAbsolutePath());
                } else {
                    gauloisConfig.delete();
                }
            }
        } catch (SaxonApiException | IOException | URISyntaxException | CommandLineException | InterruptedException ex) {
            throw new MavenReportException(ex.getMessage(), ex);
        }
    }

    @Override
    public String getOutputName() {
        getLog().debug("getOutputName()");
        return "XSL Documentation";
    }

    @Override
    public String getCategoryName() {
        getLog().debug("getCategoryName()");
        return MavenReport.CATEGORY_PROJECT_REPORTS;
    }

    @Override
    public String getName(Locale locale) {
        getLog().debug("getName(Locale)");
        return "XSL Doc";
    }

    @Override
    public String getDescription(Locale locale) {
        getLog().debug("getDescription(Locale)");
        return "XSL documentation";
    }

    @Override
    public void setReportOutputDirectory(File file) {
        getLog().debug("setOutputDirectory("+file.getAbsolutePath()+")");
        this.outputDirectory = file;
    }

    @Override
    public File getReportOutputDirectory() {
        getLog().debug("getOutputDirectory() -> "+outputDirectory.getAbsolutePath());
        return outputDirectory;
    }

    @Override
    public boolean isExternalReport() {
        getLog().debug("isExternalReport()");
        return true;
    }

    @Override
    public boolean canGenerateReport() {
        getLog().debug("canGenerateReport()");
        return true;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(skip) {
            getLog().info("Skipping xsl doc generation");
            return;
        }
        RenderingContext context = new RenderingContext( outputDirectory, getOutputName() + ".html" );
        SiteRendererSink sink = new SiteRendererSink( context );
        Locale locale = Locale.getDefault();
        try {
            generate( sink, locale );
        } catch (MavenReportException ex) {
            throw new MojoExecutionException("while generating XSL Documentation", ex);
        }
    }
    
    private File generateGauloisConfig() throws SaxonApiException, IOException {
        initSaxon();
        InputStream templateStream = getClass().getResourceAsStream("/xsl-doc_gp.xml");
        if(templateStream==null) {
            getLog().error("Unable to load /xsl-doc_gp.xml from classpath.");
            return null;
        }
        XdmNode configTemplate = builder.build(new StreamSource(templateStream));
        XsltExecutable exec = xslCompiler.compile(new StreamSource(getClass().getResourceAsStream("/gp-generator.xsl")));
        XsltTransformer transformer = exec.load();
        File configFile = File.createTempFile("gp-", ".xml");
        Serializer serializer = proc.newSerializer(configFile);
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
        transformer.setDestination(serializer);
        transformer.setInitialContextNode(configTemplate);
        transformer.transform();
        return configFile;
    }
    
    /**
     * Initialize Saxon stuff
     */
    private void initSaxon() {
        if(proc!=null) return;
        ProtocolInstaller.registerAdditionalProtocols();
        proc = new Processor(Configuration.newConfiguration());
        builder = proc.newDocumentBuilder();
        xslCompiler = proc.newXsltCompiler();
    }
    
    /**
     * Creates the classpath, based on classloader.
     * This works only if classloader is a URLClassLoader, which is guaranted
     * in maven.
     * @return The classpath to use in command line
     * @throws URISyntaxException 
     */
    private String createClasspath() throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        String bestStart = null;
        ClassLoader cl = getClass().getClassLoader();
        if(cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader)cl;
            for(URL u:ucl.getURLs()) {
                String path = new File(u.toURI()).getAbsolutePath();
                if(path.contains("slf4j") && path.contains("jcl")) {
                    continue;
                }
                getLog().debug("Adding to classpath : "+u.toExternalForm());
                if(bestStart==null) {
                    bestStart = path;
                } else {
                    String tmp = bestStart;
                    while(!path.startsWith(tmp) && tmp.length()>=2) {
                        tmp = tmp.substring(0, tmp.length()-2);
                    }
                    if(tmp.length()>2 && !tmp.equals(System.getProperty("user.home"))) bestStart=tmp;
                }
                sb.append(path).append(File.pathSeparator);
            }
            // here, bestStart should be the .m2 repository
            // awfull hack to add slf4j-api in classpath which is removed somewhere, don't know why
            String slf4japiPath = bestStart.concat("org/slf4j/slf4j-api/1.5.6/slf4j-api-1.5.6.jar");
            getLog().info("SLF4J: "+slf4japiPath);
            sb.append(slf4japiPath);
        }
        return sb.toString();
    }
    
    private Commandline.Argument createArgument(String value) {
        Commandline.Argument arg = new Commandline.Argument();
        arg.setLine(value);
        return arg;
    }
    private Commandline.Argument createArgument(File file) {
        Commandline.Argument arg = new Commandline.Argument();
        arg.setFile(file);
        return arg;
    }
    
}
