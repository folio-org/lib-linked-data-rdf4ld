package org.folio.rdf4ld.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class Rdf4LdServiceIT {
  @Autowired
  private Rdf4LdService rdf4LdService;

  @ParameterizedTest
  @MethodSource("exportProfileArgs")
  void mapLdToBibframe2Rdf_returnsSerializedModel_forStringInput(String fixture,
                                                                  String expectedBibframeType)
    throws IOException {
    // given
    var input = new String(getClass().getResourceAsStream(fixture).readAllBytes());

    // when
    var result = rdf4LdService.mapLdToBibframe2Rdf(input, RDFFormat.JSONLD);

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThan(0);
    assertThat(result.toString()).contains(expectedBibframeType);
    assertThat(result.toString().lines().count()).isGreaterThan(1);
  }

  @ParameterizedTest
  @MethodSource("exportProfileArgs")
  void mapLdToBibframe2Rdf_returnsSerializedModelWithoutPrettyPrinting_forStringInput(
    String fixture, String expectedBibframeType) throws IOException {
    // given
    var input = new String(getClass().getResourceAsStream(fixture).readAllBytes());
    var outputConfig = new WriterConfig();
    outputConfig.set(BasicWriterSettings.PRETTY_PRINT, false);

    // when
    var result = rdf4LdService.mapLdToBibframe2Rdf(input, RDFFormat.JSONLD, outputConfig);

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThan(0);
    assertThat(result.toString()).contains(expectedBibframeType);
    assertThat(result.toString().lines().count()).isEqualTo(1);
  }

  static Stream<Arguments> exportProfileArgs() {
    return Stream.of(
      Arguments.of("/rdf/exported_books.json", "http://id.loc.gov/ontologies/bibframe/Monograph"),
      Arguments.of("/rdf/exported_serial.json", "http://id.loc.gov/ontologies/bibframe/Serial")
    );
  }
}
