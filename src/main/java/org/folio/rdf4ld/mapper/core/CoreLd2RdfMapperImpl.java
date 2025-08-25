package org.folio.rdf4ld.mapper.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
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
  private final Function<Long, String> resourceUrlProvider;
  private final RdfMapperUnitProvider rdfMapperUnitProvider;

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
    ofNullable(mapping)
      .ifPresent(m -> m.getOutgoingEdges().stream()
        .filter(oem -> nonNull(oem.getLdResourceDef()))
        .filter(oem -> (oem.getLdResourceDef().getTypeSet().isEmpty()
          || edge.getTarget().getTypes().containsAll(oem.getLdResourceDef().getTypeSet()))
          && edge.getPredicate().equals(oem.getLdResourceDef().getPredicate()))
        .forEach(oem -> {
          var mapper = rdfMapperUnitProvider.getMapper(oem.getLdResourceDef());
          mapper.mapToBibframe(edge.getTarget(), modelBuilder, oem, edge.getSource());
        })
      );
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
      .ifPresent(propertyArray -> {
        var predicate = pm.getEdgeParentBfDef().getPredicate();
        propertyArray.forEach(node -> {
          var id = generateId(resource, pm, idMap);
          var blankNode = Values.bnode(id);
          modelBuilder.subject(blankNode);
          pm.getEdgeParentBfDef().getTypeSet().forEach(type -> modelBuilder.add(RDF.TYPE, iri(type)));
          modelBuilder.add(pm.getBfProperty(), node.asText());
          linkResources(iri(resourceUrlProvider.apply(resource.getId())), blankNode, predicate, modelBuilder);
        });
      });
  }

}
