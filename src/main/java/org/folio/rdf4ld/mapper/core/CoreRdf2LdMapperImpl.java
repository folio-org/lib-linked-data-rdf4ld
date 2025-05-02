package org.folio.rdf4ld.mapper.core;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.MapperUnitProvider;
import org.folio.rdf4ld.model.EdgeMapping;
import org.folio.rdf4ld.model.PropertyMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreRdf2LdMapperImpl implements CoreRdf2LdMapper {
  private final MapperUnitProvider mapperUnitProvider;
  private final ObjectMapper objectMapper;

  @Override
  public JsonNode mapDoc(Statement statement, Model model, Set<PropertyMapping> propertyMappings) {
    var doc = new HashMap<String, List<String>>();
    propertyMappings
      // TODO: look for a property under different subject (resource) if required by mapping profile
      // not needed for Thin Thread
      .forEach(pm -> model.getStatements(statement.getSubject(), Values.iri(pm.getBfProperty()), null)
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
  public Stream<Statement> selectStatementsByType(Model model, String typeIri, Set<String> bfTypeSet) {
    return model.stream()
      .filter(st -> st.getPredicate().stringValue().equals(typeIri)
        && bfTypeSet.contains(st.getObject().stringValue()))
      .map(Statement::getSubject)
      .flatMap(subject -> stream(model.getStatements(subject, null, null).spliterator(), false));
  }

  public Set<ResourceEdge> mapEdges(Set<EdgeMapping> edgeMappings,
                                    Model model,
                                    Resource parent,
                                    boolean outgoingOrIncoming,
                                    String typeIri) {
    return ofNullable(edgeMappings)
      .stream()
      .flatMap(Set::stream)
      .flatMap(oem -> mapEdgeTargets(model, oem, typeIri).stream()
        .map(r -> new ResourceEdge(
          getSource(parent, outgoingOrIncoming, r),
          getTarget(parent, outgoingOrIncoming, r),
          oem.getLdResourceDef().getPredicate()))
      )
      .collect(toSet());
  }

  private Resource getTarget(Resource parent, boolean outgoingOrIncoming, Resource current) {
    return outgoingOrIncoming ? current : parent;
  }

  private Resource getSource(Resource parent, boolean outgoingOrIncoming, Resource current) {
    return outgoingOrIncoming ? parent : current;
  }

  private Set<Resource> mapEdgeTargets(Model model, EdgeMapping edgeMapping, String typeIri) {
    // TODO fetch remote resource if edgeMapping.fetchRemote() is true
    var mapperUnit = mapperUnitProvider.getMapper(edgeMapping.getLdResourceDef());
    return selectStatementsByType(model, typeIri, edgeMapping.getBfResourceDef().getTypeSet())
      .map(st -> mapperUnit.mapToLd(model, st, edgeMapping.getResourceMapping(),
        edgeMapping.getLdResourceDef().getTypeSet(), typeIri, edgeMapping.getFetchRemote()))
      .collect(toSet());
  }

}
