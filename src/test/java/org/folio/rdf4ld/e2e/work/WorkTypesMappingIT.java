package org.folio.rdf4ld.e2e.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
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
class WorkTypesMappingIT {

  private static final String BASE_PATH = "/rdf/work/types/";
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithNotAddedDefaultType_ifNoDefaultWorkTypeProvider()
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_no_extra_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).isEmpty();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getTypes()).isEqualTo(Set.of(WORK));
  }

  @Test
  void mapBibframe2RdfToLd_shouldDiscardRdf_ifNotSupportedWorkExtraType() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_unsupported_extra_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    assertThatThrownBy(() -> rdf4LdMapper.mapBibframe2RdfToLd(model))
      //then
      .isInstanceOf(Rdf2LdMappingException.class);
  }

  @ParameterizedTest
  @MethodSource("importProfileArgs")
  void mapBibframe2RdfToLd_shouldReturnMappedWorkPerProfile(
    String fixture, ResourceTypeDictionary expectedWorkType) throws IOException {
    // given
    var input = getClass().getResourceAsStream(BASE_PATH + fixture);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).isEmpty();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getTypes()).isEqualTo(Set.of(WORK, expectedWorkType));
  }

  static Stream<Arguments> importProfileArgs() {
    return Stream.of(
      Arguments.of("work_books.json", BOOKS),
      Arguments.of("work_serial.json", CONTINUING_RESOURCES)
    );
  }

}
