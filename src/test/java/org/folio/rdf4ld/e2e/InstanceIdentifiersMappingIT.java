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
import static org.folio.rdf4ld.test.MonographUtil.createResource;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class InstanceIdentifiersMappingIT {

  private static final String EXPECTED_EAN = "780696204364";
  private static final String EXPECTED_ISBN = "0850598370";
  private static final String EXPECTED_ISBN_2 = "9780850598377";
  private static final String EXPECTED_LCCN = "  2010470075";
  private static final String EXPECTED_LCCN_2 = "  2019470076";

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithIdentifiers() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance/instance_identifiers.json");
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
    var expected = new String(this.getClass().getResourceAsStream("/rdf/instance/instance_identifiers.json").readAllBytes())
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

  @ParameterizedTest
  @MethodSource("identifierStatusArgs")
  void mapBibframe2RdfToLd_shouldMapIdentifierWithStatus(
    String fixturePath, ResourceTypeDictionary idType, String value,
    String statusLabel, boolean isCurrent, String idPlaceholder) throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(fixturePath);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    var expectedProps = idType == ID_LCCN
      ? Map.of(NAME, List.of(value))
      : Map.of(NAME, List.of(value), QUALIFIER, List.of("pbk"));
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, idType),
      expectedProps, value,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(statusLabel), LINK, List.of(STATUS_BASE_URI + statusLabel)), statusLabel)
    );
  }

  @ParameterizedTest
  @MethodSource("identifierStatusArgs")
  void mapLdToBibframe2Rdf_shouldMapIdentifierWithStatus(
    String fixturePath, ResourceTypeDictionary idType, String value,
    String statusLabel, boolean isCurrent, String idPlaceholder) throws IOException {
    // given
    var identifier = idType == ID_LCCN
      ? createIdentifier(value, ID_LCCN, "", isCurrent)
      : createIsbn(value, isCurrent);
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, identifier, MAP));
    var expected = new String(this.getClass().getResourceAsStream(fixturePath).readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll(idPlaceholder, identifier.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    assertThat(toJsonLdString(model)).isEqualTo(expected);
  }

  @Test
  void mapBibframe2RdfToLd_shouldMapMultipleIdentifiersOfSameType() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance/instance_identifiers_multi_same_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getOutgoingEdges()).hasSize(4);
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_LCCN),
      Map.of(NAME, List.of(EXPECTED_LCCN)), EXPECTED_LCCN,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(STATUS_CURRENT), LINK, List.of(STATUS_BASE_URI + STATUS_CURRENT)), STATUS_CURRENT)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_LCCN),
      Map.of(NAME, List.of(EXPECTED_LCCN_2)), EXPECTED_LCCN_2,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(STATUS_CANCELLED), LINK, List.of(STATUS_BASE_URI + STATUS_CANCELLED)), STATUS_CANCELLED)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_ISBN),
      Map.of(NAME, List.of(EXPECTED_ISBN), QUALIFIER, List.of("pbk")), EXPECTED_ISBN,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(STATUS_CURRENT), LINK, List.of(STATUS_BASE_URI + STATUS_CURRENT)), STATUS_CURRENT)
    );
    validateOutgoingEdge(instance, MAP, Set.of(IDENTIFIER, ID_ISBN),
      Map.of(NAME, List.of(EXPECTED_ISBN_2), QUALIFIER, List.of("pbk")), EXPECTED_ISBN_2,
      identifier -> validateOutgoingEdge(identifier, PredicateDictionary.STATUS, Set.of(STATUS),
        Map.of(LABEL, List.of(STATUS_CANCELLED), LINK, List.of(STATUS_BASE_URI + STATUS_CANCELLED)), STATUS_CANCELLED)
    );
  }

  @Test
  void mapLdToBibframe2Rdf_shouldMapMultipleIdentifiersOfSameType() throws IOException {
    // given
    var lccn1 = createIdentifier(EXPECTED_LCCN, ID_LCCN, "", true);
    var lccn2 = createIdentifier(EXPECTED_LCCN_2, ID_LCCN, "", false);
    var isbn1 = createIsbn(EXPECTED_ISBN, true);
    var isbn2 = createIsbn(EXPECTED_ISBN_2, false);
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, lccn1, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, lccn2, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, isbn1, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, isbn2, MAP));
    var expected = new String(
      this.getClass().getResourceAsStream("/rdf/instance/instance_identifiers_multi_same_type.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("LCCN1_ID", lccn1.getId().toString())
      .replaceAll("LCCN2_ID", lccn2.getId().toString())
      .replaceAll("ISBN1_ID", isbn1.getId().toString())
      .replaceAll("ISBN2_ID", isbn2.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    assertThat(toJsonLdString(model)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("isbnExportArgs")
  void mapLdToBibframe2Rdf_shouldMapIsbnQualifierAndStatusPermutations(
    String isbn, String qualifier, String statusLabel, boolean isCurrent) {
    // given
    var isbnResource = createIsbnVariant(isbn, qualifier, isCurrent);
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, isbnResource, MAP));

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString)
      .contains("http://id.loc.gov/ontologies/bibframe/Isbn")
      .contains(isbn)
      .contains(qualifier)
      .contains(STATUS_BASE_URI + statusLabel);
  }

  @ParameterizedTest
  @MethodSource("eanExportArgs")
  void mapLdToBibframe2Rdf_shouldMapEanValueAndQualifierPermutations(
    String ean, String qualifier) {
    // given
    var eanResource = createEanVariant(ean, qualifier);
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, eanResource, MAP));

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString)
      .contains("http://id.loc.gov/ontologies/bibframe/Ean")
      .contains(ean);
    if (qualifier == null) {
      assertThat(jsonLdString).doesNotContain("http://id.loc.gov/ontologies/bibframe/qualifier");
    } else {
      assertThat(jsonLdString)
        .contains("http://id.loc.gov/ontologies/bibframe/qualifier")
        .contains(qualifier);
    }
  }

  private static Resource createIsbnVariant(String isbn, String qualifier, boolean isCurrent) {
    return createResource(
      Map.of(NAME, List.of(isbn), QUALIFIER, List.of(qualifier)),
      Set.of(IDENTIFIER, ID_ISBN),
      Map.of(PredicateDictionary.STATUS, List.of(createStatusResource(isCurrent)))
    );
  }

  private static Resource createEanVariant(String ean, String qualifier) {
    var properties = qualifier == null
      ? Map.of(NAME, List.of(ean))
      : Map.of(NAME, List.of(ean), QUALIFIER, List.of(qualifier));
    return createResource(
      properties,
      Set.of(IDENTIFIER, ID_IAN),
      Map.of()
    );
  }

  private static Resource createStatusResource(boolean isCurrent) {
    var status = isCurrent ? STATUS_CURRENT : STATUS_CANCELLED;
    return createResource(
      Map.of(
        PropertyDictionary.LABEL, List.of(status),
        LINK, List.of(STATUS_BASE_URI + status)
      ),
      Set.of(ResourceTypeDictionary.STATUS),
      Map.of()
    );
  }

  static Stream<Arguments> identifierStatusArgs() {
    return Stream.of(
      Arguments.of("/rdf/instance/instance_identifier_lccn_current.json",
        ID_LCCN, EXPECTED_LCCN, STATUS_CURRENT, true, "LCCN_ID"),
      Arguments.of("/rdf/instance/instance_identifier_lccn_cancelled.json",
        ID_LCCN, EXPECTED_LCCN, STATUS_CANCELLED, false, "LCCN_ID"),
      Arguments.of("/rdf/instance/instance_identifier_isbn_current.json",
        ID_ISBN, EXPECTED_ISBN, STATUS_CURRENT, true, "ISBN_ID"),
      Arguments.of("/rdf/instance/instance_identifier_isbn_cancelled.json",
        ID_ISBN, EXPECTED_ISBN, STATUS_CANCELLED, false, "ISBN_ID")
    );
  }

  static Stream<Arguments> isbnExportArgs() {
    return Stream.of(
      Arguments.of(EXPECTED_ISBN, "pbk", STATUS_CURRENT, true),
      Arguments.of(EXPECTED_ISBN_2, "pbk", STATUS_CANCELLED, false),
      Arguments.of("9781111111111", "hbk", STATUS_CURRENT, true),
      Arguments.of("9782222222222", "hbk", STATUS_CANCELLED, false)
    );
  }

  static Stream<Arguments> eanExportArgs() {
    return Stream.of(
      Arguments.of(EXPECTED_EAN, "abc"),
      Arguments.of("9783007601470", "distribution"),
      Arguments.of("9772049364017", null)
    );
  }
}
