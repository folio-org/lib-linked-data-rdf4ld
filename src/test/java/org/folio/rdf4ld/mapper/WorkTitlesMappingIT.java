package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.getTitleLabel;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
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
class WorkTitlesMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapToLdInstance_shouldReturnMappedInstanceWithWorkWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_titles.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    validateResourceWithTitles(instance, "");
    assertThat(instance.getOutgoingEdges()).hasSize(4);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK),
      Map.of(), getTitleLabel("Work ", "Title"), r -> validateResourceWithTitles(r, "Work ")
    );
  }

}
