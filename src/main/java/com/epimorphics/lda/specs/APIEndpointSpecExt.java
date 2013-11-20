package com.epimorphics.lda.specs;

import com.epimorphics.lda.query.APIQueryExt;
import com.epimorphics.lda.vocabularies.EXT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.query.APIQuery;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class APIEndpointSpecExt extends APIEndpointSpec {
	
	private static Logger LOG = LoggerFactory.getLogger(APIEndpointSpecExt.class);
	
	public APIEndpointSpecExt(APISpec apiSpec, APISpec parent, Resource endpoint) {
    	super(apiSpec, parent, endpoint);
	}
	
	@Override
	protected void instantiateBaseQuery( Resource endpoint ) {
		LOG.debug("Instatiating base query for endpoint " + endpoint.toString());
        if(endpoint.hasProperty(EXT.construct)) {
        	addConstructor(endpoint);
        }
        else {
        	baseQuery = new APIQuery( this );
        	baseQuery.setEnableETags( enableETags( endpoint ) );
        	setAllowedReserved( endpoint, baseQuery );
        	addSelectors(endpoint);
        }        
    }
	
	private void addConstructor(Resource endpoint) {
		Statement constructStatement = endpoint.getProperty( EXT.construct );
		String constructString = constructStatement.getString();
		baseQuery = new APIQueryExt(this, true, constructString);
	}

}