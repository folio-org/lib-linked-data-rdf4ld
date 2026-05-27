package org.folio.rdf4ld.e2e;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.getTitleLabel;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
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
class MultipleResourcesImportIT {

  private static final Map<PropertyDictionary, List<String>> EXPECTED_WORK_PROPERTIES =
    Map.of(LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstancesAndHubs() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/multiple_instances_and_hubs.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(4);
    result.forEach(r -> {
      if (r.getLabel().contains("Instance1")) {
        assertResource(r, "Instance1 ", getTitleLabel("Instance1 ", "Title"), INSTANCE,
          "http://test-tobe-changed.folio.com/resources/INSTANCE_1_ID");
      } else if (r.getLabel().contains("Instance2")) {
        assertResource(r, "Instance2 ", getTitleLabel("Instance2 ", "Title"), INSTANCE,
          "http://test-tobe-changed.folio.com/resources/INSTANCE_2_ID");
      } else if (r.getLabel().contains("Hub1")) {
        assertResource(r, "Hub1 ", "Hub1 AAP", HUB,
          "http://test-tobe-changed.folio.com/resources/HUB_1_ID");
      } else if (r.getLabel().contains("Hub2")) {
        assertResource(r, "Hub2 ", "Hub2 AAP", HUB,
          "http://test-tobe-changed.folio.com/resources/HUB_2_ID");
      } else {
        fail("Unexpected resource");
      }
    });
  }

  @ParameterizedTest
  @MethodSource("importProfileArgs")
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkPerProfile(
    String fixture, ResourceTypeDictionary expectedWorkType) throws IOException {
    // given
    var input = getClass().getResourceAsStream(fixture);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    validateOutgoingEdge(instance, INSTANTIATES, of(WORK, expectedWorkType), EXPECTED_WORK_PROPERTIES, "");
  }

  static Stream<Arguments> importProfileArgs() {
    return Stream.of(
      Arguments.of("/rdf/work_books.json", BOOKS),
      Arguments.of("/rdf/work_serial.json", CONTINUING_RESOURCES)
    );
  }

  private static void assertResource(Resource resource,
                                     String prefix,
                                     String label,
                                     ResourceTypeDictionary type,
                                     String expectedLink) {
    assertThat(resource.getDoc()).isNotNull();
    assertThat(resource.isOfType(type)).isTrue();
    assertThat(resource.getLabel()).isEqualTo(label);
    validateResourceWithTitles(resource, prefix, expectedLink);
    assertThat(resource.getOutgoingEdges()).hasSize(3);
  }

}
