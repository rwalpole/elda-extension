package com.epimorphics.lda.tests;

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.routing.MatchSearcher;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Triad;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class TestConstruct {

    @Ignore
    @Test public void testDefaultTemplateInSpec() {
		testConstruct(
				"",
				"",
				"; ext:construct '" +
						"CONSTRUCT{?parent dri:childCount ?childCount}"+
						"WHERE{SELECT ?parent (COUNT(?child) AS ?childCount)"+
						"WHERE {?parent dri:catalogUUID ?uuid ."+
						"?child dri:catalogParentUUID ?parent."+
						"}GROUP BY ?parent}'"
		);
	}
	
	/**
	Ensure that running the configured endpoint correctly exposes in
	the view the :predicate item from the model but not the :catiprede.
	
	<p><code>params</code> may be applied to the query part of the URL
	to select a specific view. <code>inSpec</code> is a Turtle fragment
	inserted into the API declaration. <code>inEndpoint</code> is a
	Turtle fragment inserted into the endpoint declaration.
	*/
	public void testConstruct( String params, String inSpec, String inEndpoint ) {
		
		String prefixes = "@prefix : <eh:/> . @prefix dri: <http://wwww.devexe.co.uk/vocabularies/dri#> .";

        StringBuilder builder = new StringBuilder(prefixes);
        builder.append("@prefix api:     <http://purl.org/linked-data/api/vocab#> .");
        builder.append("@prefix dc:      <http://purl.org/dc/elements/1.1/> .");
        builder.append("@prefix geo:     <http://www.w3.org/2003/01/geo/wgs84_pos#> .");
        builder.append("@prefix owl:     <http://www.w3.org/2002/07/owl#> .");
        builder.append("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        builder.append("@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .");
        builder.append("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .");
        builder.append("@prefix foaf:    <http://xmlns.com/foaf/0.1/> .");
        builder.append("@prefix school:  <http://education.data.gov.uk/def/school/> .");
        builder.append("@prefix skos:    <http://www.w3.org/2004/02/skos/core#> .");
        builder.append("@prefix ext:     <http://wwww.devexe.co.uk/vocabularies/ext#> .");
        builder.append("@prefix dri:     <http://wwww.devexe.co.uk/vocabularies/dri#> .");
        builder.append(":root a api:API");
        builder.append("; api:sparqlEndpoint <here:dataPart>");
        builder.append(inSpec);
        builder.append("; api:endpoint :ep");
        builder.append(".");
        builder.append(":ep a api:ItemEndpoint");
        builder.append("; api:variable [api:name \"uuid\"; api:type xsd:string]");
        builder.append("; api:uriTemplate '/child-count/{uuid}'");
        builder.append(inEndpoint);
        builder.append(".");
        builder.append("<here:dataPart> :elements <http://localhost:2020/dri/catalog/45b57b49-9abf-4fe0-b82d-87c984564a6c>,");
        builder.append("<http://localhost:2020/dri/catalog/e2ddcb69-55b0-4816-9792-d0abab5dc6f9>,");
        builder.append("<http://localhost:2020/dri/catalog/6a1eb17a-7c08-4927-a71a-0cd6c29c252a>");
        builder.append(".");
        builder.append("<http://localhost:2020/dri/catalog/45b57b49-9abf-4fe0-b82d-87c984564a6c> a dri:catalog ;");
        builder.append("dri:catalogBatchUUID <http://localhost:2020/dri/batch/08dacdd9-9f81-4411-a87b-0b68185ecfa7> ;");
        builder.append("dri:catalogCollectionUUID <http://localhost:2020/dri/collection/e9f3c8e9-e883-4fcf-a9a3-5caf0c808c5d> ;");
        builder.append("dri:catalogKnown  \"2012-06-04T00:00:00\"^^xsd:dateTime ;");
        builder.append("dri:catalogReference \"WO/16/409/27_31\"^^xsd:string ;");
        builder.append("dri:catalogUUID  \"45b57b49-9abf-4fe0-b82d-87c984564a6c\"^^xsd:string .");
        builder.append("<http://localhost:2020/dri/catalog/e2ddcb69-55b0-4816-9792-d0abab5dc6f9> a dri:catalog ;");
        builder.append("dri:catalogBatchUUID <http://localhost:2020/dri/batch/08dacdd9-9f81-4411-a87b-0b68185ecfa7> ;");
        builder.append("dri:catalogCollectionUUID <http://localhost:2020/dri/collection/e9f3c8e9-e883-4fcf-a9a3-5caf0c808c5d> ;");
        builder.append("dri:catalogKnown  \"2012-06-04T00:00:00\"^^xsd:dateTime ;");
        builder.append("dri:catalogParentUUID <http://localhost:2020/dri/catalog/45b57b49-9abf-4fe0-b82d-87c984564a6c> ;");
        builder.append("dri:catalogReference \"WO/16/409/27_31/112\"^^xsd:string ;");
        builder.append("dri:catalogUUID  \"e2ddcb69-55b0-4816-9792-d0abab5dc6f9\"^^xsd:string .");
        builder.append("<http://localhost:2020/dri/catalog/6a1eb17a-7c08-4927-a71a-0cd6c29c252a> a dri:catalog ;");
        builder.append("dri:catalogBatchUUID <http://localhost:2020/dri/batch/08dacdd9-9f81-4411-a87b-0b68185ecfa7> ;");
        builder.append("dri:catalogCollectionUUID <http://localhost:2020/dri/collection/e9f3c8e9-e883-4fcf-a9a3-5caf0c808c5d> ;");
        builder.append("dri:catalogKnown  \"2012-06-04T00:00:00\"^^xsd:dateTime ;");
        builder.append("dri:catalogParentUUID <http://localhost:2020/dri/catalog/45b57b49-9abf-4fe0-b82d-87c984564a6c> ;");
        builder.append("dri:catalogReference \"WO/16/409/27_31/142\"^^xsd:string ;");
        builder.append("dri:catalogUUID  \"6a1eb17a-7c08-4927-a71a-0cd6c29c252a\"^^xsd:string .");

		Model specModel = ModelIOUtils.modelFromTurtle(builder.toString());
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		
		APISpec spec = SpecUtil.specFrom( root );
				
		APIEndpoint ep = new APIEndpointImpl( spec.getEndpoints().get(0) );
		Bindings epBindings = ep.getSpec().getBindings();
		epBindings.put("uuid", "45b57b49-9abf-4fe0-b82d-87c984564a6c");
		MultiMap<String, String> map = MakeData.parseQueryString( params );
		URI ru = URIUtils.newURI( "/this" );
		Bindings cc = Bindings.createContext( bindTemplate( epBindings, "/this", "/path", map ), map );
        APIEndpoint.Request request = new APIEndpoint.Request(controls, ru, cc);
		Triad<APIResultSet, Map<String, String>, Bindings> resultsAndFormat = ep.call( request );
		Model rsm = resultsAndFormat.a.getMergedModel();
		
		Model obtained = ModelFactory.createDefaultModel();
		obtained.add( rsm );
		
		assertHas( obtained, prefixes, "<http://localhost:2020/dri/catalog/45b57b49-9abf-4fe0-b82d-87c984564a6c> dri:childCount 2" );
	}

	private void assertHas( Model obtained, String ... lines ) {
		Model wanted = modelFrom( lines );
		for (Statement s: wanted.listStatements().toList())
			if (!obtained.contains( s ))
				fail("missing required statement: " + s );
	}
	
	private Bindings bindTemplate( Bindings epBindings, String template, String path, MultiMap<String, String> qp ) {
		MatchSearcher<String> ms = new MatchSearcher<String>();
		ms.register( template, "IGNORED" );
		Map<String, String> bindings = new HashMap<String, String>();
		ms.lookup( bindings, path, qp );
		return epBindings.updateAll( bindings ); 
	}
	
	static final Controls controls = new Controls( true, new Times() );
	
	private Model modelFrom(String ... lines) {
		StringBuilder ttl = new StringBuilder();
		for (String line: lines) ttl.append( line ).append( '\n' );
		return ModelIOUtils.modelFromTurtle( ttl.toString() );
	}

}