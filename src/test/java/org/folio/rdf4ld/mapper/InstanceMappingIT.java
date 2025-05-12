package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.rdf4ld.test.MonographUtil.getSampleInstanceResource;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;

import java.io.IOException;
import java.util.List;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class InstanceMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapToLd_Instance_shouldReturnMappedInstance() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapToLdInstance(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getDoc()).isNotNull();
    validateProperty(instance.getDoc(), DIMENSIONS.getValue(),
      List.of("Instance dimensions 1", "Instance dimensions 2"));
    validateProperty(instance.getDoc(), STATEMENT_OF_RESPONSIBILITY.getValue(),
      List.of("Instance responsibilityStatement 1", "Instance responsibilityStatement 2")
    );
    assertThat(instance.getLabel()).isEqualTo("Title mainTitle, Title subtitle");
  }

  @Test
  void mapToBibframeRdf_shouldReturnMappedRdfInstance() {
    // given
    var instance = getSampleInstanceResource();

    // when
    var model = rdf4LdMapper.mapToBibframeRdfInstance(instance);

    //then
    assertThat(model).hasSizeGreaterThan(0);
  }

}
