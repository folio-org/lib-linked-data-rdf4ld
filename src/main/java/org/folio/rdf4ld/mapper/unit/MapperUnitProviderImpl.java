package org.folio.rdf4ld.mapper.unit;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.rdf4ld.model.LdResourceDef;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MapperUnitProviderImpl implements MapperUnitProvider {
  private final BaseMapperUnit baseMapperUnit;
  private final Set<MapperUnit> mapperUnits;

  public MapperUnitProviderImpl(@Lazy BaseMapperUnit baseMapperUnit,
                                @Lazy Set<MapperUnit> mapperUnits) {
    this.baseMapperUnit = baseMapperUnit;
    this.mapperUnits = mapperUnits;
  }

  @Override
  public MapperUnit getMapper(LdResourceDef ldResourceDef) {
    return mapperUnits.stream()
      .filter(m -> m.getClass().isAnnotationPresent(MapperDefinition.class))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperDefinition.class);
        return isNull(ldResourceDef.getTypeSet()) || ldResourceDef.getTypeSet().equals(toSet(annotation.types()))
          && isNull(ldResourceDef.getPredicate()) || ldResourceDef.getPredicate() == annotation.predicate();
      })
      .findFirst()
      .orElseGet(() -> {
        log.info("No mapper found for resource types [{}]{}, using BaseMapperUnit",
          ldResourceDef.getTypeSet().stream().map(ResourceTypeDictionary::getUri).collect(joining(", ")),
          ldResourceDef.getPredicate() != null
            ? " and predicate [" + ldResourceDef.getPredicate().getUri() + "]" : null);
        return baseMapperUnit;
      });
  }

  private HashSet<ResourceTypeDictionary> toSet(ResourceTypeDictionary[] types) {
    return new HashSet<>(asList(types));
  }
}
