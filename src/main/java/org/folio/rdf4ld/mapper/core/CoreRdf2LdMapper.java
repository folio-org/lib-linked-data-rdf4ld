package org.folio.rdf4ld.mapper.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import tools.jackson.databind.JsonNode;

public interface CoreRdf2LdMapper {

  JsonNode mapDoc(org.eclipse.rdf4j.model.Resource resource, Model model, Collection<PropertyMapping> propertyMappings);

  JsonNode toJson(Map<String, List<String>> map);

  Set<ResourceEdge> mapOutgoingEdges(Collection<ResourceMapping> edgeMappings,
                                     Model model,
                                     Resource parent,
                                     org.eclipse.rdf4j.model.Resource rdfParent);

}
