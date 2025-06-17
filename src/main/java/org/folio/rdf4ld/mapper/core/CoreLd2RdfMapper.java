package org.folio.rdf4ld.mapper.core;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;

public interface CoreLd2RdfMapper {

  IRI getResourceIri(String nameSpace, String id);

  void mapProperties(Resource resource, ModelBuilder modelBuilder, ResourceMapping mapping);

  void mapOutgoingEdge(ModelBuilder modelBuilder,
                       ResourceEdge edge,
                       ResourceInternalMapping resourceMapping,
                       String nameSpace);

}
