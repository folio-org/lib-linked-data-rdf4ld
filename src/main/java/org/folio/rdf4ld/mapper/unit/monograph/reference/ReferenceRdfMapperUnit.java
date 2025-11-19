package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static java.util.Optional.empty;
import static org.folio.rdf4ld.util.RdfUtil.readSupportedExtraTypes;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;

@RequiredArgsConstructor
public abstract class ReferenceRdfMapperUnit implements RdfMapperUnit {
  protected final BaseRdfMapperUnit baseRdfMapperUnit;
  protected final FingerprintHashService hashService;
  protected final MockLccnResourceService mockLccnResourceService;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    Optional<Resource> resourceOptional = empty();
    if (resource instanceof IRI iri) {
      resourceOptional = Optional.of(mockLccnResourceService.mockLccnResource(iri.getLocalName()));
    }
    if (resource instanceof BNode node) {
      resourceOptional = baseRdfMapperUnit.mapToLd(model, node, mapping, parent)
        .map(mapped -> addExtraTypes(model, node, mapped));
    }
    return resourceOptional;
  }

  private Resource addExtraTypes(Model model, BNode node, Resource mapped) {
    readSupportedExtraTypes(model, node).forEach(mapped::addType);
    mapped.setId(hashService.hash(mapped));
    return mapped;
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, parent);
  }

}
