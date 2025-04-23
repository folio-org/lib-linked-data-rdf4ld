package org.folio.rdf4ld.mapper.core;

import static java.util.Optional.ofNullable;
import static java.util.stream.StreamSupport.stream;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.mapper.unit.monograph.InstanceMapperUnit;
import org.folio.rdf4ld.mapper.unit.monograph.TitleMapperUnit;
import org.folio.rdf4ld.mapper.unit.monograph.WorkMapperUnit;
import org.folio.rdf4ld.model.EdgeMapping;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.PropertyMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreRdf2LdMapperImpl implements CoreRdf2LdMapper {

  private final ObjectMapper objectMapper;

  @Override
  public JsonNode mapDoc(Statement statement, Model model, Set<PropertyMapping> propertyMappings) {
    var doc = new HashMap<String, List<String>>();
    propertyMappings
      // TODO: look for a property under different subject (resource) if required by mapping profile
      // not needed for Thin Thread
      .forEach(pm -> model.getStatements(statement.getSubject(), Values.iri(pm.bfProperty()), null)
        .forEach(st -> {
            var props = doc.computeIfAbsent(pm.ldProperty().getValue(), str -> new ArrayList<>());
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

  @Override
  // TODO this is dummy mapper selector, it should be replaced with Spring Bean resolving by custom annotation
  public MapperUnit getMapper(LdResourceDef ldResourceDef) {
    if (ldResourceDef.typeSet().equals(Set.of(INSTANCE))) {
      return new InstanceMapperUnit(this, new CoreLd2RdfMapperImpl(objectMapper, this));
    } else if (ldResourceDef.typeSet().equals(Set.of(TITLE))
      && PredicateDictionary.TITLE.equals(ldResourceDef.predicate())) {
      return new TitleMapperUnit(this, new CoreLd2RdfMapperImpl(objectMapper, this));
    } else if (ldResourceDef.typeSet().equals(Set.of(WORK))
      && PredicateDictionary.INSTANTIATES.equals(ldResourceDef.predicate())) {
      return new WorkMapperUnit(this, new CoreLd2RdfMapperImpl(objectMapper, this));
    } else {
      // more resources in future
      return null;
    }
  }

  public Set<ResourceEdge> mapEdges(Set<EdgeMapping> edgeMappings,
                                    Model model,
                                    Resource parent,
                                    boolean outgoingOrIncoming,
                                    String typeIri) {
    return ofNullable(edgeMappings)
      .stream()
      .flatMap(Set::stream)
      .flatMap(oem -> mapEdges(model, oem, typeIri).stream().
        map(r -> new ResourceEdge(
          getSource(parent, outgoingOrIncoming, r),
          getTarget(parent, outgoingOrIncoming, r),
          oem.ldResourceDef().predicate()))
      )
      .collect(Collectors.toSet());
  }

  private Resource getTarget(Resource parent, boolean outgoingOrIncoming, Resource current) {
    return outgoingOrIncoming ? current : parent;
  }

  private Resource getSource(Resource parent, boolean outgoingOrIncoming, Resource current) {
    return outgoingOrIncoming ? parent : current;
  }

  private Set<Resource> mapEdges(Model model, EdgeMapping edgeMapping, String typeIri) {
    // TODO fetch remote resource if edgeMapping.fetchRemote() is true
    return ofNullable(getMapper(edgeMapping.ldResourceDef()))
      .map(mapperUnit -> selectStatementsByType(model, typeIri, edgeMapping.bfResourceDef().typeSet())
        .map(st -> mapperUnit.mapToLd(model, st, edgeMapping.fetchRemote()))
        .collect(Collectors.toSet()))
      .orElseGet(() -> {
        System.out.println("No mapper present for edge mapping " + edgeMapping);
        return new HashSet<>();
      });
  }

}
