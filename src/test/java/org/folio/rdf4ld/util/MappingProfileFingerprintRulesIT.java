package org.folio.rdf4ld.util;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ABBREVIATED_TITLE;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class MappingProfileFingerprintRulesIT {

  private static final Set<Set<String>> EXCLUDED_TYPES = Set.of(
    Set.of(ABBREVIATED_TITLE.name()) // Use all properties for fingerprinting
  );

  @Autowired
  private MappingProfileReader mappingProfileReader;

  @Autowired
  private FingerprintRules fingerprintRules;

  @Test
  void getBibframe20Profile_shouldHaveFingerprintRulesForAllTypeCombinations() {
    // given
    var mappingProfile = mappingProfileReader.getBibframe20Profile();
    var profileTypeCombinations = extractTypeCombinations(mappingProfile.getTopResourceMappings());
    profileTypeCombinations.removeAll(EXCLUDED_TYPES);
    var fingerprintTypeCombinations = fingerprintRules.getRules().stream()
      .map(FingerprintRules.FingerprintRule::types)
      .map(this::normalizeTypes)
      .collect(LinkedHashSet::new, Set::add, Set::addAll);

    // when
    var missingTypeCombinations = new LinkedHashSet<>(profileTypeCombinations);
    missingTypeCombinations.removeAll(fingerprintTypeCombinations);

    // then
    assertThat(missingTypeCombinations)
      .withFailMessage("Missing fingerprint rules for type combinations: %s", missingTypeCombinations)
      .isEmpty();
  }

  private Set<Set<String>> extractTypeCombinations(List<ResourceMapping> resourceMappings) {
    var typeCombinations = new LinkedHashSet<Set<String>>();
    resourceMappings.forEach(resourceMapping ->
      collectTypeCombinations(resourceMapping, typeCombinations)
    );
    return typeCombinations;
  }

  private void collectTypeCombinations(ResourceMapping resourceMapping, Set<Set<String>> typeCombinations) {
    if (resourceMapping == null) {
      return;
    }

    var typeSet = resourceMapping.getLdResourceDef();
    if (typeSet != null) {
      var normalizedTypes = normalizeTypes(typeSet.getTypeSet());
      if (!normalizedTypes.isEmpty()) {
        typeCombinations.add(normalizedTypes);
      }
    }

    var internalMapping = resourceMapping.getResourceMapping();
    if (internalMapping != null && internalMapping.getOutgoingEdges() != null) {
      internalMapping.getOutgoingEdges().forEach(outgoingEdge ->
        collectTypeCombinations(outgoingEdge, typeCombinations)
      );
    }
  }

  private Set<String> normalizeTypes(Collection<?> types) {
    return types.stream()
      .filter(Objects::nonNull)
      .map(this::toTypeName)
      .map(String::trim)
      .filter(not(String::isBlank))
      .collect(LinkedHashSet::new, Set::add, Set::addAll);
  }

  private String toTypeName(Object type) {
    if (type instanceof Enum<?> enumType) {
      return enumType.name();
    }
    return String.valueOf(type);
  }
}
