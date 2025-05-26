package org.folio.rdf4ld.mapper.unit.monograph;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.util.ResourceUtil.getPrimaryMainTitle;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = WORK, predicate = INSTANTIATES)
public class WorkRdfMapperUnit implements RdfMapperUnit {
  private final BaseRdfMapperUnit baseRdfMapperUnit;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceInternalMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          Boolean localOnly,
                          Function<String, Optional<Resource>> resourceProvider) {
    var work = baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, ldTypes, localOnly, resourceProvider);
    work.setLabel(getPrimaryMainTitle(work));
    return work;
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceInternalMapping resourceMapping,
                            String nameSpace,
                            Set<String> bfTypeSet) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, nameSpace, bfTypeSet);
  }
}
