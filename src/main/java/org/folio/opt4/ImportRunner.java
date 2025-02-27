package org.folio.opt4;

import java.io.IOException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.opt4.mapper.TopMapper;

public class ImportRunner {
    private static final String FILENAME = "LoC_RDF.json";

    public static void main(String[] args) throws IOException {
        var input = ImportRunner.class.getResourceAsStream("/" + FILENAME);
        var model = Rio.parse(input, "", RDFFormat.JSONLD);

        var result = new TopMapper().mapToLd(model);

        result.forEach(r -> {
            System.out.println("Resource with types " + r.getTypes() + "\ndoc: " + r.getDoc().toPrettyString());
            r.getOutgoingEdges().forEach(oe ->
                    System.out.println("Outgoing edge: " + oe.getTarget().getTypes() + " under predicate "
                            + oe.getPredicate().getUri() + "\ndoc: " + oe.getTarget().getDoc().toPrettyString()));
            r.getIncomingEdges().forEach(ie ->
                    System.out.println("Incoming edge: " + ie.getSource().getTypes() + " under predicate "
                            + ie.getPredicate().getUri() + "\ndoc: " + ie.getSource().getDoc().toPrettyString()));
        });
    }
}