package org.folio.rdf4ld.mapper;

import static java.util.Optional.ofNullable;
import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.COLLABORATOR;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.DEGREE_GRANTOR;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
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

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgents_withCurrentLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_agent_lccn.json");
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
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

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
        new ResourceEdge(work, creator, DEGREE_GRANTOR),
        new ResourceEdge(work, contributor, CONTRIBUTOR),
        new ResourceEdge(work, contributor, ILLUSTRATOR),
        new ResourceEdge(work, contributor, COLLABORATOR)
      ));
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgents_withNoCurrentLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_agent_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    var creatorLabel = "Creator Agent";
    var contributorLabel = "Contributor Agent";
    validateOutgoingEdge(instance, INSTANTIATES, of(WORK), Map.of(), "",
      work -> {
        assertThat(work.getId()).isNotNull();
        assertThat(work.getIncomingEdges()).isEmpty();
        assertThat(work.getOutgoingEdges()).hasSize(6);
        validateAgent(work, creatorLabel, CREATOR, PERSON);
        validateOutgoingEdge(work, AUTHOR, of(PERSON), Map.of(LABEL, List.of(creatorLabel)), creatorLabel);
        validateOutgoingEdge(work, DEGREE_GRANTOR, of(PERSON), Map.of(LABEL, List.of(creatorLabel)), creatorLabel);
        validateAgent(work, contributorLabel, CONTRIBUTOR, FAMILY);
        validateOutgoingEdge(work, ILLUSTRATOR, of(FAMILY), Map.of(LABEL, List.of(contributorLabel)), contributorLabel);
        validateOutgoingEdge(work, COLLABORATOR, of(FAMILY), Map.of(LABEL, List.of(contributorLabel)),
          contributorLabel);
      });
  }

  private void validateAgent(Resource work,
                             String agentLabel,
                             PredicateDictionary predicate,
                             ResourceTypeDictionary type) {
    validateOutgoingEdge(work, predicate, of(type), Map.of(LABEL, List.of(agentLabel)), agentLabel,
      agent -> {
        assertThat(agent.getId()).isNotNull();
        assertThat(agent.getIncomingEdges()).isEmpty();
        assertThat(agent.getOutgoingEdges()).isEmpty();
      }
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/work_agent_lccn.json",
    "/rdf/work_agent_no_lccn.json"
  })
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithAgents(String rdfFile) throws IOException {
    // given
    var work = createWork("work");
    var isCurrent = !rdfFile.contains("no_lccn");
    var creator = createAgent("n2021004098", isCurrent, List.of(PERSON), "Creator Agent");
    var contributor = createAgent("n2021004092", isCurrent, List.of(FAMILY), "Contributor Agent");
    work.addOutgoingEdge(new ResourceEdge(work, creator, CREATOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, AUTHOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, DEGREE_GRANTOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, ILLUSTRATOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, COLLABORATOR));
    var instance = createInstance("instance").setDoc(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream(rdfFile).readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("CREATOR_ID", "_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "_" + contributor.getId().toString())
      .replaceAll("CREATOR_AGENT_ID", "_" + creator.getId().toString() + "_agent")
      .replaceAll("CONTRIBUTOR_AGENT_ID", "_" + contributor.getId().toString() + "_agent");

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
