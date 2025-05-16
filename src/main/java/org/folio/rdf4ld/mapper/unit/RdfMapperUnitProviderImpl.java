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
public class RdfMapperUnitProviderImpl implements RdfMapperUnitProvider {
  private final BaseRdfMapperUnit baseMapperUnit;
  private final Set<RdfMapperUnit> rdfMapperUnits;

  public RdfMapperUnitProviderImpl(@Lazy BaseRdfMapperUnit baseMapperUnit,
                                   @Lazy Set<RdfMapperUnit> rdfMapperUnits) {
    this.baseMapperUnit = baseMapperUnit;
    this.rdfMapperUnits = rdfMapperUnits;
  }

  @Override
  public RdfMapperUnit getMapper(LdResourceDef ldResourceDef) {
    return rdfMapperUnits.stream()
      .filter(m -> m.getClass().isAnnotationPresent(RdfMapperDefinition.class))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(RdfMapperDefinition.class);
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
