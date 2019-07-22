package sm.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.accumulo.AccumuloRyaDAO;

import org.apache.rya.rdftriplestore.RdfCloudTripleStore;
import org.apache.rya.rdftriplestore.RyaSailRepository;
import org.apache.rya.rdftriplestore.inference.InferenceEngine;

import org.apache.accumulo.core.client.Connector;

import org.openrdf.query.*;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.*;
import java.util.Set;

public class TestInference {
    private static Connector connector;
    private static AccumuloRyaDAO dao;

    private static AccumuloRdfConfiguration conf;
    private static RdfCloudTripleStore store;
    private static InferenceEngine inferenceEngine;
    private static Repository repository;
    private static RepositoryConnection conn;

    public static void setUp() throws Exception{

   dao = new AccumuloRyaDAO();

        connector = new ZooKeeperInstance("myInstance", "masternode:2181").getConnector("root", "1234");
        dao.setConnector(connector);
        conf = new AccumuloRdfConfiguration();
        conf.setTablePrefix("testrya_");
        conf.setDisplayQueryPlan(true);
        dao.setConf(conf);
        dao.init();
        store = new RdfCloudTripleStore();
        store.setConf(conf);
        store.setRyaDAO(dao);
        inferenceEngine = new InferenceEngine();
        inferenceEngine.setRyaDAO(dao);
        inferenceEngine.refreshGraph();

       // inferenceEngine.setRefreshGraphSchedule(1000);
        store.setInferenceEngine(inferenceEngine);

        store.initialize();
       repository = new RyaSailRepository(store);

        conn = repository.getConnection();





        conn = repository.getConnection();

conn.close();
        //repository.initialize();
      load();

    }


    private static void load() throws RepositoryException, RDFParseException, IOException {
conn = repository.getConnection();

        // T-Box
        String ttlString = MODEL_TTL;
        InputStream stringInput = new ByteArrayInputStream(ttlString.getBytes());
        conn.add(stringInput, "http://test/inference2", RDFFormat.TURTLE);

        // A-Box
        ttlString = BUCKET_TTL;
        stringInput = new ByteArrayInputStream(ttlString.getBytes());
        conn.add(stringInput, "http://test/inference1", RDFFormat.TURTLE);

        conn.commit();
    }
    public static void testWithoutSubquery() throws RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, MalformedQueryException {
     TupleQuery  tupleQuery = null;
      /* final String query="PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ex: <http://example.org/> \n" +

                "select * where {?s rdf:type ex:animal.}";*/
      final String query = "SELECT ?i ?i_label ?i_class ?i_v1"
                + "WHERE {"
                + "?i <http://www.w3.org/2000/01/rdf-schema#label> ?i_label ."
                + "?i a ?i_class ."
                + "?i_class <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://dragon-research.com/cham/model/model1#Model1Class> ."
                + "OPTIONAL { ?i <http://dragon-research.com/cham/model/model1#name> ?i_v1 } ."
                + "}"
                + "ORDER BY ?i_label";


        conn = repository.getConnection();
        try {
             tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }
        TupleQueryResultHandler writer = new SPARQLResultsXMLWriter(System.out);
        try {
            tupleQuery.evaluate(writer);

        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } catch (TupleQueryResultHandlerException e) {
            e.printStackTrace();
        }

        conn.close();
    }
    public static void tearDown() throws Exception {

        repository.shutDown();
    }

  /*  private static String MODEL_TTL="@prefix :    <http://test/inference2#>. " +
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix ex:   <http://example.org/> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@base <http://www.w3.org/2002/07/owl#> .\n" +
            "\n" +
            "[ rdf:type owl:Ontology\n" +
            " ] .\n" +
            "<http://example.org/animal> rdf:type owl:Class .\n" +
            "<http://example.org/cat> rdf:type owl:Class ;\n" +
            "                         rdfs:subClassOf <http://example.org/animal> .\n" +
            "<http://example.org/dog> rdf:type owl:Class ;\n" +
            "                         rdfs:subClassOf <http://example.org/animal> .";

    private static String BUCKET_TTL ="@prefix :    <http://test/inference1#>. " +
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix ex:   <http://example.org/> .\n" +
            "@base <http://www.w3.org/2002/07/owl#> .\n" +
            "ex:dog1\t   rdf:type\t    ex:animal.\n" +
            "ex:cat1\t   rdf:type\t    ex:cat.\n" +
            "ex:cat\t   rdfs:subClassOf  ex:animal.\n" ;*/

    /**
     * The ontology t-box in turtle.
     */
 private static String MODEL_TTL = "@prefix :        <http://dragon-research.com/cham/model/model1#> ."
            + "@prefix cham:    <http://dragon-research.com/cham/schema#> ."
            + "@prefix dc:      <http://purl.org/dc/elements/1.1/> ."
            + "@prefix owl:     <http://www.w3.org/2002/07/owl#> ."
            + "@prefix qudt:    <http://data.nasa.gov/qudt/owl/qudt#> ."
            + "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
            + "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> ."
            + "@prefix unit:    <http://data.nasa.gov/qudt/owl/unit#> ."
            + "@prefix xml:     <http://www.w3.org/XML/1998/namespace> ."
            + "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> ."
            + ""
            + "<http://dragon-research.com/cham/model/model1>"
            + "      rdf:type owl:Ontology ;"
            + "      rdfs:label \"Model1 Ontology\"^^xsd:string ;"
            + "      :versionInfo \"0.1\"^^xsd:string ;"
            + "      dc:title \"Model1 Ontology\"^^xsd:string ."
            + ""
            + ":ModelClassD"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"ModelClassD\"^^xsd:string ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:maxQualifiedCardinality"
            + "                        \"1\"^^xsd:nonNegativeInteger ;"
            + "                owl:onDataRange xsd:string ;"
            + "                owl:onProperty :name"
            + "              ] ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:allValuesFrom :Model1ClassAssoc ;"
            + "                owl:onProperty :hasModel1ClassAssoc"
            + "              ] ."
            + ""
            + ":ModelClassC"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"ModelClassC\"^^xsd:string ;"
            + "      rdfs:subClassOf :ModelClassD ."
            + ""
            + ":Modle1ClassB"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"Modle1ClassB\"^^xsd:string ;"
            + "      rdfs:subClassOf :ModelClassC ."
            + ""
            + ":Model1ClassA"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"Model1ClassA\"^^xsd:string ;"
            + "      rdfs:subClassOf :Modle1ClassB ."
            + ""
            + ":Model1Class"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"Model1Class\"^^xsd:string ;"
            + "      rdfs:subClassOf :Model1ClassA ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:maxQualifiedCardinality"
            + "                        \"1\"^^xsd:nonNegativeInteger ;"
            + "                owl:onDataRange xsd:string ;"
            + "                owl:onProperty :model1ClassId"
            + "              ] ."
            + ""
            + ":Model1Event"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"Model1Event\"^^xsd:string ;"
            + "      rdfs:subClassOf :Event ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:allValuesFrom :Model1ClassA ;"
            + "                owl:onProperty :hasModel1ClassA"
            + "              ] ."
            + ""
            + ":Model1ClassAssoc"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"Model1ClassAssoc\"^^xsd:string ;"
            + "      rdfs:subClassOf owl:Thing ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:maxQualifiedCardinality"
            + "                        \"1\"^^xsd:nonNegativeInteger ;"
            + "                owl:onDataRange xsd:string ;"
            + "                owl:onProperty :name"
            + "              ] ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:maxQualifiedCardinality"
            + "                        \"1\"^^xsd:nonNegativeInteger ;"
            + "                owl:onClass :ModelClassD ;"
            + "                owl:onProperty :hasEntity"
            + "              ] ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:allValuesFrom :ModelClassD ;"
            + "                owl:onProperty :hasEntity"
            + "              ] ."
            + ""
            + ":TemporalEntity"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"TemporalEntity\"^^xsd:string ;"
            + "      rdfs:subClassOf owl:Thing ."
            + ""
            + ":TemporalInstant"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"TemporalInstant\"^^xsd:string ;"
            + "      rdfs:subClassOf :TemporalEntity ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:maxQualifiedCardinality"
            + "                        \"1\"^^xsd:nonNegativeInteger ;"
            + "                owl:onDataRange xsd:dateTime ;"
            + "                owl:onProperty :dateTime"
            + "              ] ."
            + ""
            + ":model1ClassId"
            + "      rdf:type owl:DatatypeProperty ;"
            + "      rdfs:domain :Model1Class ;"
            + "      rdfs:label \"model1ClassId\"^^xsd:string ;"
            + "      rdfs:range xsd:string ."
            + ""
            + ":hasModel1ClassAssoc"
            + "      rdf:type owl:ObjectProperty ;"
            + "      rdfs:domain :ModelClassD ;"
            + "      rdfs:label \"hasModel1ClassAssoc\"^^xsd:string ;"
            + "      rdfs:range :Model1ClassAssoc ."
            + ""
            + ":name"
            + "      rdf:type owl:DatatypeProperty ;"
            + "      rdfs:domain :Model1ClassAssoc , :ModelClassD ;"
            + "      rdfs:label \"name\"^^xsd:string ;"
            + "      rdfs:range xsd:string ."
            + ""
            + ":hasTemporalEntity"
            + "      rdf:type owl:ObjectProperty ;"
            + "      rdfs:domain :ThreatAnalysis , :Event , :TrackingData , :Threat , :Vulnerability ;"
            + "      rdfs:label \"hasTemporalEntity\"^^xsd:string ;"
            + "      rdfs:range :TemporalEntity ."
            + ""
            + ":hasEntity"
            + "      rdf:type owl:ObjectProperty ;"
            + "      rdfs:domain :Model1ClassAssoc ;"
            + "      rdfs:label \"hasEntity\"^^xsd:string ;"
            + "      rdfs:range :ModelClassD ."
            + ""
            + ":dateTime"
            + "      rdf:type owl:DatatypeProperty ;"
            + "      rdfs:domain :TemporalInstant ;"
            + "      rdfs:label \"dateTime\"^^xsd:string ;"
            + "      rdfs:range xsd:dateTime ."
            + ""
            + ":Event"
            + "      rdf:type owl:Class ;"
            + "      rdfs:label \"Event\"^^xsd:string ;"
            + "      rdfs:subClassOf :ModelClassD ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:allValuesFrom :TemporalEntity ;"
            + "                owl:onProperty :hasTemporalEntity"
            + "              ] ;"
            + "      rdfs:subClassOf"
            + "              [ rdf:type owl:Restriction ;"
            + "                owl:maxQualifiedCardinality"
            + "                        \"1\"^^xsd:nonNegativeInteger ;"
            + "                owl:onClass :TemporalEntity ;"
            + "                owl:onProperty :hasTemporalEntity"
            + "              ] ."
            + ""
            + ":hasModel1ClassA"
            + "      rdf:type owl:ObjectProperty ;"
            + "      rdfs:domain :Model1Event ;"
            + "      rdfs:label \"hasModel1ClassA\"^^xsd:string ;"
            + "      rdfs:range :Model1ClassA ."
            + ""
            + "rdfs:label"
            + "      rdf:type owl:AnnotationProperty ."
            + ""
            + "xsd:date"
            + "      rdf:type rdfs:Datatype ."
            + ""
            + "xsd:time"
            + "      rdf:type rdfs:Datatype .";

    /**
     * The ontology a-box in turtle.
     */
  private static String BUCKET_TTL = "@prefix :        <http://dragon-research.com/cham/bucket/bucket1#> ."
            + "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> ."
            + "@prefix owl:     <http://www.w3.org/2002/07/owl#> ."
            + "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> ."
            + "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
            + "@prefix model1:   <http://dragon-research.com/cham/model/model1#> ."
            + ""
            + ":i1   a       model1:Model1Class ;"
            + "      rdfs:label \"Model1Class 1\"^^xsd:string ;"
            + "      model1:name \"Model1Class 1\"^^xsd:string ;"
            + "      model1:hasModel1ClassAssoc :i1-assoc ;"
            + "      model1:model1ClassId \"ID01\"^^xsd:string ."
            + "      "
            + ":i1-assoc a model1:Model1ClassAssoc ;"
            + "      rdfs:label \"Model1Class 1 Assoc\"^^xsd:string ;"
            + "      model1:hasEntity :i1-event ."
            + "      "
            + ":i1-event a model1:Model1Event ;"
            + "      rdfs:label \"Model1Class 1 Event\"^^xsd:string ;"
            + "      model1:hasTemporalEntity :i1-time ."
            + ""
            + ":i1-time a model1:TemporalInstant ;"
            + "      rdfs:label \"Model1Class 1 Time\"^^xsd:string ;"
            + "      model1:dateTime \"1994-02-07T21:47:01.000Z\"^^xsd:dateTime ."
            + "      "
            + ":i2   a       model1:Model1Class ;"
            + "      rdfs:label \"Model1Class 2\"^^xsd:string ;"
            + "      model1:name \"Model1Class 2\"^^xsd:string ;"
            + "      model1:hasModel1ClassAssoc :i2-assoc ;"
            + "      model1:model1ClassId \"ID02\"^^xsd:string ."
            + ""
            + ":i2-assoc a model1:Model1ClassAssoc ;"
            + "      rdfs:label \"Model1Class 2 Assoc\"^^xsd:string ;"
            + "      model1:hasEntity :i2-event ."
            + "      "
            + ":i2-event a model1:Model1Event ;"
            + "      rdfs:label \"Model1Class 2 Event\"^^xsd:string ;"
            + "      model1:hasTemporalEntity :i2-time ."
            + ""
            + ":i2-time a model1:TemporalInstant ;"
            + "      rdfs:label \"Model1Class 2 Time\"^^xsd:string ;"
            + "      model1:dateTime \"1995-11-06T05:15:01.000Z\"^^xsd:dateTime ."
            + "      "
            + ":i3   a       model1:Model1Class ;"
            + "      rdfs:label \"Model1Class 3\"^^xsd:string ;"
            + "      model1:name \"Model1Class 3\"^^xsd:string ;"
            + "      model1:hasModel1ClassAssoc :i3-assoc ;"
            + "      model1:model1ClassId \"ID03\"^^xsd:string ."
            + ""
            + ":i3-assoc a model1:Model1ClassAssoc ;"
            + "      rdfs:label \"Model1Class 3 Assoc\"^^xsd:string ;"
            + "      model1:hasEntity :i3-event ."
            + "      "
            + ":i3-event a model1:Model1Event ;"
            + "      rdfs:label \"Model1Class 3 Event\"^^xsd:string ;"
            + "      model1:hasTemporalEntity :i3-time ."
            + ""
            + ":i3-time a model1:TemporalInstant ;"
            + "      rdfs:label \"Model1Class 3 Time\"^^xsd:string ;"
            + "      model1:dateTime \"1999-04-30T16:30:00.000Z\"^^xsd:dateTime ."
            + "      "
            + ":i4   a       model1:Model1Class ;"
            + "      rdfs:label \"Model1Class 4\"^^xsd:string ;"
            + "      model1:name \"Model1Class 4\"^^xsd:string ;"
            + "      model1:hasModel1ClassAssoc :i4-assoc ;"
            + "      model1:model1ClassId \"ID04\"^^xsd:string ."
            + ""
            + ":i4-assoc a model1:Model1ClassAssoc ;"
            + "      rdfs:label \"Model1Class 4 Assoc\"^^xsd:string ;"
            + "      model1:hasEntity :i4-event ."
            + "      "
            + ":i4-event a model1:Model1Event ;"
            + "      rdfs:label \"Model1Class 4 Event\"^^xsd:string ;"
            + "      model1:hasTemporalEntity :i4-time ."
            + ""
            + ":i4-time a model1:TemporalInstant ;"
            + "      rdfs:label \"Model1Class 4 Time\"^^xsd:string ;"
            + "      model1:dateTime \"2001-02-27T21:20:00.000Z\"^^xsd:dateTime ."
            + "      "
            + ":i5   a       model1:Model1Class ;"
            + "      rdfs:label \"Model1Class 5\"^^xsd:string ;"
            + "      model1:name \"Model1Class 5\"^^xsd:string ;"
            + "      model1:hasModel1ClassAssoc :i5-assoc ;"
            + "      model1:model1ClassId \"ID05\"^^xsd:string ."
            + ""
            + ":i5-assoc a model1:Model1ClassAssoc ;"
            + "      rdfs:label \"Model1Class 5 Assoc\"^^xsd:string ;"
            + "      model1:hasEntity :i5-event ."
            + "      "
            + ":i5-event a model1:Model1Event ;"
            + "      rdfs:label \"Model1Class 5 Event\"^^xsd:string ;"
            + "      model1:hasTemporalEntity :i5-time ."
            + ""
            + ":i5-time a model1:TemporalInstant ;"
            + "      rdfs:label \"Model1Class 5 Time\"^^xsd:string ;"
            + "      model1:dateTime \"2002-01-16T00:30:00.000Z\"^^xsd:dateTime ."
            + "      "
            + ":i6   a       model1:Model1Class ;"
            + "      rdfs:label \"Model1Class 6\"^^xsd:string ;"
            + "      model1:name \"Model1Class 6\"^^xsd:string ;"
            + "      model1:hasModel1ClassAssoc :i6-assoc ;"
            + "      model1:model1ClassId \"ID06\"^^xsd:string ."
            + ""
            + ":i6-assoc a model1:Model1ClassAssoc ;"
            + "      rdfs:label \"Model1Class 6 Assoc\"^^xsd:string ;"
            + "      model1:hasEntity :i6-event ."
            + "      "
            + ":i6-event a model1:Model1Event ;"
            + "      rdfs:label \"Model1Class 6 Event\"^^xsd:string ;"
            + "      model1:hasTemporalEntity :i6-time ."
            + ""
            + ":i6-time a model1:TemporalInstant ;"
            + "      rdfs:label \"Model1Class 6 Time\"^^xsd:string ;"
            + "      model1:dateTime \"2003-04-08T13:43:00.000Z\"^^xsd:dateTime .";




   public static void main (String args []) throws RepositoryException, QueryEvaluationException, MalformedQueryException, TupleQueryResultHandlerException {
       try {
           setUp();
       } catch (Exception e) {
           e.printStackTrace();
       }
       testWithoutSubquery();
       try {
           tearDown();
       } catch (Exception e) {
           e.printStackTrace();
       }

   }
}
