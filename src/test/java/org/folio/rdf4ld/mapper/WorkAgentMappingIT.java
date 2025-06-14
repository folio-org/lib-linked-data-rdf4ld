package org.folio.rdf4ld.mapper;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.COLLABORATOR;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.EDITOR;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkAgentMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;
  @MockitoBean
  private Function<String, Optional<Resource>> resourceProvider;

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/work_agent.json",
    "/rdf/work_agent_id.json"
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
    when(resourceProvider.apply(anyString()))
      .thenAnswer(inv -> ofNullable(foundByLccnResources.get(inv.getArgument(0, String.class))));

    // when
    var result = rdf4LdMapper.mapToLdInstance(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK), Map.of(), "",
      work -> validateResourceWithGivenEdges(work,
        new ResourceEdge(work, creator, CREATOR),
        new ResourceEdge(work, creator, AUTHOR),
        new ResourceEdge(work, creator, EDITOR),
        new ResourceEdge(work, contributor, CONTRIBUTOR),
        new ResourceEdge(work, contributor, ILLUSTRATOR),
        new ResourceEdge(work, contributor, COLLABORATOR)
      ));
  }

}
