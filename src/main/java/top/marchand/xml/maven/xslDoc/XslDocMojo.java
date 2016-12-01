/**
 * This Source Code Form is subject to the terms of 
 * the Mozilla Public License, v. 2.0. If a copy of 
 * the MPL was not distributed with this file, You 
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.maven.xslDoc;

import fr.efl.chaine.xslt.GauloisPipe;
import fr.efl.chaine.xslt.InvalidSyntaxException;
import fr.efl.chaine.xslt.SaxonConfigurationFactory;
import fr.efl.chaine.xslt.config.Config;
import fr.efl.chaine.xslt.config.ConfigUtil;
import fr.efl.chaine.xslt.utils.ParameterValue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
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


    @Override
    public void generate(Sink sink, Locale locale) throws MavenReportException {
        getLog().debug("generate(Sink, Locale)");
        try {
            File gauloisConfig = generateGauloisConfig();
            if(gauloisConfig==null) {
                throw new MavenReportException("Unable to build gaulois-pipe config file. See previous errors.");
            }
            GauloisPipe piper = new GauloisPipe(new SaxonConfigurationFactory() {
                @Override
                public Configuration getConfiguration() {
                    return proc.getUnderlyingConfiguration();
                }
            });
            // try to create commandLine
            URL url = getClass().getResource("/top/marchand/xml/xslDoc/XslDocMojo.class");
            String jarPluginUrl = url.toExternalForm().substring(4);
            jarPluginUrl = jarPluginUrl.substring(0, jarPluginUrl.indexOf("!/"));
            getLog().debug("Jar File URL : "+jarPluginUrl);
            
            ArtifactFactory factory = new DefaultArtifactFactory();
            factory.createPluginArtifact("top.marchand.xml", "xslDoc-maven-plugin", vr)
            Commandline cmdLine = new Commandline();
            cmdLine.
            
            
            ConfigUtil cu = new ConfigUtil(proc.getUnderlyingConfiguration(), piper.getUriResolver(), gauloisConfig.toURI().toURL().toExternalForm());
            HashMap<String, ParameterValue> parameters = new HashMap<>();
            parameters.put("sources", new ParameterValue("sources", xslDirectory.getAbsolutePath()));
            parameters.put("outputFolder", new ParameterValue("outputFolder", outputDirectory.getAbsolutePath()));
            Config config = cu.buildConfig(parameters);
            piper.setConfig(config);
            piper.setInstanceName("XSL-DOC");
            piper.launch();
            if(gauloisConfig.exists()) {
                if(keepGeneratedConfigFile) {
                    getLog().debug("Omitting config file deletion : "+gauloisConfig.getAbsolutePath());
                } else {
                    gauloisConfig.delete();
                }
            }
        } catch (SaxonApiException | IOException | InvalidSyntaxException | URISyntaxException ex) {
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
        transformer.setParameter(QName.XS_NAME, configTemplate);
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
    
}
