package org.folio.rdf4ld.mapper.unit.monograph;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.rdf4ld.util.ResourceUtil.getFirstValue;
import static org.folio.rdf4ld.util.ResourceUtil.getPrimaryMainTitles;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.Mapper;
import org.folio.rdf4ld.mapper.unit.BaseMapperUnit;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;

@Mapper(types = INSTANCE)
@RequiredArgsConstructor
public class InstanceMapperUnit implements MapperUnit {
  private final BaseMapperUnit baseMapperUnit;

  @Override
  public Resource mapToLd(Model model,
                          Statement statement,
                          ResourceMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          String typeIri,
                          boolean fetchRemote) {
    var instance = baseMapperUnit.mapToLd(model, statement, resourceMapping, ldTypes, typeIri, fetchRemote);
    instance.setLabel(getFirstValue(() -> getPrimaryMainTitles(instance)));
    return instance;
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            String nameSpace,
                            Set<String> bfTypeSet) {
    baseMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, nameSpace, bfTypeSet);
  }
}
