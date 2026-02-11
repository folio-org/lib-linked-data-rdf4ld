package org.folio.rdf4ld.mapper.unit;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
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
  public RdfMapperUnit getMapper(Collection<ResourceTypeDictionary> typeSet, PredicateDictionary predicate) {
    return rdfMapperUnits.stream()
      .filter(m -> m.getClass().isAnnotationPresent(RdfMapperDefinition.class))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(RdfMapperDefinition.class);
        return (typeSet.isEmpty() || new HashSet<>(typeSet).equals(toSet(annotation.types())))
          && (isNull(predicate) || predicate == annotation.predicate());
      })
      .findFirst()
      .orElseGet(() -> {
        log.debug("No mapper found for resource types [{}]{}, using BaseMapperUnit",
          typeSet.stream().map(ResourceTypeDictionary::getUri).collect(joining(", ")),
          predicate != null ? " and predicate [" + predicate.getUri() + "]" : null);
        return baseMapperUnit;
      });
  }

  private HashSet<ResourceTypeDictionary> toSet(ResourceTypeDictionary[] types) {
    return new HashSet<>(asList(types));
  }
}
