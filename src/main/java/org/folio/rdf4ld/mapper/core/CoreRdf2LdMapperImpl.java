package org.folio.rdf4ld.mapper.core;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.folio.rdf4ld.util.JsonUtil.getJsonMapper;
import static org.folio.rdf4ld.util.RdfUtil.IRI;
import static org.folio.rdf4ld.util.RdfUtil.getAllTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;

@Component
@RequiredArgsConstructor
public class CoreRdf2LdMapperImpl implements CoreRdf2LdMapper {
  private final RdfMapperUnitProvider rdfMapperUnitProvider;
  private final JsonMapper jsonMapper = getJsonMapper();

  @Override
  public JsonNode mapDoc(org.eclipse.rdf4j.model.Resource resource, Model model,
                         Collection<PropertyMapping> propertyMappings) {
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
    if (IRI.equals(pm.getBfProperty())) {
      addProperty(pm, doc, resource.stringValue());
    } else {
      model.getStatements(resource, Values.iri(pm.getBfProperty()), null)
        .forEach(st -> addProperty(pm, doc, st.getObject().stringValue()));
    }
  }

  private void addProperty(PropertyMapping pm, Map<String, List<String>> doc, String value) {
    var props = doc.computeIfAbsent(pm.getLdProperty().getValue(), str -> new ArrayList<>());
    if (!props.contains(value)) {
      props.add(value);
    }
  }

  @Override
  public JsonNode toJson(Map<String, List<String>> map) {
    var node = jsonMapper.convertValue(map, JsonNode.class);
    return !(node instanceof NullNode) ? node : jsonMapper.createObjectNode();
  }

  @Override
  public Set<ResourceEdge> mapOutgoingEdges(Collection<ResourceMapping> edgeMappings,
                                            Model model,
                                            Resource parent,
                                            org.eclipse.rdf4j.model.Resource rdfParent) {
    return ofNullable(edgeMappings)
      .stream()
      .flatMap(Collection::stream)
      .filter(oem -> nonNull(oem.getLdResourceDef()))
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
    var ldResourceDef = edgeMapping.getLdResourceDef();
    var mapperUnit = rdfMapperUnitProvider.getMapper(ldResourceDef.getTypeSet(), ldResourceDef.getPredicate());
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
        || TRUE.equals(bfResourceDef.getIgnoreTypesMatch())
        || (TRUE.equals(bfResourceDef.getPartialTypesMatch())
        ? getAllTypes(model, child).containsAll(bfResourceDef.getTypeSet())
        : getAllTypes(model, child).equals(new HashSet<>(bfResourceDef.getTypeSet())))
      );
  }

}
