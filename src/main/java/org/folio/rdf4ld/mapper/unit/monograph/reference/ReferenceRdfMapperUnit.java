package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static java.util.Optional.empty;
import static org.folio.rdf4ld.util.RdfUtil.AUTHORITY_LD_TO_BF_TYPES;

import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;

@RequiredArgsConstructor
public abstract class ReferenceRdfMapperUnit implements RdfMapperUnit {
  protected final BaseRdfMapperUnit baseRdfMapperUnit;
  private final FingerprintHashService hashService;
  private final Function<String, Optional<Resource>> resourceProvider;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    Optional<Resource> resourceOptional = empty();
    if (resource instanceof IRI iri) {
      resourceOptional = resourceProvider.apply(iri.getLocalName());
    }
    if (resource instanceof BNode node) {
      resourceOptional = baseRdfMapperUnit.mapToLd(model, node, mapping, parent)
        .map(mapped -> addExtraTypes(model, node, mapped));
    }
    return resourceOptional;
  }

  private Resource addExtraTypes(Model model, BNode node, Resource mapped) {
    model.filter(node, RDF.TYPE, null)
      .stream()
      .map(Statement::getObject)
      .map(Value::stringValue)
      .filter(type -> AUTHORITY_LD_TO_BF_TYPES.inverse().containsKey(type))
      .map(type -> AUTHORITY_LD_TO_BF_TYPES.inverse().get(type))
      .forEach(mapped::addType);
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
