package com.epimorphics.lda.query;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.rdfq.SparqlSupport;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.NoteBoard;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.Couple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.ArrayList;
import java.util.List;


public class APIQueryExt extends APIQuery {
	
	private boolean isConstructQuery;
	
	private String constructString;
	
	public APIQueryExt(APIQuery other, boolean isConstructQuery, String constructString) {
		super(other);
		this.isConstructQuery = isConstructQuery;
		this.constructString = constructString;
	}
	
	public APIQueryExt(QueryBasis qb, boolean isConstructQuery, String constructString) {
		super(qb);
		this.isConstructQuery = isConstructQuery;
		this.constructString = constructString;
	}
	
	@Override
	protected APIResultSet runQueryWithSource(NoteBoard nb, Controls c, APISpec spec, Bindings call, String graphName, View view, Source source ) {
		APIResultSet rs;
		Times t = c.times;
		long origin = System.currentTimeMillis();
		if(!isConstructQuery()) {
            Couple<String, List<Resource>> queryAndResults = selectResources(c, spec, call, source);
            long afterSelect = System.currentTimeMillis();
            //
            t.setSelectionDuration(afterSelect - origin);
            String outerSelect = queryAndResults.a;
            List<Resource> results = queryAndResults.b;
            //
            rs = fetchDescriptionOfAllResources(c, outerSelect, spec, graphName, view, results);
            //
            long afterView = System.currentTimeMillis();
            t.setViewDuration(afterView - afterSelect);
            rs.setSelectQuery(outerSelect);
		}
		else {
			Model resultModel = runConstructQuery( c, spec, call, source );
			ResIterator resourceIterator = resultModel.listSubjects();
			List<Resource> results = new ArrayList<Resource>();
			while(resourceIterator.hasNext()) {
				results.add(resourceIterator.next());
			}
			rs = new APIResultSet(resultModel.getGraph(), results, resultModel.size() < pageSize, enableETags, "", view );
		}
		
		return rs;
	}
	
	private boolean isConstructQuery() {
		return isConstructQuery;
	}
	
	private Model runConstructQuery( Controls c, APISpec spec, Bindings cc, Source source ) {
		String constructQuery = assembleConstructQuery( cc, spec.getPrefixMap() );
		Times times = c.times;
		times.setSelectQuerySize( constructQuery );
		//List<Resource> already = cache.getCachedResources( constructQuery );
		/*if (c.allowCache && already != null)
		    {
			c.getTimes().usedSelectionCache();
		    if (log.isDebugEnabled()) log.debug( "re-using cached results for query " + constructQuery );
		    return new Couple<String, List<Resource>>(constructQuery, already);
		    }*/
		Query q = createQuery( constructQuery );
		if (log.isDebugEnabled()) log.debug( "Running query: " + constructQuery.replaceAll( "\n", " " ) );
		return source.executeConstruct(q);
	}
	
	private String assembleConstructQuery( Bindings cc, PrefixMapping prefixes ) {  	
    	PrefixLogger pl = new PrefixLogger( prefixes );   
    	return assembleRawConstructQuery( pl, cc );
    }

	private String assembleRawConstructQuery( PrefixLogger pl, Bindings cc ) {
    	
		// Add prefixes
		StringBuilder sparqlConstruct = new StringBuilder();
		SparqlSupport.appendPrefixes(sparqlConstruct, pl.getPrefixMapping() );
		
    	// Add bound variables
    	String boundConstruct = bindDefinedvariables( pl, constructString, cc );
    	
    	sparqlConstruct.append(boundConstruct);
		return sparqlConstruct.toString();
    }
	
	@Override
	public APIQuery copy() {
		return new APIQueryExt( this, this.isConstructQuery, this.constructString );
	}

}