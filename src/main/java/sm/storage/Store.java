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

import org.openrdf.model.Resource;
import org.openrdf.query.*;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.*;
import java.util.Set;

public class Store{
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
        conf.setTablePrefix("modelrya_");
        conf.setDisplayQueryPlan(true);
        dao.setConf(conf);
        dao.init();
        store = new RdfCloudTripleStore();
        store.setConf(conf);
        store.setRyaDAO(dao);
        inferenceEngine = new InferenceEngine();
        inferenceEngine.setRyaDAO(dao);
        inferenceEngine.refreshGraph();
        store.setInferenceEngine(inferenceEngine);
        store.initialize();
        repository = new RyaSailRepository(store);
        conn = repository.getConnection();
        load();

    }


    private static void load() throws RepositoryException, RDFParseException, IOException {
        conn = repository.getConnection();

        // T-Box
        String ttlString = MODEL_TTL;
    InputStream stringInput = new ByteArrayInputStream(ttlString.getBytes());
   conn.add(stringInput, "http://test/inference", RDFFormat.TURTLE);

        // A-Box
        final File file = new File("/home/admin/IdeaProjects/social-media-collectors/src/main/resources/rdf.rdf");

        try {
            conn.add(new FileInputStream(file), file.getName(),
                    RDFFormat.TURTLE, new Resource[]{});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        } catch (org.openrdf.repository.RepositoryException e) {
            e.printStackTrace();
        }


        conn.commit();
    }
    public static void testWithoutSubquery() throws RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, MalformedQueryException {
        TupleQuery  tupleQuery = null;

        final String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX model: <http://localhost/socialNetwork/model#>\n" +
                "SELECT *\n" +
                "{ \n" +
                "  ?s  rdf:type model:Content .\n" +
                "}";


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
        conn.close();
        repository.shutDown();
    }

    /**
     * The ontology t-box in turtle.
     */
    private static String MODEL_TTL = "@prefix :    <http://test/inference#>. " +
            "@prefix : <http://localhost/socialNetwork/model#> .\n" +
            "@prefix dc: <http://purl.org/dc/elements/1.1/> .\n" +
            "@prefix ns: <http://www.w3.org/2003/06/sw-vocab-status/ns#> .\n" +
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix wot: <http://xmlns.com/wot/0.1/> .\n" +
            "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix skos: <http://www.w3.org/2004/02/skos/core#> .\n" +
            "@prefix model: <http://localhost/socialNetwork/model#> .\n" +
            "@prefix terms: <http://purl.org/dc/terms/> .\n" +
            "@base <http://localhost/socialNetwork/model> .\n" +
            "\n" +
            "<http://localhost/socialNetwork/model> rdf:type owl:Ontology ;\n" +
            "                                        owl:imports <http://purl.org/marl/ns> ,\n" +
            "                                                    <http://www.w3.org/2006/time#2016> ,\n" +
            "                                                    foaf: .\n" +
            "\n" +
            "#################################################################\n" +
            "#    Object Properties\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#aReplyTo\n" +
            "model:aReplyTo rdf:type owl:ObjectProperty ;\n" +
            "               owl:inverseOf model:hasReply ;\n" +
            "               rdfs:domain model:Reply ;\n" +
            "               rdfs:range model:Content .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#actionOn\n" +
            "model:actionOn rdf:type owl:ObjectProperty ;\n" +
            "               owl:inverseOf model:hasAction ;\n" +
            "               rdfs:domain model:Activity ;\n" +
            "               rdfs:range model:Content .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#contains\n" +
            "model:contains rdf:type owl:ObjectProperty ;\n" +
            "               owl:inverseOf model:sourcedFrom ;\n" +
            "               rdfs:domain model:Source .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#createdAt\n" +
            "model:createdAt rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:domain model:Content ;\n" +
            "                rdfs:range <http://www.w3.org/2006/time#DateTimeDescription> .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#endsOn\n" +
            "model:endsOn rdf:type owl:ObjectProperty ;\n" +
            "             rdfs:domain model:Event ;\n" +
            "             rdfs:range <http://www.w3.org/2006/time#DateTimeDescription> .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#followedBy\n" +
            "model:followedBy rdf:type owl:ObjectProperty ;\n" +
            "                 owl:inverseOf model:follows ;\n" +
            "                 rdfs:domain foaf:Person ;\n" +
            "                 rdfs:range foaf:Person .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#follows\n" +
            "model:follows rdf:type owl:ObjectProperty ;\n" +
            "              rdfs:domain foaf:Person ;\n" +
            "              rdfs:range foaf:Person .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#friendOf\n" +
            "model:friendOf rdf:type owl:ObjectProperty ,\n" +
            "                        owl:SymmetricProperty ;\n" +
            "               rdfs:domain foaf:Person ;\n" +
            "               rdfs:range foaf:Person .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasAction\n" +
            "model:hasAction rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:domain model:Content ;\n" +
            "                rdfs:range model:Activity .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasActionMetrics\n" +
            "model:hasActionMetrics rdf:type owl:ObjectProperty ;\n" +
            "                       rdfs:domain model:Author ;\n" +
            "                       rdfs:range model:ActivityMetrics .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasBirthDay\n" +
            "model:hasBirthDay rdf:type owl:ObjectProperty ;\n" +
            "                  rdfs:domain foaf:Person ;\n" +
            "                  rdfs:range <http://www.w3.org/2006/time#MonthOfYear> .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasCategory\n" +
            "model:hasCategory rdf:type owl:ObjectProperty ;\n" +
            "                  rdfs:domain model:VideoProperties ;\n" +
            "                  rdfs:range model:Category .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasConnectivity\n" +
            "model:hasConnectivity rdf:type owl:ObjectProperty ;\n" +
            "                      rdfs:domain model:Author ;\n" +
            "                      rdfs:range model:ConnectivityMetrics .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasContentFeatures\n" +
            "model:hasContentFeatures rdf:type owl:ObjectProperty ;\n" +
            "                         rdfs:domain model:Type ;\n" +
            "                         rdfs:range model:ContentFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasDate\n" +
            "model:hasDate rdf:type owl:ObjectProperty ;\n" +
            "              rdfs:range model:Date .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasDuration\n" +
            "model:hasDuration rdf:type owl:ObjectProperty ;\n" +
            "                  rdfs:domain model:VideoProperties ;\n" +
            "                  rdfs:range <http://www.w3.org/2006/time#DurationDescription> .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasFeedBack\n" +
            "model:hasFeedBack rdf:type owl:ObjectProperty ;\n" +
            "                  rdfs:domain model:Content ;\n" +
            "                  rdfs:range model:FeedBack .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasGender\n" +
            "model:hasGender rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:domain foaf:Person ;\n" +
            "                rdfs:range model:Gender .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasMember\n" +
            "model:hasMember rdf:type owl:ObjectProperty ;\n" +
            "                owl:inverseOf model:isMemberOf ;\n" +
            "                rdfs:domain model:Group ;\n" +
            "                rdfs:range [ rdf:type owl:Class ;\n" +
            "                             owl:unionOf ( foaf:Organization\n" +
            "                                           foaf:Person\n" +
            "                                         )\n" +
            "                           ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasParticipant\n" +
            "model:hasParticipant rdf:type owl:ObjectProperty ;\n" +
            "                     owl:inverseOf model:participateOn ;\n" +
            "                     rdfs:domain model:Event ;\n" +
            "                     rdfs:range [ rdf:type owl:Class ;\n" +
            "                                  owl:unionOf ( foaf:Organization\n" +
            "                                                foaf:Person\n" +
            "                                              )\n" +
            "                                ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasProfileImage\n" +
            "model:hasProfileImage rdf:type owl:ObjectProperty ;\n" +
            "                      rdfs:domain model:Author ;\n" +
            "                      rdfs:range model:Image .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasProperties\n" +
            "model:hasProperties rdf:type owl:ObjectProperty ;\n" +
            "                    rdfs:domain model:Type ;\n" +
            "                    rdfs:range model:Properties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasReaction\n" +
            "model:hasReaction rdf:type owl:ObjectProperty ;\n" +
            "                  owl:inverseOf model:reactionTo ;\n" +
            "                  rdfs:domain model:Content ;\n" +
            "                  rdfs:range model:Reaction .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasReactionFrom\n" +
            "model:hasReactionFrom rdf:type owl:ObjectProperty ;\n" +
            "                      owl:inverseOf model:reactTo ;\n" +
            "                      rdfs:domain model:Content ;\n" +
            "                      rdfs:range model:Author ;\n" +
            "                      owl:propertyChainAxiom ( model:hasReaction\n" +
            "                                               model:reactedBy\n" +
            "                                             ) .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasReply\n" +
            "model:hasReply rdf:type owl:ObjectProperty ;\n" +
            "               rdfs:domain model:Content ;\n" +
            "               rdfs:range model:Reply .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasStatus\n" +
            "model:hasStatus rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:range model:Status .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasSubscription\n" +
            "model:hasSubscription rdf:type owl:ObjectProperty ;\n" +
            "                      owl:inverseOf model:subscribeOn .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasType\n" +
            "model:hasType rdf:type owl:ObjectProperty ;\n" +
            "              rdfs:range model:Type .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#isMemberOf\n" +
            "model:isMemberOf rdf:type owl:ObjectProperty ;\n" +
            "                 rdfs:range model:Group .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#made\n" +
            "model:made rdf:type owl:ObjectProperty ;\n" +
            "           owl:inverseOf model:madeBy ;\n" +
            "           rdfs:domain model:Author ;\n" +
            "           rdfs:range model:Activity .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#madeBy\n" +
            "model:madeBy rdf:type owl:ObjectProperty ;\n" +
            "             rdfs:domain model:Activity ;\n" +
            "             rdfs:range model:Author .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#participateOn\n" +
            "model:participateOn rdf:type owl:ObjectProperty ;\n" +
            "                    rdfs:domain [ rdf:type owl:Class ;\n" +
            "                                  owl:unionOf ( foaf:Organization\n" +
            "                                                foaf:Person\n" +
            "                                              )\n" +
            "                                ] ;\n" +
            "                    rdfs:range model:Event .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#publish\n" +
            "model:publish rdf:type owl:ObjectProperty ;\n" +
            "              owl:inverseOf model:publishedBy ;\n" +
            "              rdfs:domain model:Author ;\n" +
            "              rdfs:range model:Publication .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#publishedBy\n" +
            "model:publishedBy rdf:type owl:ObjectProperty ;\n" +
            "                  rdfs:domain model:Publication ;\n" +
            "                  rdfs:range model:Author .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#reactBy\n" +
            "model:reactBy rdf:type owl:ObjectProperty ;\n" +
            "              owl:inverseOf model:reactedBy ;\n" +
            "              rdfs:domain model:Author ;\n" +
            "              rdfs:range model:Reaction ;\n" +
            "              rdfs:comment \"the reaction made by a user to react to content\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#reactTo\n" +
            "model:reactTo rdf:type owl:ObjectProperty ;\n" +
            "              rdfs:domain model:Author ;\n" +
            "              rdfs:range model:Content ;\n" +
            "              owl:propertyChainAxiom ( model:reactBy\n" +
            "                                       model:hasReaction\n" +
            "                                     ) .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#reactedBy\n" +
            "model:reactedBy rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:domain model:Reaction ;\n" +
            "                rdfs:range model:Author .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#reactionTo\n" +
            "model:reactionTo rdf:type owl:ObjectProperty ;\n" +
            "                 rdfs:domain model:Reaction ;\n" +
            "                 rdfs:range model:Content .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#relatedTo\n" +
            "model:relatedTo rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:domain model:Publication ;\n" +
            "                rdfs:range model:Publication .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#repliedFrom\n" +
            "model:repliedFrom rdf:type owl:ObjectProperty ;\n" +
            "                  owl:inverseOf model:replyTo ;\n" +
            "                  rdfs:domain model:Content ;\n" +
            "                  rdfs:range model:Author ;\n" +
            "                  owl:propertyChainAxiom ( model:hasReply\n" +
            "                                           model:replyedBy\n" +
            "                                         ) .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#reply\n" +
            "model:reply rdf:type owl:ObjectProperty ;\n" +
            "            owl:inverseOf model:replyedBy ;\n" +
            "            rdfs:domain model:Author ;\n" +
            "            rdfs:range model:Reply .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#replyTo\n" +
            "model:replyTo rdf:type owl:ObjectProperty ;\n" +
            "              rdfs:domain model:Author ;\n" +
            "              rdfs:range model:Content ;\n" +
            "              owl:propertyChainAxiom ( model:reply\n" +
            "                                       model:aReplyTo\n" +
            "                                     ) .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#replyedBy\n" +
            "model:replyedBy rdf:type owl:ObjectProperty ;\n" +
            "                rdfs:range model:Author .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#sourcedFrom\n" +
            "model:sourcedFrom rdf:type owl:ObjectProperty ;\n" +
            "                  rdfs:range model:Source .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#startsOn\n" +
            "model:startsOn rdf:type owl:ObjectProperty ;\n" +
            "               rdfs:domain model:Event ;\n" +
            "               rdfs:range <http://www.w3.org/2006/time#DateTimeDescription> .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#subscribeOn\n" +
            "model:subscribeOn rdf:type owl:ObjectProperty .\n" +
            "\n" +
            "\n" +
            "###  http://purl.org/marl/ns#extractedFrom\n" +
            "<http://purl.org/marl/ns#extractedFrom> rdfs:range model:Text .\n" +
            "\n" +
            "\n" +
            "###  http://purl.org/marl/ns#hasOpinion\n" +
            "<http://purl.org/marl/ns#hasOpinion> rdfs:domain model:Text .\n" +
            "\n" +
            "\n" +
            "#################################################################\n" +
            "#    Data properties\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#about\n" +
            "model:about rdf:type owl:DatatypeProperty ;\n" +
            "            rdfs:comment \"describes the author metadata\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#activityMetrics\n" +
            "model:activityMetrics rdf:type owl:DatatypeProperty .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#connectivityProperty\n" +
            "model:connectivityProperty rdf:type owl:DatatypeProperty ;\n" +
            "                           rdfs:domain model:ConnectivityMetrics ;\n" +
            "                           rdfs:comment \"describes the metrics related to the author is connectivity\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#feedbackProperty\n" +
            "model:feedbackProperty rdf:type owl:DatatypeProperty ;\n" +
            "                       rdfs:domain model:FeedBack ;\n" +
            "                       rdfs:comment \"describes the number of feedBack generated by users to react on a publication\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#followersCount\n" +
            "model:followersCount rdf:type owl:DatatypeProperty ;\n" +
            "                     rdfs:subPropertyOf model:connectivityProperty ;\n" +
            "                     rdfs:domain model:ConnectivityMetrics ;\n" +
            "                     rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#friendsCount\n" +
            "model:friendsCount rdf:type owl:DatatypeProperty ;\n" +
            "                   rdfs:subPropertyOf model:connectivityProperty ;\n" +
            "                   rdfs:domain model:ConnectivityMetrics ;\n" +
            "                   rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasAge\n" +
            "model:hasAge rdf:type owl:DatatypeProperty ;\n" +
            "             rdfs:subPropertyOf model:about ;\n" +
            "             rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasColor\n" +
            "model:hasColor rdf:type owl:DatatypeProperty ;\n" +
            "               rdfs:subPropertyOf owl:topDataProperty ;\n" +
            "               rdfs:domain model:ImageFeatures ;\n" +
            "               rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasDescription\n" +
            "model:hasDescription rdf:type owl:DatatypeProperty ;\n" +
            "                     rdfs:subPropertyOf owl:topDataProperty ;\n" +
            "                     rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasFormat\n" +
            "model:hasFormat rdf:type owl:DatatypeProperty ;\n" +
            "                rdfs:subPropertyOf model:mediaProperties ;\n" +
            "                rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasHashTag\n" +
            "model:hasHashTag rdf:type owl:DatatypeProperty ;\n" +
            "                 rdfs:subPropertyOf model:textEmbededEntity ;\n" +
            "                 rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasHeight\n" +
            "model:hasHeight rdf:type owl:DatatypeProperty ;\n" +
            "                rdfs:subPropertyOf model:mediaProperties ;\n" +
            "                rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasId\n" +
            "model:hasId rdf:type owl:DatatypeProperty ,\n" +
            "                     owl:FunctionalProperty ;\n" +
            "            rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasInterest\n" +
            "model:hasInterest rdf:type owl:DatatypeProperty ;\n" +
            "                  rdfs:subPropertyOf model:about ;\n" +
            "                  rdfs:domain model:Author ;\n" +
            "                  rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasJob\n" +
            "model:hasJob rdf:type owl:DatatypeProperty ;\n" +
            "             rdfs:subPropertyOf model:about ;\n" +
            "             rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasLanguage\n" +
            "model:hasLanguage rdf:type owl:DatatypeProperty ;\n" +
            "                  rdfs:subPropertyOf model:textProperties ;\n" +
            "                  rdfs:domain model:TextProperties ;\n" +
            "                  rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasLastName\n" +
            "model:hasLastName rdf:type owl:DatatypeProperty ;\n" +
            "                  rdfs:subPropertyOf model:about ;\n" +
            "                  rdfs:domain model:Author ;\n" +
            "                  rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasLength\n" +
            "model:hasLength rdf:type owl:DatatypeProperty ;\n" +
            "                rdfs:subPropertyOf model:textProperties ;\n" +
            "                rdfs:domain model:TextProperties ;\n" +
            "                rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasLink\n" +
            "model:hasLink rdf:type owl:DatatypeProperty ,\n" +
            "                       owl:FunctionalProperty ;\n" +
            "              rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasLocation\n" +
            "model:hasLocation rdf:type owl:DatatypeProperty ;\n" +
            "                  rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasRates\n" +
            "model:hasRates rdf:type owl:DatatypeProperty ;\n" +
            "               rdfs:subPropertyOf model:feedbackProperty ;\n" +
            "               rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasScreenName\n" +
            "model:hasScreenName rdf:type owl:DatatypeProperty ;\n" +
            "                    rdfs:subPropertyOf model:about ;\n" +
            "                    rdfs:domain model:Author ;\n" +
            "                    rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasSize\n" +
            "model:hasSize rdf:type owl:DatatypeProperty ;\n" +
            "              rdfs:subPropertyOf model:mediaProperties ;\n" +
            "              rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasTags\n" +
            "model:hasTags rdf:type owl:DatatypeProperty ;\n" +
            "              rdfs:subPropertyOf model:mediaProperties ;\n" +
            "              rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasTitle\n" +
            "model:hasTitle rdf:type owl:DatatypeProperty ;\n" +
            "               rdfs:subPropertyOf model:videoProperties ;\n" +
            "               rdfs:domain model:VideoProperties ;\n" +
            "               rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasTopic\n" +
            "model:hasTopic rdf:type owl:DatatypeProperty ;\n" +
            "               rdfs:subPropertyOf model:textEmbededEntity ;\n" +
            "               rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#hasWidth\n" +
            "model:hasWidth rdf:type owl:DatatypeProperty ;\n" +
            "               rdfs:subPropertyOf model:mediaProperties ;\n" +
            "               rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#likesCount\n" +
            "model:likesCount rdf:type owl:DatatypeProperty ;\n" +
            "                 rdfs:subPropertyOf model:feedbackProperty ;\n" +
            "                 rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#mediaProperties\n" +
            "model:mediaProperties rdf:type owl:DatatypeProperty ;\n" +
            "                      rdfs:domain model:MediaProperties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#membersCount\n" +
            "model:membersCount rdf:type owl:DatatypeProperty ;\n" +
            "                   rdfs:subPropertyOf model:connectivityProperty ;\n" +
            "                   rdfs:domain model:ConnectivityMetrics ;\n" +
            "                   rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#publicationsCount\n" +
            "model:publicationsCount rdf:type owl:DatatypeProperty ;\n" +
            "                        rdfs:subPropertyOf model:activityMetrics ;\n" +
            "                        rdfs:domain model:ActivityMetrics ;\n" +
            "                        rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#replyCount\n" +
            "model:replyCount rdf:type owl:DatatypeProperty ;\n" +
            "                 rdfs:subPropertyOf model:feedbackProperty ;\n" +
            "                 rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#sharesCount\n" +
            "model:sharesCount rdf:type owl:DatatypeProperty ;\n" +
            "                  rdfs:subPropertyOf model:feedbackProperty ;\n" +
            "                  rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#subscriptionsCount\n" +
            "model:subscriptionsCount rdf:type owl:DatatypeProperty ;\n" +
            "                         rdfs:subPropertyOf model:connectivityProperty .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#textEmbededEntity\n" +
            "model:textEmbededEntity rdf:type owl:DatatypeProperty ;\n" +
            "                        rdfs:domain model:TextFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#textProperties\n" +
            "model:textProperties rdf:type owl:DatatypeProperty ;\n" +
            "                     rdfs:domain model:TextProperties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#url\n" +
            "model:url rdf:type owl:DatatypeProperty ;\n" +
            "          rdfs:subPropertyOf model:textEmbededEntity ;\n" +
            "          rdfs:range xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#videoProperties\n" +
            "model:videoProperties rdf:type owl:DatatypeProperty ;\n" +
            "                      rdfs:domain model:VideoProperties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#viewsCount\n" +
            "model:viewsCount rdf:type owl:DatatypeProperty ;\n" +
            "                 rdfs:subPropertyOf model:feedbackProperty ;\n" +
            "                 rdfs:range xsd:int .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/accountName\n" +
            "foaf:accountName rdfs:subPropertyOf model:about .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/familyName\n" +
            "foaf:familyName rdfs:subPropertyOf model:about .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/family_name\n" +
            "foaf:family_name rdfs:subPropertyOf model:about .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/firstName\n" +
            "foaf:firstName rdfs:subPropertyOf model:about .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/lastName\n" +
            "foaf:lastName rdfs:subPropertyOf model:about .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/surname\n" +
            "foaf:surname rdfs:subPropertyOf model:about .\n" +
            "\n" +
            "\n" +
            "#################################################################\n" +
            "#    Classes\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Activity\n" +
            "model:Activity rdf:type owl:Class ;\n" +
            "               owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                     owl:oneOf ( model:Publish\n" +
            "                                                 model:Reply\n" +
            "                                                 model:Share\n" +
            "                                               )\n" +
            "                                   ] ;\n" +
            "               rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
            "                                 owl:onProperty model:hasDate ;\n" +
            "                                 owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                                 owl:onClass model:Date\n" +
            "                               ] ;\n" +
            "               rdfs:comment \"actions made by online social media users\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#ActivityMetrics\n" +
            "model:ActivityMetrics rdf:type owl:Class ;\n" +
            "                      rdfs:subClassOf model:AuthorPopularityMetrics .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Author\n" +
            "model:Author rdf:type owl:Class ;\n" +
            "             rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
            "                               owl:onProperty model:sourcedFrom ;\n" +
            "                               owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                               owl:onClass model:Source\n" +
            "                             ] ,\n" +
            "                             [ rdf:type owl:Restriction ;\n" +
            "                               owl:onProperty model:hasDescription ;\n" +
            "                               owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                             ] ,\n" +
            "                             [ rdf:type owl:Restriction ;\n" +
            "                               owl:onProperty model:hasId ;\n" +
            "                               owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                             ] ,\n" +
            "                             [ rdf:type owl:Restriction ;\n" +
            "                               owl:onProperty model:hasLink ;\n" +
            "                               owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                             ] ;\n" +
            "             owl:hasKey ( model:hasId\n" +
            "                        ) ,\n" +
            "                        ( model:hasLink\n" +
            "                        ) ;\n" +
            "             rdfs:comment \"the online social media users who publish the content in different social media websites\"^^xsd:string ;\n" +
            "             rdfs:label \"User\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#AuthorPopularityMetrics\n" +
            "model:AuthorPopularityMetrics rdf:type owl:Class ;\n" +
            "                              rdfs:subClassOf model:ContextualFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Category\n" +
            "model:Category rdf:type owl:Class ;\n" +
            "               owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                     owl:oneOf ( model:Animation\n" +
            "                                                 model:Documentary\n" +
            "                                                 model:Education\n" +
            "                                                 model:Movie\n" +
            "                                                 model:Music\n" +
            "                                                 model:Sport\n" +
            "                                               )\n" +
            "                                   ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#ConnectivityMetrics\n" +
            "model:ConnectivityMetrics rdf:type owl:Class ;\n" +
            "                          rdfs:subClassOf model:AuthorPopularityMetrics .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Content\n" +
            "model:Content rdf:type owl:Class ;\n" +
            "              rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
            "                                owl:onProperty model:hasFeedBack ;\n" +
            "                                owl:allValuesFrom model:FeedBack\n" +
            "                              ] ,\n" +
            "                              [ rdf:type owl:Restriction ;\n" +
            "                                owl:onProperty model:hasType ;\n" +
            "                                owl:minQualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                                owl:onClass model:Type\n" +
            "                              ] ,\n" +
            "                              [ rdf:type owl:Restriction ;\n" +
            "                                owl:onProperty model:hasDate ;\n" +
            "                                owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                                owl:onClass model:Date\n" +
            "                              ] ,\n" +
            "                              [ rdf:type owl:Restriction ;\n" +
            "                                owl:onProperty model:sourcedFrom ;\n" +
            "                                owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                                owl:onClass model:Source\n" +
            "                              ] ,\n" +
            "                              [ rdf:type owl:Restriction ;\n" +
            "                                owl:onProperty model:hasId ;\n" +
            "                                owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                              ] ,\n" +
            "                              [ rdf:type owl:Restriction ;\n" +
            "                                owl:onProperty model:hasLink ;\n" +
            "                                owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                              ] ;\n" +
            "              owl:hasKey ( model:hasId\n" +
            "                         ) ,\n" +
            "                         ( model:hasLink\n" +
            "                         ) ;\n" +
            "              rdfs:comment \"reperesent the content generated by online social media users such as comments and publications\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#ContentFeatures\n" +
            "model:ContentFeatures rdf:type owl:Class ;\n" +
            "                      rdfs:subClassOf model:PopularityMetrics .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#ContextualFeatures\n" +
            "model:ContextualFeatures rdf:type owl:Class ;\n" +
            "                         rdfs:subClassOf model:PopularityMetrics .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Date\n" +
            "model:Date rdf:type owl:Class ;\n" +
            "           rdfs:comment \"describes the date of the creation of an online profile or the date of the generation of a content\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Event\n" +
            "model:Event rdf:type owl:Class .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#FeedBack\n" +
            "model:FeedBack rdf:type owl:Class ;\n" +
            "               rdfs:subClassOf model:ContextualFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Gender\n" +
            "model:Gender rdf:type owl:Class ;\n" +
            "             owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                   owl:oneOf ( model:Female\n" +
            "                                               model:Mal\n" +
            "                                             )\n" +
            "                                 ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Group\n" +
            "model:Group rdf:type owl:Class ;\n" +
            "            rdfs:subClassOf model:Author ,\n" +
            "                            [ rdf:type owl:Restriction ;\n" +
            "                              owl:onProperty model:participateOn ;\n" +
            "                              owl:allValuesFrom model:Event\n" +
            "                            ] ;\n" +
            "            rdfs:comment \"a set of online social media users share the same interest\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Image\n" +
            "model:Image rdf:type owl:Class ;\n" +
            "            rdfs:subClassOf model:Media ;\n" +
            "            rdfs:comment \"Image\"^^xsd:string ;\n" +
            "            rdfs:label \"Image\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#ImageFeatures\n" +
            "model:ImageFeatures rdf:type owl:Class ;\n" +
            "                    rdfs:subClassOf model:ContentFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#ImageProperties\n" +
            "model:ImageProperties rdf:type owl:Class ;\n" +
            "                      rdfs:subClassOf model:MediaProperties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Media\n" +
            "model:Media rdf:type owl:Class ;\n" +
            "            rdfs:subClassOf model:Type ;\n" +
            "            rdfs:comment \"describes the user generated media such as Image and video\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#MediaProperties\n" +
            "model:MediaProperties rdf:type owl:Class ;\n" +
            "                      rdfs:subClassOf model:Properties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#MediaSharing\n" +
            "model:MediaSharing rdf:type owl:Class ;\n" +
            "                   owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                         owl:oneOf ( model:YouTube\n" +
            "                                                   )\n" +
            "                                       ] ;\n" +
            "                   rdfs:subClassOf model:Source ;\n" +
            "                   rdfs:comment \"describes the set of online social media used to publish media data such as YouTube, Instagram, etc.\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#MicroBlogging\n" +
            "model:MicroBlogging rdf:type owl:Class ;\n" +
            "                    owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                          owl:oneOf ( model:Twitter\n" +
            "                                                    )\n" +
            "                                        ] ;\n" +
            "                    rdfs:subClassOf model:Source ;\n" +
            "                    rdfs:comment \"describes the set of microblug online social media  such as Twitter\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#PopularityMetrics\n" +
            "model:PopularityMetrics rdf:type owl:Class ;\n" +
            "                        rdfs:comment \"defines the set of metrics used to analyse and predict the popularity of online genertade content such as videos and photos\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Properties\n" +
            "model:Properties rdf:type owl:Class ;\n" +
            "                 rdfs:subClassOf model:ContextualFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Publication\n" +
            "model:Publication rdf:type owl:Class ;\n" +
            "                  rdfs:subClassOf model:Content ,\n" +
            "                                  [ rdf:type owl:Restriction ;\n" +
            "                                    owl:onProperty model:publishedBy ;\n" +
            "                                    owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                                    owl:onClass model:Author\n" +
            "                                  ] ;\n" +
            "                  rdfs:comment \"describes the content generated by users such as posts, tweets and youtube videos.\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Reaction\n" +
            "model:Reaction rdf:type owl:Class ;\n" +
            "               owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                     owl:oneOf ( model:Dislike\n" +
            "                                                 model:Favorite\n" +
            "                                                 model:Like\n" +
            "                                                 model:Rate\n" +
            "                                               )\n" +
            "                                   ] ;\n" +
            "               rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
            "                                 owl:onProperty model:hasDate ;\n" +
            "                                 owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                                 owl:onClass model:Date\n" +
            "                               ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Reply\n" +
            "model:Reply rdf:type owl:Class ;\n" +
            "            rdfs:subClassOf model:Content ,\n" +
            "                            [ rdf:type owl:Restriction ;\n" +
            "                              owl:onProperty model:replyedBy ;\n" +
            "                              owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                              owl:onClass model:Author\n" +
            "                            ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Single\n" +
            "model:Single rdf:type owl:Class ;\n" +
            "             rdfs:subClassOf model:Author .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#SocialNetwork\n" +
            "model:SocialNetwork rdf:type owl:Class ;\n" +
            "                    owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                          owl:oneOf ( model:Facebook\n" +
            "                                                    )\n" +
            "                                        ] ;\n" +
            "                    rdfs:subClassOf model:Source ;\n" +
            "                    rdfs:comment \"describes the set of online social media used in social purpose such as Facebook, MySpace, etc.\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Source\n" +
            "model:Source rdf:type owl:Class ;\n" +
            "             rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
            "                               owl:onProperty model:hasLink ;\n" +
            "                               owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                             ] ;\n" +
            "             owl:hasKey ( model:hasLink\n" +
            "                        ) ;\n" +
            "             rdfs:comment \"Describes the online social media website from which sourced the content or the author profile\"^^xsd:string ;\n" +
            "             rdfs:label \"Online Social Media\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Status\n" +
            "model:Status rdf:type owl:Class ;\n" +
            "             owl:equivalentClass [ rdf:type owl:Class ;\n" +
            "                                   owl:oneOf ( model:Married\n" +
            "                                               model:Single\n" +
            "                                               model:Widow\n" +
            "                                             )\n" +
            "                                 ] .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Text\n" +
            "model:Text rdf:type owl:Class ;\n" +
            "           rdfs:subClassOf model:Type ,\n" +
            "                           [ rdf:type owl:Restriction ;\n" +
            "                             owl:onProperty model:hasContentFeatures ;\n" +
            "                             owl:allValuesFrom model:TextFeatures\n" +
            "                           ] ,\n" +
            "                           [ rdf:type owl:Restriction ;\n" +
            "                             owl:onProperty model:hasProperties ;\n" +
            "                             owl:allValuesFrom model:TextProperties\n" +
            "                           ] ;\n" +
            "           rdfs:comment \"text can designes a full text publication or textula publication linked to media data\"^^xsd:string ;\n" +
            "           rdfs:label \"Text\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#TextFeatures\n" +
            "model:TextFeatures rdf:type owl:Class ;\n" +
            "                   rdfs:subClassOf model:ContentFeatures ;\n" +
            "                   rdfs:comment \"describes the content of a textual description\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#TextProperties\n" +
            "model:TextProperties rdf:type owl:Class ;\n" +
            "                     rdfs:subClassOf model:Properties .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Type\n" +
            "model:Type rdf:type owl:Class ;\n" +
            "           rdfs:comment \"describes the types of content that can be generated by online social media users such as videos, text and Image\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Video\n" +
            "model:Video rdf:type owl:Class ;\n" +
            "            rdfs:subClassOf model:Media ;\n" +
            "            rdfs:comment \"video\"^^xsd:string ;\n" +
            "            rdfs:label \"Video\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#VideoFeatures\n" +
            "model:VideoFeatures rdf:type owl:Class ;\n" +
            "                    rdfs:subClassOf model:ContentFeatures .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#VideoProperties\n" +
            "model:VideoProperties rdf:type owl:Class ;\n" +
            "                      rdfs:subClassOf model:MediaProperties ,\n" +
            "                                      [ rdf:type owl:Restriction ;\n" +
            "                                        owl:onProperty model:hasDescription ;\n" +
            "                                        owl:cardinality \"1\"^^xsd:nonNegativeInteger\n" +
            "                                      ] .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/Organization\n" +
            "foaf:Organization rdfs:subClassOf model:Single .\n" +
            "\n" +
            "\n" +
            "###  http://xmlns.com/foaf/0.1/Person\n" +
            "foaf:Person rdf:type owl:Class ;\n" +
            "            rdfs:subClassOf model:Single ,\n" +
            "                            [ rdf:type owl:Restriction ;\n" +
            "                              owl:onProperty model:hasGender ;\n" +
            "                              owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                              owl:onClass model:Gender\n" +
            "                            ] ,\n" +
            "                            [ rdf:type owl:Restriction ;\n" +
            "                              owl:onProperty model:hasStatus ;\n" +
            "                              owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" +
            "                              owl:onClass model:Status\n" +
            "                            ] .\n" +
            "\n" +
            "\n" +
            "#################################################################\n" +
            "#    Individuals\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Animation\n" +
            "model:Animation rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Dislike\n" +
            "model:Dislike rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Documentary\n" +
            "model:Documentary rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Education\n" +
            "model:Education rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Facebook\n" +
            "model:Facebook rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Favorite\n" +
            "model:Favorite rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Female\n" +
            "model:Female rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Like\n" +
            "model:Like rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Mal\n" +
            "model:Mal rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Married\n" +
            "model:Married rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Movie\n" +
            "model:Movie rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Music\n" +
            "model:Music rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#News\n" +
            "model:News rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Publish\n" +
            "model:Publish rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Rate\n" +
            "model:Rate rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Reply\n" +
            "model:Reply rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Share\n" +
            "model:Share rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Single\n" +
            "model:Single rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Sport\n" +
            "model:Sport rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Twitter\n" +
            "model:Twitter rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#Widow\n" +
            "model:Widow rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://localhost/socialNetwork/model#YouTube\n" +
            "model:YouTube rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "#################################################################\n" +
            "#    Annotations\n" +
            "#################################################################\n" +
            "\n" +
            "model:Reply rdfs:comment \"describes the comments generated by users to reply to publication such as comments on youtube videos\"^^xsd:string .\n" +
            "\n" +
            "\n" +
            "###  Generated by the OWL API (version 4.2.8.20170104-2310) https://github.com/owlcs/owlapi\n" ;





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
