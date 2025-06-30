package org.folio.rdf4ld.mapper.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreLd2RdfMapperImpl implements CoreLd2RdfMapper {
  private static final String LD_NAMESPACE = "http://test-tobe-changed.folio.com/resources/";
  private final RdfMapperUnitProvider rdfMapperUnitProvider;

  @Override
  public IRI getResourceIri(String id) {
    return Values.iri(LD_NAMESPACE, id);
  }

  @Override
  public void mapProperties(Resource resource, ModelBuilder modelBuilder, ResourceMapping mapping) {
    final var idMap = new EnumMap<PropertyDictionary, Integer>(PropertyDictionary.class);
    ofNullable(mapping.getResourceMapping())
      .ifPresent(irm ->
        irm.getProperties().forEach(p -> {
          if (isNull(p.getEdgeParentBfDef())) {
            mapDirectProperty(modelBuilder, p.getBfProperty(), resource, p.getLdProperty());
          } else {
            mapPropertyToAnotherResource(modelBuilder, resource, p, idMap);
          }
        })
      );
  }

  @Override
  public void mapOutgoingEdge(ModelBuilder modelBuilder,
                              ResourceEdge edge,
                              ResourceInternalMapping mapping) {
    mapping.getOutgoingEdges().stream()
      .filter(oem -> nonNull(oem.getLdResourceDef()))
      .filter(oem -> (oem.getLdResourceDef().getTypeSet().isEmpty()
        || edge.getTarget().getTypes().equals(oem.getLdResourceDef().getTypeSet()))
        && edge.getPredicate().equals(oem.getLdResourceDef().getPredicate()))
      .forEach(oem -> {
        var mapper = rdfMapperUnitProvider.getMapper(oem.getLdResourceDef());
        mapper.mapToBibframe(edge.getTarget(), modelBuilder, oem, edge.getSource());
      });
  }

  @Override
  public void linkResources(ModelBuilder modelBuilder,
                            org.eclipse.rdf4j.model.Resource source,
                            org.eclipse.rdf4j.model.Resource target,
                            String bfPredicate) {
    modelBuilder.subject(source);
    modelBuilder.add(bfPredicate, target);
  }

  private String generateId(Resource resource,
                            org.folio.rdf4ld.model.PropertyMapping p,
                            Map<PropertyDictionary, Integer> idMap) {
    idMap.put(p.getLdProperty(), idMap.getOrDefault(p.getLdProperty(), 0) + 1);
    return p.getLdProperty().name() + "_" + idMap.get(p.getLdProperty()) + "_" + resource.getId();
  }

  private void mapDirectProperty(ModelBuilder modelBuilder,
                                 String predicate,
                                 Resource resource,
                                 PropertyDictionary property) {
    if (property == PropertyDictionary.LABEL) {
      modelBuilder.add(predicate, resource.getLabel());
      return;
    }
    ofNullable(resource.getDoc())
      .map(d -> d.get(property.getValue()))
      .ifPresent(propertyArray ->
        propertyArray.forEach(node -> modelBuilder.add(predicate, node.asText()))
      );
  }

  private void mapPropertyToAnotherResource(ModelBuilder modelBuilder,
                                            Resource resource,
                                            PropertyMapping pm,
                                            Map<PropertyDictionary, Integer> idMap) {
    ofNullable(resource.getDoc())
      .map(doc -> doc.get(pm.getLdProperty().getValue()))
      .ifPresent(propertyArray ->
        propertyArray.forEach(node -> {
          var id = generateId(resource, pm, idMap);
          var blankNode = Values.bnode(id);
          modelBuilder.subject(blankNode);
          pm.getEdgeParentBfDef().getTypeSet().forEach(type -> modelBuilder.add(RDF.TYPE, Values.iri(type)));
          modelBuilder.add(pm.getBfProperty(), node.asText());
          linkResources(modelBuilder, getResourceIri(resource.getId().toString()), blankNode,
            pm.getEdgeParentBfDef().getPredicate()
          );
        })
      );
  }

}
