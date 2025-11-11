package org.folio.rdf4ld.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class Rdf4LdServiceIT {
  @Autowired
  private Rdf4LdService rdf4LdService;

  @Test
  void mapLdToBibframe2Rdf_throwsForInvalidJson() {
    // given
    var input = "{";
    var format = RDFFormat.JSONLD;

    // when
    assertThatThrownBy(() -> rdf4LdService.mapLdToBibframe2Rdf(input, format))
      // then
      .isInstanceOf(JsonProcessingException.class);
  }

  @Test
  @SneakyThrows
  void mapLdToBibframe2Rdf_returnsSerializedModel_forStringInput() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/exported.json");
    var input = new String(inputStream.readAllBytes());
    var format = RDFFormat.JSONLD;

    // when
    var result = rdf4LdService.mapLdToBibframe2Rdf(input, format);

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThan(0);
    assertThat(result.toString().lines().count()).isGreaterThan(1);
  }

  @Test
  @SneakyThrows
  void mapLdToBibframe2Rdf_returnsSerializedModelWithoutPrettyPrinting_forStringInput() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/exported.json");
    var input = new String(inputStream.readAllBytes());
    var format = RDFFormat.JSONLD;
    var outputConfig = new WriterConfig();
    outputConfig.set(BasicWriterSettings.PRETTY_PRINT, false);

    // when
    var result = rdf4LdService.mapLdToBibframe2Rdf(input, format, outputConfig);

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThan(0);
    assertThat(result.toString().lines().count()).isEqualTo(1);
  }
}
