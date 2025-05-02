package org.folio.rdf4ld.mapper.unit.monograph;

import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertiesString;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.Mapper;
import org.folio.rdf4ld.mapper.unit.BaseMapperUnit;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Mapper(types = TITLE, predicate = PredicateDictionary.TITLE)
public class TitleMapperUnit implements MapperUnit {
  private final BaseMapperUnit baseMapperUnit;

  @Override
  public Resource mapToLd(Model model,
                          Statement statement,
                          ResourceMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          String typeIri,
                          boolean fetchRemote) {
    var title = baseMapperUnit.mapToLd(model, statement, resourceMapping, ldTypes, typeIri, fetchRemote);
    title.setLabel(getPropertiesString(title.getDoc(), MAIN_TITLE, SUBTITLE));
    return title;
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
