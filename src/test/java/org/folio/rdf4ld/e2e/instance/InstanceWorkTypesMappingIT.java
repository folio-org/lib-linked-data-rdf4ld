package org.folio.rdf4ld.e2e.instance;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.rdf4ld.mapper.Rdf2LdMappingException;
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
class InstanceWorkTypesMappingIT {

  private static final String BASE_PATH = "/rdf/instance/work/types/";
  private static final Map<PropertyDictionary, List<String>> EXPECTED_WORK_PROPERTIES =
    Map.of(PropertyDictionary.LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithNotAddedDefaultType_ifNoDefaultWorkTypeProvider()
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "instance_work_no_extra_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK), EXPECTED_WORK_PROPERTIES, "",
      work -> assertThat(work.getId()).isNotNull()
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldDiscardRdf_ifNotSupportedWorkExtraType() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "instance_work_unsupported_extra_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    assertThatThrownBy(() -> rdf4LdMapper.mapBibframe2RdfToLd(model))
      //then
      .isInstanceOf(Rdf2LdMappingException.class);
  }

  @ParameterizedTest
  @MethodSource("importProfileArgs")
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkPerProfile(
    String fixture, ResourceTypeDictionary expectedWorkType) throws IOException {
    // given
    var input = getClass().getResourceAsStream(BASE_PATH + fixture);
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
      Arguments.of("instance_work_books.json", BOOKS),
      Arguments.of("instance_work_serial.json", CONTINUING_RESOURCES)
    );
  }

}
