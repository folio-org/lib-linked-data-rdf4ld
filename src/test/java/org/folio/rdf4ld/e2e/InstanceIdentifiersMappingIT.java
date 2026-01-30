package org.folio.rdf4ld.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.rdf4ld.test.MonographUtil.STATUS_BASE_URI;
import static org.folio.rdf4ld.test.MonographUtil.STATUS_CANCELLED;
import static org.folio.rdf4ld.test.MonographUtil.STATUS_CURRENT;
import static org.folio.rdf4ld.test.MonographUtil.createEan;
import static org.folio.rdf4ld.test.MonographUtil.createIdentifier;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createIsbn;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
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

  private static final String EXPECTED_EAN = "780696204364";
  private static final String EXPECTED_ISBN = "0850598370";
  private static final String EXPECTED_LCCN = "  2010470075";
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithIdentifiers() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_identifiers.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getOutgoingEdges()).hasSize(3);
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_LCCN),
      Map.of(NAME, List.of(EXPECTED_LCCN)), EXPECTED_LCCN,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(STATUS_CURRENT), LINK, List.of(STATUS_BASE_URI + STATUS_CURRENT)), STATUS_CURRENT)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_ISBN),
      Map.of(NAME, List.of(EXPECTED_ISBN), QUALIFIER, List.of("pbk")), EXPECTED_ISBN,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(STATUS_CANCELLED), LINK, List.of(STATUS_BASE_URI + STATUS_CANCELLED)), STATUS_CANCELLED)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_IAN),
      Map.of(NAME, List.of(EXPECTED_EAN), QUALIFIER, List.of("abc")), EXPECTED_EAN, null
    );
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithIdentifiers() throws IOException {
    // given
    var ean = createEan(EXPECTED_EAN);
    var isbn = createIsbn(EXPECTED_ISBN, false);
    var lccn = createIdentifier(EXPECTED_LCCN, ID_LCCN, "", true);
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, ean, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, isbn, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, lccn, MAP));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/instance_identifiers.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("EAN_ID", ean.getId().toString())
      .replaceAll("ISBN_ID", isbn.getId().toString())
      .replaceAll("LCCN_ID", lccn.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

}
