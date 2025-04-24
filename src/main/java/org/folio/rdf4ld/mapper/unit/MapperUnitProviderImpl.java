package org.folio.rdf4ld.mapper.unit;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.rdf4ld.mapper.Mapper;
import org.folio.rdf4ld.model.LdResourceDef;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MapperUnitProviderImpl implements MapperUnitProvider {
  private final BaseMapperUnit baseMapperUnit;
  private final Set<MapperUnit> mapperUnits;

  @Override
  public MapperUnit getMapper(LdResourceDef ldResourceDef) {
    return mapperUnits.stream()
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(Mapper.class);
        return isNull(ldResourceDef.typeSet()) || ldResourceDef.typeSet().equals(toSet(annotation.types()))
          && isNull(ldResourceDef.predicate()) || ldResourceDef.predicate() == annotation.predicate();
      })
      .findFirst()
      .orElseGet(() -> {
        log.info("No mapper found for resource types [{}]{}, using BaseMapperUnit",
          ldResourceDef.typeSet().stream().map(ResourceTypeDictionary::getUri).collect(joining(", ")),
          ldResourceDef.predicate() != null ? " and predicate [" + ldResourceDef.predicate().getUri() + "]" : null);
        return baseMapperUnit;
      });
  }

  private HashSet<ResourceTypeDictionary> toSet(ResourceTypeDictionary[] types) {
    return new HashSet<>(asList(types));
  }
}
