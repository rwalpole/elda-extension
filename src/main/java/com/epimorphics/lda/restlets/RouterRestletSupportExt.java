package com.epimorphics.lda.restlets;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.routing.*;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specmanager.SpecManagerExtImpl;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.LocatorFile;
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
        Router result = new DefaultRouter();
        String baseFilePath = ServletUtils.withTrailingSlash(con.getRealPath("/"));
        AuthMap am = AuthMap.loadAuthMap( EldaFileManager.get(), noNamesAndValues );
        ModelLoader modelLoader = new APIModelLoader( baseFilePath );
        addBaseFilepath( baseFilePath );
        //
        SpecManagerImpl sm = new SpecManagerExtImpl(result, modelLoader);
        SpecManagerFactory.set(sm);
        //
        for (PrefixAndFilename pf: pfs) {
            loadOneConfigFile( result, am, modelLoader, pf.prefixPath, pf.fileName );
        }
        int count = result.countTemplates();
        return count == 0  ? RouterFactory.getDefaultRouter() : result;
    }

    /**
     Add the baseFilePath to the FileManager singleton. Only do it
     once, otherwise the instance will get larger on each config load
     (at least that won't be once per query, though). Just possibly
     there may be multiple servlet contexts so we add a new only only if
     its not already in the instance's locator list.
     */
    // FIXME - this method needs to be protected in RouterRestletSupport
    private static void addBaseFilepath(String baseFilePath) {
        FileManager fm = EldaFileManager.get();
        for (Iterator<Locator> il = fm.locators(); il.hasNext();) {
            Locator l = il.next();
            if (l instanceof LocatorFile)
                if (((LocatorFile) l).getName().equals(baseFilePath))
                    return;
        }
        log.info( "adding locator for " + baseFilePath );
        EldaFileManager.get().addLocatorFile( baseFilePath );
    }
}
