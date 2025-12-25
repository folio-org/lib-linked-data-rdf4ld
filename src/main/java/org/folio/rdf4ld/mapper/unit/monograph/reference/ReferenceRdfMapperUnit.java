package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static java.util.Optional.of;
import static org.eclipse.rdf4j.model.util.Values.bnode;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.RdfUtil.writeBlankNode;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccnLink;

import java.util.Optional;
import java.util.function.LongFunction;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.folio.rdf4ld.util.ResourceUtil;

@RequiredArgsConstructor
public abstract class ReferenceRdfMapperUnit implements RdfMapperUnit {
  protected final BaseRdfMapperUnit baseRdfMapperUnit;
  protected final FingerprintHashService hashService;
  protected final MockLccnResourceService mockLccnResourceService;
  protected final LongFunction<String> resourceUrlProvider;
  protected final CoreLd2RdfMapper coreLd2RdfMapper;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    var mappedOptional = baseRdfMapperUnit.mapToLd(model, resource, mapping, parent)
      .map(mapped -> ResourceUtil.enrichResource(mapped, model, resource, hashService));
    if (resource instanceof IRI iri) {
      mappedOptional = of(mockLccnResourceService.mockLccnResource(mappedOptional.orElse(null), iri.getLocalName()));
    }
    return mappedOptional;
  }

  @Override
  public void mapToBibframe(Resource reference,
                            ModelBuilder modelBuilder,
                            ResourceMapping mapping,
                            Resource parent) {
    var parentIri = iri(resourceUrlProvider.apply(parent.getId()));
    var predicate = mapping.getBfResourceDef().getPredicate();
    getCurrentLccnLink(reference)
      .ifPresentOrElse(link -> linkResources(parentIri, iri(link), predicate, modelBuilder),
        () -> {
          var node = bnode("_" + reference.getId());
          linkResources(parentIri, node, predicate, modelBuilder);
          writeBlankNode(node, reference, modelBuilder, mapping, coreLd2RdfMapper);
        }
      );
  }

}
