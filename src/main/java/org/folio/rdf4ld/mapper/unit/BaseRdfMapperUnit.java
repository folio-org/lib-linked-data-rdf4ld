package org.folio.rdf4ld.mapper.unit;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL_RDF;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertiesString;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseRdfMapperUnit implements RdfMapperUnit {

  private static final PropertyDictionary[] DEFAULT_LABELS = {LABEL, LABEL_RDF};
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource rdfResource,
                          ResourceInternalMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          Boolean localOnly, Function<String, Optional<Resource>> resourceProvider) {
    var resource = new Resource();
    resource.setCreatedDate(new Date());
    resource.setTypes(ldTypes);
    resource.setDoc(coreRdf2LdMapper.mapDoc(rdfResource, model, resourceMapping.getProperties()));
    setLabel(resource, resourceMapping);
    var outEdges = coreRdf2LdMapper.mapOutgoingEdges(resourceMapping.getOutgoingEdges(), model, resource, rdfResource,
      resourceProvider);
    resource.setOutgoingEdges(outEdges);
    resource.setId(hashService.hash(resource));
    return resource;
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
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceInternalMapping resourceMapping,
                            String nameSpace,
                            Set<String> bfTypeSet) {
    modelBuilder.subject(coreLd2RdfMapper.getResourceIri(nameSpace, String.valueOf(resource.getId())))
      .add(RDF.TYPE, bfTypeSet.iterator().next());
    resourceMapping.getProperties()
      .forEach(p -> coreLd2RdfMapper.mapProperty(modelBuilder, p.getBfProperty(), resource, p.getLdProperty()));
    resource.getOutgoingEdges()
      .forEach(oe -> coreLd2RdfMapper.mapOutgoingEdge(modelBuilder, oe, resourceMapping, nameSpace));
  }

}
