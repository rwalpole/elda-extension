package com.epimorphics.lda.restlets;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.routing.ServletUtils;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.shared.WrappedException;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rob on 11/02/2014.
 */
public class RouterRestletExt extends RouterRestlet {

    /**
     Initialise this RouterRestletExt. Happens a lot, so expensive
     initialisations should be cached. Sets the router used by
     this instance according to the appropriate LDA configs.
     */
    public RouterRestletExt( @Context ServletContext con ) {
        super(con);
    }

    public static class Init implements ServletContextListener {

        static boolean announced = false;

        @Override public void contextInitialized(ServletContextEvent sce) {
            ServletContext sc = sce.getServletContext();
            if (announced == false) {
                String baseFilePath = ServletUtils.withTrailingSlash(sc.getRealPath("/"));
                String propertiesFile = "log4j.properties";
                PropertyConfigurator.configure(baseFilePath + propertiesFile);
                log.info( "\n\n    =>=> Starting Elda extended (Init)" + Version.string + "\n" );
                announced = true;
            }
            getRouterFor( sc );
        }

        @Override public void contextDestroyed(ServletContextEvent sce) {
        }
    }

    /**
     Answer a router initialised with the URI templates appropriate to
     this context path. Such a router may already be in the routers table,
     in which case it is used, otherwise a new router is created, initialised,
     put in the table, and returned.
     */
    static synchronized Router getRouterFor(ServletContext con) {
        // log.info( "getting router for context path '" + givenContextPath + "'" );
        String contextPath = RouterRestletSupport.flatContextPath(con.getContextPath());
        TimestampedRouter r = routers.get(contextPath);
        long timeNow = System.currentTimeMillis();
        //
        if (r == null) {
            log.info( "creating router for '" + contextPath + "'");
            long interval = getRefreshInterval(contextPath);
            r = new TimestampedRouter( RouterRestletSupportExt.createRouterFor(con), timeNow, interval );
            routers.put(contextPath, r );
        } else if (r.nextCheck < timeNow) {
            long latestTime = RouterRestletSupport.latestConfigTime(con, contextPath);
            if (latestTime > r.timestamp) {
                log.info( "reloading router for '" + contextPath + "'");
                long interval = getRefreshInterval(contextPath);
                r = new TimestampedRouter( RouterRestletSupportExt.createRouterFor(con), timeNow, interval );
                DOMUtils.clearCache();
                Cache.Registry.clearAll();
                routers.put( contextPath, r );
            } else {
                // checked, but no change to reload
                // log.info("don't need to reload router, will check again later." );
                r.deferCheck();
            }
        } else {
            // Don't need to check yet, still in waiting period
            // log.info( "Using existing router, not time to check yet." );
        }
        //
        return r.router;
    }

}
