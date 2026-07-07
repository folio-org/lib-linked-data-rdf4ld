package org.folio.rdf4ld.e2e.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.rdf4ld.test.TestUtil.getTitleLabel;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
import java.util.List;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
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
class WorkInstanceMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedWorkWithInstance() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/work_instance.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getLabel()).isEqualTo(getTitleLabel("Work ", "Title"));
    validateResourceWithTitles(work, "Work ", "http://test-tobe-changed.folio.com/resources/WORK_ID");
    assertThat(work.getOutgoingEdges()).hasSize(3);
    assertThat(work.getIncomingEdges()).hasSize(1);

    var instance = work.getIncomingEdges().iterator().next().getSource();
    assertThat(instance.getDoc()).isNotNull();
    validateProperty(instance.getDoc(), DIMENSIONS.getValue(),
      List.of("Instance dimensions 1", "Instance dimensions 2"));
    validateProperty(instance.getDoc(), STATEMENT_OF_RESPONSIBILITY.getValue(),
      List.of("Instance responsibilityStatement 1", "Instance responsibilityStatement 2")
    );
    assertThat(instance.getLabel()).isEqualTo(getTitleLabel("", "Title"));
    validateResourceWithTitles(instance, "", "http://test-tobe-changed.folio.com/resources/INSTANCE_ID");
    assertThat(instance.getOutgoingEdges()).hasSize(3);
  }

}
