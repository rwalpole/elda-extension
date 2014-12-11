package com.epimorphics.lda.restlets;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.APIFactory;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.routing.*;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specmanager.SpecManagerExtImpl;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.specs.APISpecExt;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.LocatorFile;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rob on 11/02/2014.
 */
public class RouterRestletSupportExt extends RouterRestletSupport {

    public static Router createRouterFor( ServletContext con ) {
        String contextName = RouterRestletSupport.flatContextPath( con.getContextPath() );
        List<PrefixAndFilename> pfs = prefixAndFilenames( con, contextName );
        //
        Router router = new DefaultRouter();
        String baseFilePath = ServletUtils.withTrailingSlash(con.getRealPath("/"));
<<<<<<< HEAD
=======
        //AuthMap am = AuthMap.loadAuthMap( EldaFileManager.get(), noNamesAndValues );
>>>>>>> 2d9ca82cee965f0f07c1d970a53c3b0ea4627520
        ModelLoader modelLoader = new APIModelLoader( baseFilePath );
        addBaseFilepath( baseFilePath );
        //
        SpecManagerImpl sm = new SpecManagerExtImpl(router, modelLoader);
        SpecManagerFactory.set(sm);
        //
        for (PrefixAndFilename pf: pfs) {
<<<<<<< HEAD
            RouterRestletSupportExt.loadOneConfigFile( router, "", modelLoader, pf.prefixPath, pf.fileName );
=======
            loadOneConfigFile( router, "", modelLoader, pf.prefixPath, pf.fileName );
>>>>>>> 2d9ca82cee965f0f07c1d970a53c3b0ea4627520
        }
        int count = router.countTemplates();
        return count == 0  ? RouterFactory.getDefaultRouter() : router;
    }

    /*
     * Hidden static method - has the same signature as a method in the superclass but is used to create a
     * APISpecExt instead of an APISpec
     */
    public static void loadOneConfigFile(Router router, String appName, ModelLoader ml, String prefixPath, String thisSpecPath) {
        log.info( "Loading spec file from " + thisSpecPath + " with prefix path " + prefixPath );
        Model init = ml.loadModel( thisSpecPath );
        ServletUtils.addLoadedFrom( init, thisSpecPath );
        log.info( "Loaded " + thisSpecPath + ": " + init.size() + " statements" );
        for (ResIterator ri = init.listSubjectsWithProperty( RDF.type, API.API ); ri.hasNext();) {
            Resource api = ri.next();
            Resource specRoot = init.getResource(api.getURI());
            try {
                SpecManagerFactory.get().addSpec(prefixPath, appName, prefixPath, api.getURI(), "", init );
            } catch (APISecurityException e) {
                throw new WrappedException(e);
            }
            APISpec apiSpec = new APISpecExt( EldaFileManager.get(), specRoot, ml );
            APIFactory.registerApi(router, prefixPath, apiSpec);
        }
    }

}
