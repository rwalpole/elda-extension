package com.epimorphics.lda.specmanager;

import com.epimorphics.lda.specs.APISpecExt;
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

}