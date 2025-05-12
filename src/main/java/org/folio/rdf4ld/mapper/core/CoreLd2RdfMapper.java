package org.folio.rdf4ld.mapper.core;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.model.ResourceInternalMapping;

public interface CoreLd2RdfMapper {

  IRI getResourceIri(String nameSpace, String id);

  void mapProperty(ModelBuilder modelBuilder,
                   String predicate,
                   Resource resource,
                   PropertyDictionary property);

  void mapOutgoingEdge(ModelBuilder modelBuilder,
                       ResourceEdge edge,
                       ResourceInternalMapping resourceMapping,
                       String nameSpace);

  void linkResources(ModelBuilder modelBuilder,
                     ResourceEdge edge,
                     String parentNamesSpace,
                     String targetNamesSpace,
                     String bfPredicate);
}
