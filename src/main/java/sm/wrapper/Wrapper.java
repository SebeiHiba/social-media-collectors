package sm.wrapper;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;

import org.eclipse.rdf4j.rio.Rio;
import twitter4j.JSONException;
import twitter4j.JSONObject;


public  class Wrapper {


    public void mapp(String record, String topic) throws JSONException {
        System.out.println("****record = "+record);
        System.out.println("****topic = "+topic);
        InputStream in = null;
        JSONObject JSON_complete = null;
        FileWriter file = null;
        // RmlMapper mapper=mapperConfiguration ();

        try {
            // InputStream targetStream = new ByteArrayInputStream(record.value().getBytes());
            in = org.apache.commons.io.IOUtils.toInputStream(record, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapp(in,topic);

    }


    public void mapp (InputStream inputStream, String mappingFile) {

        System.out.println("inputStream " + inputStream);
        Path basePath = Paths.get("/home/admin/IdeaProjects/social-media-collectors/src/main/resources");
        RmlMapper mapper =
                RmlMapper
                        .newBuilder()
                        .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
                        .addFunctions(new RMLFunctions())
                        .build();
        mapper.bindInputStream("inputStream", inputStream);

        // Get mapping file from same folder
        Set<TriplesMap> mapping = RmlMappingLoader.build().load(
                Paths.get("/home/admin/IdeaProjects/social-media-collectors/src/main/resources/" + mappingFile + "Mapping.ttl"),
                RDFFormat.TURTLE);


        // Execute mapping
        Model result = mapper.map(mapping);
        // Print model
        result.forEach(System.out::println);
        //copy model into file
        FileOutputStream file = copyModel(result);
        //store File in rya store
    }

    public FileOutputStream copyModel (Model model ) 	{

        if (model==null) {
            System.out.println("Null or empty Statement model");
        }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(
                        "/home/admin/IdeaProjects/social-media-collectors/src/main/resources/output.ttl");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                Rio.write(model, out, RDFFormat.TURTLE);

                // Rio.write(result, out, RDFFormat.TRIG);

            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return out;
        }
    public Model mappFile (InputStream inputStream, String mappingFile) {

        System.out.println("inputStream " + inputStream);
        Path basePath = Paths.get("/home/admin/IdeaProjects/social-media-collectors/src/main/resources");
        RmlMapper mapper =
                RmlMapper
                        .newBuilder()
                        .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
                        .addFunctions(new RMLFunctions())
                        .build();
        mapper.bindInputStream("inputStream", inputStream);

        // Get mapping file from same folder
        Set<TriplesMap> mapping = RmlMappingLoader.build().load(
                Paths.get("/home/admin/IdeaProjects/social-media-collectors/src/main/resources/" + mappingFile + "Mapping.ttl"),
                RDFFormat.TURTLE);


        // Execute mapping
        Model result = mapper.map(mapping);
        // Print model
        result.forEach(System.out::println);
      return result;
    }
}


