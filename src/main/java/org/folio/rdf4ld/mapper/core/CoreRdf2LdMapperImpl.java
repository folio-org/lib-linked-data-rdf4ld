package org.folio.rdf4ld.mapper.core;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreRdf2LdMapperImpl implements CoreRdf2LdMapper {
  private final RdfMapperUnitProvider rdfMapperUnitProvider;
  private final ObjectMapper objectMapper;

  @Override
  public JsonNode mapDoc(org.eclipse.rdf4j.model.Resource resource, Model model,
                         Set<PropertyMapping> propertyMappings) {
    var doc = new HashMap<String, List<String>>();
    propertyMappings
      .forEach(pm -> {
        if (isNull(pm.getEdgeParentBfDef())) {
          getDirectProperty(resource, model, pm, doc);
        } else {
          selectLinkedResources(model, pm.getEdgeParentBfDef(), resource)
            .forEach(r -> getDirectProperty(r, model, pm, doc));
        }
      });
    return doc.isEmpty() ? null : toJson(doc);
  }

  private void getDirectProperty(org.eclipse.rdf4j.model.Resource resource,
                                 Model model,
                                 PropertyMapping pm,
                                 Map<String, List<String>> doc) {
    model.getStatements(resource, Values.iri(pm.getBfProperty()), null)
      .forEach(st -> {
          var props = doc.computeIfAbsent(pm.getLdProperty().getValue(), str -> new ArrayList<>());
          props.add(st.getObject().stringValue());
        }
      );
  }

  @Override
  public JsonNode toJson(Map<String, List<String>> map) {
    var node = objectMapper.convertValue(map, JsonNode.class);
    return !(node instanceof NullNode) ? node : objectMapper.createObjectNode();
  }

  @Override
  public Stream<org.eclipse.rdf4j.model.Resource> selectSubjectsByType(Model model,
                                                                       Set<String> bfTypeSet) {
    return bfTypeSet.stream()
      .map(type -> model.filter(null, RDF.TYPE, Values.iri(type)))
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
      .filter(oem -> nonNull(oem.getLdResourceDef()))
      .flatMap(oem -> mapEdgeTargets(model, oem, parent, rdfParent).stream()
        .map(r -> new ResourceEdge(parent, r, oem.getLdResourceDef().getPredicate()))
      )
      .collect(toSet());
  }

  @Override
  public Set<String> getAllTypes(Model model, org.eclipse.rdf4j.model.Resource resource) {
    return model.filter(resource, RDF.TYPE, null)
      .stream()
      .map(Statement::getObject)
      .map(Value::stringValue)
      .collect(Collectors.toSet());
  }

  private Set<Resource> mapEdgeTargets(Model model,
                                       ResourceMapping edgeMapping,
                                       Resource parent,
                                       org.eclipse.rdf4j.model.Resource rdfParent) {
    // fetch remote resource if it's not presented and edgeMapping.localOnly() is not true
    var mapperUnit = rdfMapperUnitProvider.getMapper(edgeMapping.getLdResourceDef());
    return selectLinkedResources(model, edgeMapping.getBfResourceDef(), rdfParent)
      .map(resource -> mapperUnit.mapToLd(model, resource, edgeMapping, parent))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(toSet());
  }

  private Stream<org.eclipse.rdf4j.model.Resource> selectLinkedResources(Model model,
                                                                         BfResourceDef bfResourceDef,
                                                                         org.eclipse.rdf4j.model.Resource parent) {
    return model.filter(parent, Values.iri(bfResourceDef.getPredicate()), null)
      .stream()
      .map(Statement::getObject)
      .filter(Value::isResource)
      .map(org.eclipse.rdf4j.model.Resource.class::cast)
      .filter(child -> bfResourceDef.getTypeSet().isEmpty()
        || (TRUE.equals(bfResourceDef.getPartialTypesMatch())
        ? getAllTypes(model, child).containsAll(bfResourceDef.getTypeSet())
        : getAllTypes(model, child).equals(bfResourceDef.getTypeSet()))
      );
  }

}
