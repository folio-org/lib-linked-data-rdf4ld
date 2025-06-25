package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class InstanceIdentifiersMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithIdentifiers() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_identifiers.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    final var expectedLccn = "  2010470075";
    final var expectedIsbn = "0850598370";
    final var expectedEan = "780696204364";
    final var currentStatusLabel = "current";
    final var currentStatusLink = "http://id.loc.gov/vocabulary/mstatus/current";
    final var cancelledStatusLabel = "cancinv";
    final var cancelledStatusLink = "http://id.loc.gov/vocabulary/mstatus/cancinv";

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getOutgoingEdges()).hasSize(3);
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_LCCN),
      Map.of(NAME, List.of(expectedLccn)), expectedLccn,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(currentStatusLabel), LINK, List.of(currentStatusLink)), currentStatusLabel)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_ISBN),
      Map.of(NAME, List.of(expectedIsbn), QUALIFIER, List.of("pbk")), expectedIsbn,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(cancelledStatusLabel), LINK, List.of(cancelledStatusLink)), cancelledStatusLabel)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_EAN),
      Map.of(EAN_VALUE, List.of(expectedEan), QUALIFIER, List.of("abc")), expectedEan, null
    );
  }

}
