import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;

public class GraphDB {
    public static void main(String[] args) {
        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/MyIMDb");
        RepositoryConnection connection = repository.getConnection();

        connection.clear();
        connection.begin();

        try {
            connection.add(GraphDB.class.getResourceAsStream("/rml/outputFile.ttl"), "urn:base",
                    RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.commit();

        String queryString1 = "PREFIX schema: <http://schema.org/>\n";
        queryString1 += "PREFIX ex: <http://example.com/>\n";
        queryString1 += "\n";
        queryString1 += "SELECT ?title\n";
        queryString1 += "WHERE {\n";
        queryString1 += "  {\n";
        queryString1 += "    SELECT ?ratingId (MAX(?totalRating) AS ?maxRating)\n";
        queryString1 += "    {\n";
        queryString1 += "      ?rating a schema:Movie ;\n";
        queryString1 += "        schema:total ?totalRating ;\n";
        queryString1 += "        schema:identifier ?ratingId .\n";
        queryString1 += "    }\n";
        queryString1 += "    GROUP BY ?ratingId\n";
        queryString1 += "    ORDER BY DESC(?maxRating)\n";
        queryString1 += "    LIMIT 1\n";
        queryString1 += "  }\n";
        queryString1 += "  ?movie schema:identifier ?ratingId ;\n";
        queryString1 += "    schema:primaryTitle ?title .\n";
        queryString1 += "}";

        String queryString2 = "PREFIX schema: <http://schema.org/>\n";
        queryString2 += "\n";
        queryString2 += "SELECT ?actorName (SUM(?averageRating) / COUNT(DISTINCT ?film) AS ?overallAverageRating)\n";
        queryString2 += "WHERE {\n";
        queryString2 += "  ?actor a schema:Actor ;\n";
        queryString2 += "         schema:name ?actorName ;\n";
        queryString2 += "         schema:knownForTitles ?film .\n";
        queryString2 += "  ?film schema:average ?averageRating .\n";
        queryString2 += "}\n";
        queryString2 += "GROUP BY ?actorName\n";
        queryString2 += "ORDER BY DESC(?overallAverageRating)\n";
        queryString2 += "LIMIT 1";

        String queryString3 = "PREFIX schema: <http://schema.org/>\n";
        queryString3 += "\n";
        queryString3 += "SELECT ?actorName (AVG(?duration) AS ?averageDuration)\n";
        queryString3 += "WHERE {\n";
        queryString3 += "  ?actor a schema:Actor ;\n";
        queryString3 += "         schema:name ?actorName ;\n";
        queryString3 += "         schema:knownForTitles ?film .\n";
        queryString3 += "  ?film a schema:Movie ;\n";
        queryString3 += "        schema:runtimeMinutes ?duration .\n";
        queryString3 += "}\n";
        queryString3 += "GROUP BY ?actorName\n";
        queryString3 += "LIMIT 1";

        String queryString4 =  "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?director ?movie ?title\n" +
                "WHERE {\n" +
                "  ?film a schema:Movie ;\n" +
                "        schema:primaryTitle ?title .\n" +
                "  SERVICE <https://dbpedia.org/sparql/> {\n" +
                "    ?movie dbpedia:director ?director ;\n" +
                "           rdfs:label ?title.\n" +
                "  }\n" +
                "}";
        
     // Execute Query 1
        TupleQuery query1 = connection.prepareTupleQuery(queryString1);
        try (TupleQueryResult result1 = query1.evaluate()) {
            System.out.println("Results for Query 1:");
            while (result1.hasNext()) {
                BindingSet solution = result1.next();
                System.out.println(solution);
            }
        }

        // Execute Query 2
        TupleQuery query2 = connection.prepareTupleQuery(queryString2);
        try (TupleQueryResult result2 = query2.evaluate()) {
            System.out.println("Results for Query 2:");
            while (result2.hasNext()) {
                BindingSet solution = result2.next();
                System.out.println(solution);
            }
        }
        
        // Execute Query 3
        TupleQuery query3 = connection.prepareTupleQuery(queryString3);
        try (TupleQueryResult result3 = query3.evaluate()) {
            System.out.println("Results for Query 3:");
            while (result3.hasNext()) {
                BindingSet solution = result3.next();
                System.out.println(solution);
            }
        }
        
        // Execute Query 4
        TupleQuery query4 = connection.prepareTupleQuery(queryString4);
        try (TupleQueryResult result4 = query4.evaluate()) {
            System.out.println("Results for Query 4:");
            while (result4.hasNext()) {
                BindingSet solution = result4.next();
                System.out.println(solution);
            }
        }
        
        connection.close();
        repository.shutDown();
      
    }
}
