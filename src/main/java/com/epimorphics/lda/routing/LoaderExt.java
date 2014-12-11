package com.epimorphics.lda.routing;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.restlets.RouterRestletSupport;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specmanager.SpecManagerExtImpl;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.vocabularies.ELDA;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.parsers.FactoryConfigurationError;

public class LoaderExt extends Loader {
	
	private static final long serialVersionUID = 3444577768749490791L;

    @Override public void init() {
        ServletConfig fig = getServletConfig();
        ServletContext sc = getServletContext();
        baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
        configureLog4J();
        log.info( "\n\n  Starting Elda (Loader) " + Version.string + " " + ELDA.tag + "\n" );
        log.info( "baseFilePath: " + baseFilePath );
        String prefixPath = getInitParameter( Container.INITIAL_SPECS_PREFIX_PATH_NAME );
        ServletUtils.setupLARQandTDB( sc );
        modelLoader = new APIModelLoader( baseFilePath );
        EldaFileManager.get().addLocatorFile( baseFilePath );
        //
        //AuthMap am = AuthMap.loadAuthMap( EldaFileManager.get(), wrapParameters() );
        //
        SpecManagerFactory.set(new SpecManagerExtImpl(RouterFactory.getDefaultRouter(), modelLoader));
        //
        String contextName = RouterRestletSupport.flatContextPath(sc.getContextPath());

        for (String specTemplate : ServletUtils.getSpecNamesFromContext(adaptConfig(fig))) {
            String specPath = specTemplate.replaceAll( "\\{APP\\}", contextName );
            ServletUtils.loadSpecsFromFiles( "", modelLoader, baseFilePath, prefixPath, specPath );
        }
    }

    /**
     We do this because for reasons that are not completely clear the
     given ServletContext doesn't have the binding for INITIAL_SPECS_PARAM_NAME,
     but the ServletConfig does.
     */
    private ServletUtils.GetInitParameter adaptConfig(final ServletConfig fig) {
        return new ServletUtils.GetInitParameter() {

            @Override public String getInitParameter(String name) {
                return fig.getInitParameter(name);
            }
        };
    }

    // Putting log4j.properties in the classes root as normal doesn't
    // seem to work in WTP even though it does for normal tomcat usage
    // This is an attempt to force logging configuration to be loaded

    private void configureLog4J() throws FactoryConfigurationError {
        String file = getInitParameter(Container.LOG4J_PARAM_NAME);
        if (file == null) file = "log4j.properties";
        if (file != null) {
            if (file.endsWith( ".xml" )) {
                DOMConfigurator.configure(baseFilePath + file);
            }
            else {
                PropertyConfigurator.configure(baseFilePath + file);
            }
        }
    }

}
