package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
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
class InstanceTitlesMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapToLdInstance_shouldReturnMappedInstanceWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_titles.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapToLdInstance(model, null);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    validateResourceWithTitles(instance, "");
    assertThat(instance.getOutgoingEdges()).hasSize(3);
  }

}
