package org.folio.rdf4ld.mapper.core;

import static java.util.Objects.nonNull;

import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.MapperUnitProvider;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreLd2RdfMapperImpl implements CoreLd2RdfMapper {
  private final MapperUnitProvider mapperUnitProvider;

  @Override
  public IRI getResourceIri(String nameSpace, String id) {
    return Values.iri(nameSpace, id);
  }

  @Override
  public void mapProperty(ModelBuilder modelBuilder,
                          String predicate,
                          Resource resource,
                          PropertyDictionary property) {
    if (property == PropertyDictionary.LABEL) {
      modelBuilder.add(predicate, resource.getLabel());
      return;
    }
    if (nonNull(resource.getDoc().get(property.getValue()))) {
      resource.getDoc().get(property.getValue())
        .forEach(node -> modelBuilder.add(predicate, node.asText()));
    }
  }

  @Override
  public void mapOutgoingEdge(ModelBuilder modelBuilder,
                              ResourceEdge edge,
                              ResourceInternalMapping resourceMapping,
                              String nameSpace) {
    resourceMapping.getOutgoingEdges().stream()
      .filter(oem -> edge.getTarget().getTypes().equals(oem.getLdResourceDef().getTypeSet())
        && edge.getPredicate().equals(oem.getLdResourceDef().getPredicate()))
      .forEach(oem -> {
        var mapper = mapperUnitProvider.getMapper(oem.getLdResourceDef());
        mapper.mapToBibframe(edge.getTarget(), modelBuilder, resourceMapping, oem.getBfNameSpace(),
          oem.getBfResourceDef().getTypeSet());
        linkResources(modelBuilder, edge, nameSpace, oem.getBfNameSpace(), oem.getBfResourceDef().getPredicate());
      });
  }

  @Override
  public void linkResources(ModelBuilder modelBuilder,
                            ResourceEdge edge,
                            String parentNamesSpace,
                            String targetNamesSpace,
                            String bfPredicate) {
    modelBuilder.subject(getResourceIri(parentNamesSpace, String.valueOf(edge.getSource().getId())));
    var iri = getResourceIri(targetNamesSpace, String.valueOf(edge.getTarget().getId()));
    modelBuilder.add(bfPredicate, iri);
  }

}
