package sm.storage;


import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.accumulo.AccumuloRyaDAO;
import org.apache.rya.prospector.service.ProspectorServiceEvalStatsDAO;
import org.apache.rya.rdftriplestore.RdfCloudTripleStore;
import org.apache.rya.rdftriplestore.RyaSailRepository;

import org.apache.rya.rdftriplestore.inference.InferenceEngine;
import org.apache.rya.rdftriplestore.inference.InferenceEngineException;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.*;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryConnection;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Storage {
    public static void main  (String []args) throws RepositoryException, InferenceEngineException {
    final RdfCloudTripleStore store = new RdfCloudTripleStore();
    AccumuloRdfConfiguration conf = new AccumuloRdfConfiguration();

        AccumuloRyaDAO dao = new AccumuloRyaDAO();
        Connector connector = null;
        try {
            connector = new ZooKeeperInstance("myInstance", "masternode:2181").getConnector("root", "1234");

        } catch (AccumuloException e) {
            e.printStackTrace();
        } catch (AccumuloSecurityException e) {
            e.printStackTrace();
        }
        AccumuloRyaDAO crdfdao = new AccumuloRyaDAO();
        crdfdao.setConnector(connector);

        dao.setConnector(connector);
conf.setTablePrefix("testrya_");
dao.setConf(conf);
store.setRyaDAO(dao);
//store.setRdfDao(dao);
        ProspectorServiceEvalStatsDAO evalDao = null;
        try {
            evalDao = new ProspectorServiceEvalStatsDAO(connector, conf);
        } catch (AccumuloException e) {
            e.printStackTrace();
        } catch (AccumuloSecurityException e) {
            e.printStackTrace();
        }
        evalDao.init();
        store.setRdfEvalStatsDAO(evalDao);

        InferenceEngine inferenceEngine = new InferenceEngine();
        inferenceEngine.setRyaDAO(crdfdao);
        inferenceEngine.setConf(conf);
        store.setInferenceEngine(inferenceEngine);
//        inferenceEngine.refreshGraph();


    Repository myRepository = new RyaSailRepository(store);

        try {
            myRepository.initialize();
        } catch (org.openrdf.repository.RepositoryException e) {
            e.printStackTrace();
        }

        RepositoryConnection conn = null;

        try {
            conn = myRepository.getConnection();
        } catch (org.openrdf.repository.RepositoryException e) {
            e.printStackTrace();
        }


        //load data from file
 /*final File file = new File("/home/admin/IdeaProjects/social-media-collectors/src/main/resources/store.rdf");

                try {
                    conn.add(new FileInputStream(file), file.getName(),
                            RDFFormat.TURTLE, new Resource[]{});
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RDFParseException e) {
                    e.printStackTrace();
                } catch (org.openrdf.repository.RepositoryException e) {
                    e.printStackTrace();
                }*/


        // conn.add(new FileInputStream(file), file.getName(),
                //RDFFormat.NTRIPLES, new Resource[]{});
    /*    ValueFactory vf = new ValueFactoryImpl();
        String litdupsNS="urn:test:litdups#";
        conn.add(vf.createStatement(vf.createURI(litdupsNS, "UndergraduateStudent"), RDFS.SUBCLASSOF, vf.createURI(litdupsNS, "Student")));
        conn.add(vf.createStatement(vf.createURI(litdupsNS, "Student"), RDFS.SUBCLASSOF, vf.createURI(litdupsNS, "Person")));
        conn.add(vf.createStatement(vf.createURI(litdupsNS, "UgradA"), RDF.TYPE, vf.createURI(litdupsNS, "UndergraduateStudent")));
        conn.add(vf.createStatement(vf.createURI(litdupsNS, "StudentB"), RDF.TYPE, vf.createURI(litdupsNS, "Student")));
        conn.add(vf.createStatement(vf.createURI(litdupsNS, "PersonC"), RDF.TYPE, vf.createURI(litdupsNS, "Person")));

        try{
            conn.commit();
        } catch (org.openrdf.repository.RepositoryException e) {
            e.printStackTrace();
        }*/
   /*String query="PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ex: <http://example.org/> \n" +
                "PREFIX zoo: <http://example.org/zoo/>  \n" +
             "select ?x where {?type rdfs:subClassOf ex:animal." +
           "?x rdf:type ?type.}";*/
     final String query="PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX lit: <urn:test:litdups#>\n" +
                "select * where {?s rdf:type lit:Person.}";



        TupleQuery tupleQuery = null;
        try {
            tupleQuery = conn.prepareTupleQuery(
                    QueryLanguage.SPARQL, query);

        } catch (RepositoryException e) {
            e.printStackTrace();
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



        try {
            conn.close();
        } catch (org.openrdf.repository.RepositoryException e) {
            e.printStackTrace();
        }


            try {
                myRepository.shutDown();
            } catch (org.openrdf.repository.RepositoryException e) {
                e.printStackTrace();
            }

    }}
