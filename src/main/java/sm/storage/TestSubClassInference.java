package sm.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.accumulo.AccumuloRyaDAO;
import org.apache.rya.api.RdfCloudTripleStoreConstants;
import org.apache.rya.rdftriplestore.RdfCloudTripleStore;

import org.apache.rya.rdftriplestore.inference.InferenceEngine;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;

public class TestSubClassInference {
    private final static String LUBM = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";

    private static Connector connector;
    private static AccumuloRyaDAO dao;
    private final static ValueFactory vf = new ValueFactoryImpl();
    private static AccumuloRdfConfiguration conf;
    private static RdfCloudTripleStore store;
    private static InferenceEngine inferenceEngine;
    private static SailRepository repository;
    private static SailRepositoryConnection conn;
    private static TupleQueryResultHandler resultHandler;
    private static List<BindingSet> solutions;


    public static void setUp() throws Exception {
      //  super.setUp();
        dao = new AccumuloRyaDAO();
        //connector = new MockInstance().getConnector("myInstance", new PasswordToken("1234"));
        connector = new ZooKeeperInstance("myInstance", "masternode:2181").getConnector("root", "1234");
        dao.setConnector(connector);
        conf = new AccumuloRdfConfiguration();
        conf.setInfer(true);
        dao.setConf(conf);
        dao.init();
        store = new RdfCloudTripleStore();
        store.setConf(conf);
        store.setRyaDAO(dao);
        inferenceEngine = new InferenceEngine();
        inferenceEngine.setRyaDAO(dao);
        store.setInferenceEngine(inferenceEngine);
        inferenceEngine.refreshGraph();
        store.initialize();
        repository = new SailRepository(store);
        conn = repository.getConnection();
        solutions = new LinkedList<>();
        resultHandler = new TupleQueryResultHandler() {
            @Override
            public void endQueryResult() throws TupleQueryResultHandlerException { }
            @Override
            public void handleBoolean(final boolean value) throws QueryResultHandlerException { }
            @Override
            public void handleLinks(final List<String> linkUrls) throws QueryResultHandlerException { }
            @Override
            public void handleSolution(final BindingSet bindingSet) throws TupleQueryResultHandlerException {
                if (bindingSet != null && bindingSet.iterator().hasNext()) {
                    solutions.add(bindingSet);
                }
            }
            @Override
            public void startQueryResult(final List<String> bindingNames) throws TupleQueryResultHandlerException {
                solutions.clear();
            }
        };
    }


    public static void tearDown() throws Exception {
        conn.close();
        repository.shutDown();
        store.shutDown();
        dao.purge(conf);
        dao.destroy();
    }
    public static void testSubClassInferenceQuery() throws Exception {
        final String ontology = "INSERT DATA { GRAPH <http://updated/test> {\n"
                + "  <urn:Agent> owl:equivalentClass <http://dbpedia.org/ontology/Agent> . \n"
                + "  <urn:Person> rdfs:subClassOf <urn:Agent> . \n"
                + "  [ owl:equivalentClass <http://schema.org/Person> ] rdfs:subClassOf <http://dbpedia.org/ontology/Agent> . \n"
                + "  <" + FOAF.PERSON.stringValue() + "> owl:equivalentClass <http://dbpedia.org/ontology/Person> . \n"
                + "  <" + FOAF.PERSON.stringValue() + "> owl:equivalentClass <urn:Person> . \n"
                + "  <http://dbpedia.org/ontology/Engineer> rdfs:subClassOf <http://dbpedia.org/ontology/Person> . \n"
                + "  <http://dbpedia.org/ontology/Engineer> rdfs:subClassOf <http://example.org/Person> . \n"
                + "  <http://dbpedia.org/ontology/Engineer> owl:equivalentClass <http://www.wikidata.org/entity/Q81096> . \n"
                + "}}";
        final String instances = "INSERT DATA { GRAPH <http://updated/test> {\n"
                + "  <urn:Alice> a <http://schema.org/Person> . \n"
                + "  <urn:Bob> a <http://www.wikidata.org/entity/Q81096> . \n"
                + "  <urn:Carol> a <http://example.org/Person> . \n"
                + "  <urn:Dan> a <http://example.org/Engineer> . \n"
                + "  <urn:Eve> a <urn:Agent> . \n"
                + "}}";
        final String query = "SELECT ?x { GRAPH <http://updated/test> { ?x a <urn:Agent> } } \n";
        conn.prepareUpdate(QueryLanguage.SPARQL, ontology).execute();
        inferenceEngine.refreshGraph();
        conn.prepareUpdate(QueryLanguage.SPARQL, instances).execute();
        conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(resultHandler);
        System.out.println ("resultHandler"+resultHandler.toString());
       // resultHandler = new SPARQLResultsXMLWriter(System.out);
        final Set<Value> expected = new HashSet<>();
        expected.add(vf.createURI("urn:Alice"));
        expected.add(vf.createURI("urn:Bob"));
        expected.add(vf.createURI("urn:Eve"));
        final Set<Value> returned = new HashSet<>();
        for (final BindingSet bs : solutions) {
            returned.add(bs.getBinding("x").getValue());
        }

    }
    public static  void main (String []args) throws Exception {
        setUp();
        testSubClassInferenceQuery();
        tearDown();
    }

}
