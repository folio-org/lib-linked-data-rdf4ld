package org.folio.rdf4ld.mapper.unit.monograph;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.util.ResourceUtil.getPrimaryMainTitle;

import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = WORK, predicate = INSTANTIATES)
public class WorkRdfMapperUnit implements RdfMapperUnit {
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final FingerprintHashService hashService;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceMapping mapping,
                          Resource parent) {
    var work = baseRdfMapperUnit.mapToLd(model, resource, mapping, parent);
    work.setLabel(getPrimaryMainTitle(work));
    work.setId(hashService.hash(work));
    return work;
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping);
  }
}
