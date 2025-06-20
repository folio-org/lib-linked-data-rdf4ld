package org.folio.rdf4ld.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceMapping;

public interface Rdf4LdService {

  Set<Resource> mapRdfToLd(InputStream inputStream, String contentType, ResourceMapping resourceMapping);

  Set<Resource> mapBibframe2RdfToLd(InputStream inputStream, String contentType);

  ByteArrayOutputStream mapLdToRdf(Resource resource, RDFFormat rdfFormat, ResourceMapping resourceMapping);

  ByteArrayOutputStream mapLdToBibframe2Rdf(Resource resource, RDFFormat rdfFormat);

}
