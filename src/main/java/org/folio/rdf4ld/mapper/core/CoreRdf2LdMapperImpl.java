package org.folio.rdf4ld.mapper.core;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreRdf2LdMapperImpl implements CoreRdf2LdMapper {
  private static final String TYPE_IRI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private final RdfMapperUnitProvider rdfMapperUnitProvider;
  private final ObjectMapper objectMapper;

  @Override
  public JsonNode mapDoc(org.eclipse.rdf4j.model.Resource resource, Model model,
                         Set<PropertyMapping> propertyMappings) {
    var doc = new HashMap<String, List<String>>();
    propertyMappings
      // look for a property under different subject (resource) if required by mapping profile
      // not needed for Thin Thread
      .forEach(pm -> model.getStatements(resource, Values.iri(pm.getBfProperty()), null)
        .forEach(st -> {
            var props = doc.computeIfAbsent(pm.getLdProperty().getValue(), str -> new ArrayList<>());
            props.add(st.getObject().stringValue());
          }
        ));
    return doc.isEmpty() ? null : toJson(doc);
  }

  public JsonNode toJson(Map<String, List<String>> map) {
    var node = objectMapper.convertValue(map, JsonNode.class);
    return ! (node instanceof NullNode) ? node : objectMapper.createObjectNode();
  }

  @Override
  public Stream<org.eclipse.rdf4j.model.Resource> selectSubjectsByType(Model model,
                                                                       Set<String> bfTypeSet) {
    return bfTypeSet.stream()
      .map(type -> model.filter(null, Values.iri(TYPE_IRI), Values.iri(type)))
      .flatMap(Collection::stream)
      .map(Statement::getSubject);
  }

  @Override
  public Set<ResourceEdge> mapOutgoingEdges(Set<ResourceMapping> edgeMappings,
                                            Model model,
                                            Resource parent,
                                            org.eclipse.rdf4j.model.Resource rdfParent) {
    return ofNullable(edgeMappings)
      .stream()
      .flatMap(Set::stream)
      .flatMap(oem -> mapEdgeTargets(model, oem, parent, rdfParent).stream()
        .map(r -> new ResourceEdge(parent, r, oem.getLdResourceDef().getPredicate()))
      )
      .collect(toSet());
  }

  private Set<Resource> mapEdgeTargets(Model model,
                                       ResourceMapping edgeMapping,
                                       Resource parent,
                                       org.eclipse.rdf4j.model.Resource rdfParent) {
    // fetch remote resource if it's not presented and edgeMapping.localOnly() is not true
    var mapperUnit = rdfMapperUnitProvider.getMapper(edgeMapping.getLdResourceDef());
    return selectLinkedResources(model, edgeMapping.getBfResourceDef().getTypeSet(),
      edgeMapping.getBfResourceDef().getPredicate(), rdfParent)
      .map(resource -> mapperUnit.mapToLd(model, resource, edgeMapping, parent))
      .filter(Objects::nonNull)
      .collect(toSet());
  }

  private Stream<org.eclipse.rdf4j.model.Resource> selectLinkedResources(Model model,
                                                                         Set<String> bfTypeSet,
                                                                         String bfPredicate,
                                                                         org.eclipse.rdf4j.model.Resource parent) {
    return model.filter(parent, Values.iri(bfPredicate), null)
      .stream()
      .map(Statement::getObject)
      .filter(Value::isResource)
      .map(org.eclipse.rdf4j.model.Resource.class::cast)
      .filter(child -> bfTypeSet.isEmpty() || bfTypeSet.equals(getAllTypes(model, child)));
  }

  private Set<String> getAllTypes(Model model, org.eclipse.rdf4j.model.Resource resource) {
    return model.filter(resource, Values.iri(TYPE_IRI), null)
      .stream()
      .map(Statement::getObject)
      .map(Value::stringValue)
      .collect(Collectors.toSet());
  }

}
