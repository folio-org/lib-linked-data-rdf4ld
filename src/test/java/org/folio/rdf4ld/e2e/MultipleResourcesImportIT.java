package org.folio.rdf4ld.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.getTitleLabel;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
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
class MultipleResourcesImportIT {

  private static final String BASE_LINK = "http://test-tobe-changed.folio.com/resources/";
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedMultipleTopResources() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/multiple_top_resources.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(5);

    var instance1 = result.stream().filter(r -> r.getLabel().contains("Instance1")).findFirst().orElse(null);
    assertThat(instance1).isNotNull();
    assertResource(instance1, "Instance1 ", getTitleLabel("Instance1 ", "Title"), INSTANCE,
      "INSTANCE_1_ID", 4);

    var instance2 = result.stream().filter(r -> r.getLabel().contains("Instance2")).findFirst().orElse(null);
    assertThat(instance2).isNotNull();
    assertResource(instance2, "Instance2 ", getTitleLabel("Instance2 ", "Title"), INSTANCE,
      "INSTANCE_2_ID", 3);

    var hub1 = result.stream().filter(r -> r.getLabel().contains("Hub1")).findFirst().orElse(null);
    assertThat(hub1).isNotNull();
    assertResource(hub1, "Hub1 ", "Hub1 AAP", HUB, "HUB_1_ID", 3);

    var hub2 = result.stream().filter(r -> r.getLabel().contains("Hub2")).findFirst().orElse(null);
    assertThat(hub2).isNotNull();
    assertResource(hub2, "Hub2 ", "Hub2 AAP", HUB, "HUB_2_ID", 3);

    var work = result.stream().filter(r -> r.getLabel().contains("Work")).findFirst().orElse(null);
    assertThat(work).isNotNull();
    assertResource(work, "Work ", getTitleLabel("Work ", "Title"), WORK, "WORK_ID", 3);
  }

  private void assertResource(Resource resource,
                              String prefix,
                              String label,
                              ResourceTypeDictionary type,
                              String expectedLink,
                              int expectedEdges) {
    assertThat(resource.getDoc()).isNotNull();
    assertThat(resource.isOfType(type)).isTrue();
    assertThat(resource.getLabel()).isEqualTo(label);
    validateResourceWithTitles(resource, prefix, BASE_LINK + expectedLink);
    assertThat(resource.getOutgoingEdges()).hasSize(expectedEdges);
  }

}
