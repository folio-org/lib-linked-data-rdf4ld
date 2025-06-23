package org.folio.rdf4ld.mapper.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;

public interface CoreRdf2LdMapper {

  JsonNode mapDoc(org.eclipse.rdf4j.model.Resource resource, Model model, Set<PropertyMapping> propertyMappings);

  JsonNode toJson(Map<String, List<String>> map);

  Stream<org.eclipse.rdf4j.model.Resource> selectSubjectsByType(Model model, Set<String> bfTypeSet);

  Set<ResourceEdge> mapOutgoingEdges(Set<ResourceMapping> edgeMappings,
                                     Model model,
                                     Resource parent,
                                     org.eclipse.rdf4j.model.Resource rdfParent);

  Set<String> getAllTypes(Model model, org.eclipse.rdf4j.model.Resource resource);

}
