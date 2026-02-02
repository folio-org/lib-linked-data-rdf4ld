package org.folio.rdf4ld.e2e;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.COLLABORATOR;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATOR;
import static org.folio.ld.dictionary.PredicateDictionary.PUBLISHING_DIRECTOR;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createHub;
import static org.folio.rdf4ld.test.TestUtil.mockLccnResource;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateAgent;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class HubAgentMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedHubWithAgentMocks() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/hub_agent_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var hub = result.iterator().next();
    assertThat(hub.getId()).isNotNull();
    assertThat(hub.getIncomingEdges()).isEmpty();
    assertThat(hub.getOutgoingEdges()).hasSize(6);
    var creator = mockLccnResource("n2021004098");
    var contributor = mockLccnResource("n2021004092");
    validateResourceWithGivenEdges(hub,
      new ResourceEdge(hub, creator, CREATOR),
      new ResourceEdge(hub, creator, AUTHOR),
      new ResourceEdge(hub, creator, PUBLISHING_DIRECTOR),
      new ResourceEdge(hub, contributor, CONTRIBUTOR),
      new ResourceEdge(hub, contributor, ILLUSTRATOR),
      new ResourceEdge(hub, contributor, COLLABORATOR)
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedHubWithAgents_withLccnWithBodies() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/hub_agent_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var hub = result.iterator().next();
    assertThat(hub.getId()).isNotNull();
    assertThat(hub.getIncomingEdges()).isEmpty();
    assertThat(hub.getOutgoingEdges()).hasSize(6);
    var creatorLabel = "Creator Agent";
    var creatorMockLabel = "n2021004098";
    validateAgent(hub, creatorLabel, creatorMockLabel, CREATOR, of(PERSON, MOCKED_RESOURCE));
    var expectedCreatorProperties = Map.of(
      LABEL, List.of(creatorLabel),
      NAME, List.of(creatorLabel)
    );
    validateOutgoingEdge(hub, AUTHOR, of(PERSON, MOCKED_RESOURCE), expectedCreatorProperties, creatorMockLabel);
    validateOutgoingEdge(hub, PUBLISHING_DIRECTOR, of(PERSON, MOCKED_RESOURCE), expectedCreatorProperties,
      creatorMockLabel);
    var contributorLabel = "Contributor Agent";
    var contributorMockLabel = "n2021004092";
    validateAgent(hub, contributorLabel, contributorMockLabel, CONTRIBUTOR, of(FAMILY, MOCKED_RESOURCE));
    var expectedContributorProperties = Map.of(
      LABEL, List.of(contributorLabel),
      NAME, List.of(contributorLabel)
    );
    validateOutgoingEdge(hub, ILLUSTRATOR, of(FAMILY, MOCKED_RESOURCE), expectedContributorProperties,
      contributorMockLabel);
    validateOutgoingEdge(hub, COLLABORATOR, of(FAMILY, MOCKED_RESOURCE), expectedContributorProperties,
      contributorMockLabel);
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedHubWithAgents_withNoCurrentLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/hub_agent_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var hub = result.iterator().next();
    assertThat(hub.getId()).isNotNull();
    assertThat(hub.getIncomingEdges()).isEmpty();
    assertThat(hub.getOutgoingEdges()).hasSize(6);
    var creatorLabel = "Creator Agent";
    validateAgent(hub, creatorLabel, creatorLabel, CREATOR, of(PERSON));
    var expectedCreatorProperties = Map.of(
      LABEL, List.of(creatorLabel),
      NAME, List.of(creatorLabel)
    );
    validateOutgoingEdge(hub, AUTHOR, of(PERSON), expectedCreatorProperties, creatorLabel);
    validateOutgoingEdge(hub, PUBLISHING_DIRECTOR, of(PERSON), expectedCreatorProperties, creatorLabel);
    var contributorLabel = "Contributor Agent";
    validateAgent(hub, contributorLabel, contributorLabel, CONTRIBUTOR, of(FAMILY));
    var expectedContributorProperties = Map.of(
      LABEL, List.of(contributorLabel),
      NAME, List.of(contributorLabel)
    );
    validateOutgoingEdge(hub, ILLUSTRATOR, of(FAMILY), expectedContributorProperties, contributorLabel);
    validateOutgoingEdge(hub, COLLABORATOR, of(FAMILY), expectedContributorProperties, contributorLabel);
  }


  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/hub_agent_lccn.json",
    "/rdf/hub_agent_no_lccn.json"
  })
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfHubWithAgents(String rdfFile) throws IOException {
    // given
    var hub = createHub(Map.of());
    var isCurrent = !rdfFile.contains("no_lccn");
    var creator = createAgent("n2021004098", ID_LCNAF, isCurrent, List.of(PERSON), "Creator Agent");
    var contributor = createAgent("n2021004092", ID_LCNAF, isCurrent, List.of(FAMILY), "Contributor Agent");
    hub.addOutgoingEdge(new ResourceEdge(hub, creator, CREATOR));
    hub.addOutgoingEdge(new ResourceEdge(hub, creator, AUTHOR));
    hub.addOutgoingEdge(new ResourceEdge(hub, creator, PUBLISHING_DIRECTOR));
    hub.addOutgoingEdge(new ResourceEdge(hub, contributor, CONTRIBUTOR));
    hub.addOutgoingEdge(new ResourceEdge(hub, contributor, ILLUSTRATOR));
    hub.addOutgoingEdge(new ResourceEdge(hub, contributor, COLLABORATOR));
    var expected = new String(this.getClass().getResourceAsStream(rdfFile).readAllBytes())
      .replaceAll("HUB_ID", hub.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + contributor.getId().toString())
      .replaceAll("CREATOR_AGENT_ID", "CREATOR_" + creator.getId().toString() + "_agent")
      .replaceAll("CONTRIBUTOR_AGENT_ID", "CONTRIBUTOR_" + contributor.getId().toString() + "_agent");

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(hub);

    //then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnHubWithSameAgentAsCreatorAndContributorCorrectly() throws IOException {
    // given
    var hub = createHub(Map.of());
    var creator = createAgent("n2021004098", ID_LCNAF, true, List.of(PERSON), "Creator Agent");
    hub.addOutgoingEdge(new ResourceEdge(hub, creator, CREATOR));
    hub.addOutgoingEdge(new ResourceEdge(hub, creator, CONTRIBUTOR));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/hub_agent_as_creator_and_contributor.json")
      .readAllBytes())
      .replaceAll("HUB_ID", hub.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + creator.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(hub);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
