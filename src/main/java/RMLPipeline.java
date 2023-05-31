import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RMLPipeline {

    /**
     * Using the RMLMapper together with the graphdb: perform a mapping (airports) and load the results in graphdb
     */
    public static void main(String[] args) throws IOException {
        RMLPipeline pipeline = new RMLPipeline();

        String rootFolder = "./src/main/resources/rml/";
        String outputFile = "/outputFile.ttl";

        String[] mappingFiles = {"actors.ttl", "movies.ttl", "ratings.ttl"};

        String outPath = rootFolder + outputFile;
        Writer output = new FileWriter(outPath);

        for (String mappingFile : mappingFiles) {
            String mapPath = rootFolder + mappingFile;
            File mappingFileObj = new File(mapPath);
            pipeline.runRMLMapper(mappingFileObj, output);
        }
        

        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/MyIMDb");
        RepositoryConnection connection = repository.getConnection();

        connection.clear();
        connection.begin();
        
        try {
            connection.add(RMLPipeline.class.getResourceAsStream("/rml/moviesOntology.owl"),
                    "urn:base",
                    RDFFormat.TURTLE);
            connection.add(GraphDB.class.getResourceAsStream("/rml/outputFile.ttl"),
                    "urn:base",
                    RDFFormat.TURTLE);
            System.out.println(3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        connection.commit();
        repository.shutDown();

    }


    void runRMLMapper(File mappingFile, Writer outputFile) {
        try {

        	InputStream mappingStream = new FileInputStream(mappingFile);
            QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

            RecordsFactory factory = new RecordsFactory(mappingFile.getParent());
            Map<String, Class> libraryMap = new HashMap<>();
            libraryMap.put("IDLabFunctions", IDLabFunctions.class);

            FunctionLoader functionLoader = new FunctionLoader(null, libraryMap);

            QuadStore outputStore = new RDF4JStore();
            Executor executor = new Executor(rmlStore, factory, functionLoader, outputStore, Utils.getBaseDirectiveTurtle(mappingStream));
            QuadStore result = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));

            result.write(outputFile, "turtle");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
