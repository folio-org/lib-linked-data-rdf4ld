package org.folio.rdf4ld.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.rdf4ld.mapper.Rdf2LdMappingException;
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
class WorkTypesMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSupportedExtraTypes() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_books.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS),
      Map.of(LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID")), "",
      work -> assertThat(work.getId()).isNotNull()
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAddedDefaultType_ifNoExtraTypes() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_no_extra_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS),
      Map.of(LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID")), "",
      work -> {
        assertThat(work.getId()).isNotNull();
      }
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldDiscardRdf_ifNotSupportedWorkExtraType() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_unsupported_extra_type.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    assertThatThrownBy(() -> rdf4LdMapper.mapBibframe2RdfToLd(model))
      //then
      .isInstanceOf(Rdf2LdMappingException.class);
  }

}
