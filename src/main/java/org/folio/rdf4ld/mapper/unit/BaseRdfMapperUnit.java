package org.folio.rdf4ld.mapper.unit;

import static java.util.Optional.ofNullable;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL_RDF;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertiesString;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseRdfMapperUnit implements RdfMapperUnit {

  private static final PropertyDictionary[] DEFAULT_LABELS = {LABEL, LABEL_RDF};
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;
  private final Function<Long, String> resourceUrlProvider;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource rdfResource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    var resourceMapping = mapping.getResourceMapping();
    var resource = new Resource();
    resource.setCreatedDate(new Date());
    resource.getTypes().addAll(mapping.getLdResourceDef().getTypeSet());
    ofNullable(resourceMapping)
      .ifPresent(rm -> {
        resource.setDoc(coreRdf2LdMapper.mapDoc(rdfResource, model, resourceMapping.getProperties()));
        setLabel(resource, resourceMapping);
        var outEdges = coreRdf2LdMapper.mapOutgoingEdges(resourceMapping.getOutgoingEdges(),
          model, resource, rdfResource);
        resource.getOutgoingEdges().addAll(outEdges);
      });
    resource.setId(hashService.hash(resource));
    return Optional.of(resource);
  }

  private void setLabel(Resource resource, ResourceInternalMapping resourceMapping) {
    if (resourceMapping.getLabel().isEmpty()) {
      resource.setLabel(getPropertiesString(resource.getDoc(), DEFAULT_LABELS));
    } else {
      var labelProperties = resourceMapping.getLabel().toArray(PropertyDictionary[]::new);
      resource.setLabel(getPropertiesString(resource.getDoc(), labelProperties));
    }
  }

  @Override
  public void mapToBibframe(Resource resource, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    var resourceIri = iri(resourceUrlProvider.apply(resource.getId()));
    modelBuilder.subject(resourceIri);
    mapping.getBfResourceDef().getTypeSet().forEach(type -> modelBuilder.add(RDF.TYPE, iri(type)));
    coreLd2RdfMapper.mapProperties(resource, modelBuilder, mapping);
    resource.getOutgoingEdges().forEach(oe ->
      coreLd2RdfMapper.mapOutgoingEdge(modelBuilder, oe, mapping.getResourceMapping())
    );
    ofNullable(parent)
      .ifPresent(p -> linkResources(iri(resourceUrlProvider.apply(p.getId())),
        resourceIri, mapping.getBfResourceDef().getPredicate(), modelBuilder)
      );
  }

}
