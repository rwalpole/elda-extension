package com.epimorphics.lda.specmanager;

import com.epimorphics.lda.core.APIFactory;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.specs.APISpecExt;
import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class SpecManagerExtImpl extends SpecManagerImpl {
	
	private static Logger LOG = LoggerFactory.getLogger(SpecManagerExtImpl.class);

	public SpecManagerExtImpl(Router router, ModelLoader modelLoader) {
		super(router, modelLoader);
	}

	@Override
	public APISpec getAPISpec(Resource specRoot) {
		LOG.debug("Initialising APISpecExt...");
		return new APISpecExt(FileManager.get(), specRoot, modelLoader);
	}

    @Override
    public APISpec addSpec( String prefixPath, String appName, String context, String uri, String key, Model spec ) throws APISecurityException {
        if (specs.containsKey(uri)) {
            return updateSpec( prefixPath, appName, context, uri, key, spec );
        } else {
            log.info("Creating API spec at: " + uri);
            Resource specRoot = spec.getResource(uri);
            APISpec apiSpec = new APISpecExt(EldaFileManager.get(), specRoot, modelLoader );
            synchronized (specs) { specs.put(uri, new SpecEntry(uri, key, apiSpec, spec)); }
            APIFactory.registerApi(router, context, apiSpec);
            return apiSpec;
        }
    }

}