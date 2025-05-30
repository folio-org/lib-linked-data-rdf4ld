package org.folio.rdf4ld.mapper.unit.monograph;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.rdf4ld.util.ResourceUtil.getPrimaryMainTitle;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = INSTANCE)
public class InstanceRdfMapperUnit implements RdfMapperUnit {
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final FingerprintHashService hashService;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceInternalMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          Boolean localOnly) {
    var instance = baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, ldTypes, localOnly);
    instance.setLabel(getPrimaryMainTitle(instance));
    instance.setId(hashService.hash(instance));
    return instance;
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
