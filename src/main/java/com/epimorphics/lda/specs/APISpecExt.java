package com.epimorphics.lda.specs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.ModelLoader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class APISpecExt extends APISpec {
	
	private static Logger LOG = LoggerFactory.getLogger(APISpecExt.class);

	public APISpecExt(FileManager fm, Resource specification, ModelLoader loader) {
		super(fm, specification, loader);
	}	
	
	@Override	
	protected APIEndpointSpec getAPIEndpointSpec(Resource endpoint) {
		LOG.debug("Initialising APIEndpointSpecExt...");
		return new APIEndpointSpecExt( this, this, endpoint );
	}

}