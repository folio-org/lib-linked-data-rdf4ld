package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkAgentsMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/instance_work_agents.json",
    "/rdf/instance_work_agents_ids.json"
  })
  void mapToLdInstance_shouldReturnMappedInstanceWithWorkWithAgents(String rdfFile) throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(rdfFile);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var creator = new Resource().setId(1L).setLabel("creator");
    var contributor = new Resource().setId(2L).setLabel("contributor");
    var foundByLccnResources = Map.of(
      "n2021004098", creator,
      "n2021004092", contributor
    );

    // when
    var result = rdf4LdMapper.mapToLdInstance(model, key -> Optional.of(foundByLccnResources.get(key)));

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK), Map.of(), "",
      work -> validateResourceWithGivenEdges(work,
        new ResourceEdge(work, creator, CREATOR),
        new ResourceEdge(work, contributor, CONTRIBUTOR)
      ));
  }

}
