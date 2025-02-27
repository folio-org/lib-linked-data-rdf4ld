package org.folio.opt4;

import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.opt4.mapper.TopMapper;
import org.folio.opt4.util.MonographUtil;

public class ExportRunner {

    public static void main(String[] args) {
        var instance = MonographUtil.getSampleInstanceResource();

        var modelBuilder = new ModelBuilder();

        new TopMapper().mapToBibframe(instance, modelBuilder);

        Rio.write(modelBuilder.build(), System.out, RDFFormat.JSONLD);
    }

}