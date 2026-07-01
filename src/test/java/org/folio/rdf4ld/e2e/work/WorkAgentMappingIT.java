package org.folio.rdf4ld.e2e.work;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.COLLABORATOR;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATOR;
import static org.folio.ld.dictionary.PredicateDictionary.ISSUING_BODY;
import static org.folio.ld.dictionary.PredicateDictionary.PUBLISHING_DIRECTOR;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.mockLccnResource;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateAgent;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkAgentMappingIT {
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgentMocks() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var creator = mockLccnResource("n2021004098");
    var contributor = mockLccnResource("n2021004092");
    validateResourceWithGivenEdges(work,
        new ResourceEdge(work, creator, CREATOR),
        new ResourceEdge(work, creator, AUTHOR),
        new ResourceEdge(work, creator, PUBLISHING_DIRECTOR),
        new ResourceEdge(work, contributor, CONTRIBUTOR),
        new ResourceEdge(work, contributor, ILLUSTRATOR),
        new ResourceEdge(work, contributor, COLLABORATOR)
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgents_withLccnWithBodies() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(6);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var creatorLabel = "Creator Agent";
    var creatorMockLabel = "n2021004098";
    validateAgent(work, creatorLabel, creatorMockLabel, CREATOR, of(PERSON, MOCKED_RESOURCE));
    var creatorProperties = Map.of(
      LABEL, List.of(creatorLabel),
      NAME, List.of(creatorLabel)
    );
    validateOutgoingEdge(work, AUTHOR, of(PERSON, MOCKED_RESOURCE), creatorProperties, creatorMockLabel);
    validateOutgoingEdge(work, PUBLISHING_DIRECTOR, of(PERSON, MOCKED_RESOURCE), creatorProperties, creatorMockLabel);
    var contributorLabel = "Contributor Agent";
    var contributorMockLabel = "n2021004092";
    validateAgent(work, contributorLabel, contributorMockLabel, CONTRIBUTOR, of(FAMILY, MOCKED_RESOURCE));
    var contributorProperties = Map.of(
      LABEL, List.of(contributorLabel),
      NAME, List.of(contributorLabel)
    );
    validateOutgoingEdge(work, ILLUSTRATOR, of(FAMILY, MOCKED_RESOURCE), contributorProperties, contributorMockLabel);
    validateOutgoingEdge(work, COLLABORATOR, of(FAMILY, MOCKED_RESOURCE), contributorProperties, contributorMockLabel);
  }

  @Test
  void mapBibframe2RdfToLd_shouldMapRdfsLabelToNameAndAuthoritativeLabelToLabel() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_label_name_distinction.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(2);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var creatorProperties = Map.of(
      LABEL, List.of("Creator Agent,", "Creator Agent"),
      NAME, List.of("Creator Agent,")
    );
    validateOutgoingEdge(work, CREATOR, of(PERSON, MOCKED_RESOURCE), creatorProperties, "n2021004098");
    validateOutgoingEdge(work, AUTHOR, of(PERSON, MOCKED_RESOURCE), creatorProperties, "n2021004098");
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgents_withNoCurrentLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(6);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var creatorLabel = "Creator Agent";
    validateAgent(work, creatorLabel, creatorLabel, CREATOR, of(PERSON));
    var creatorProperties = Map.of(
      LABEL, List.of(creatorLabel),
      NAME, List.of(creatorLabel)
    );
    validateOutgoingEdge(work, AUTHOR, of(PERSON), creatorProperties, creatorLabel);
    validateOutgoingEdge(work, PUBLISHING_DIRECTOR, of(PERSON), creatorProperties, creatorLabel);
    var contributorLabel = "Contributor Agent";
    validateAgent(work, contributorLabel, contributorLabel, CONTRIBUTOR, of(FAMILY));
    var contributorProperties = Map.of(
      LABEL, List.of(contributorLabel),
      NAME, List.of(contributorLabel)
    );
    validateOutgoingEdge(work, ILLUSTRATOR, of(FAMILY), contributorProperties, contributorLabel);
    validateOutgoingEdge(work, COLLABORATOR, of(FAMILY), contributorProperties, contributorLabel);
  }

  @ParameterizedTest
  @MethodSource("noLccnAgentTypeArgs")
  void mapBibframe2RdfToLd_shouldMapAgentTypeWithNoCurrentLccn(String rdfFile,
                                                               ResourceTypeDictionary agentType)
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(rdfFile);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getOutgoingEdges()).hasSize(6);
    var creatorLabel = "Creator Agent";
    validateAgent(work, creatorLabel, creatorLabel, CREATOR, of(agentType));
    var creatorProperties = Map.of(LABEL, List.of(creatorLabel), NAME, List.of(creatorLabel));
    validateOutgoingEdge(work, AUTHOR, of(agentType), creatorProperties, creatorLabel);
    validateOutgoingEdge(work, PUBLISHING_DIRECTOR, of(agentType), creatorProperties, creatorLabel);
    var contributorLabel = "Contributor Agent";
    validateAgent(work, contributorLabel, contributorLabel, CONTRIBUTOR, of(agentType));
    var contributorProperties = Map.of(LABEL, List.of(contributorLabel), NAME, List.of(contributorLabel));
    validateOutgoingEdge(work, ILLUSTRATOR, of(agentType), contributorProperties, contributorLabel);
    validateOutgoingEdge(work, COLLABORATOR, of(agentType), contributorProperties, contributorLabel);
  }

  @Test
  void mapBibframe2RdfToLd_shouldHandleUncontrolledRoleLabels() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_uncontrolled_role_label.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getOutgoingEdges()).hasSize(3);
    var creatorProperties = Map.of(
      LABEL, List.of("Creator Agent"),
      NAME, List.of("Creator Agent")
    );
    validateAgent(work, "Creator Agent", "Creator Agent", CREATOR, of(PERSON));
    validateOutgoingEdge(work, ISSUING_BODY, of(PERSON), creatorProperties, "Creator Agent");
    validateAgent(work, "Contributor Agent", "Contributor Agent", CONTRIBUTOR, of(FAMILY));
    var contributorProperties = Map.of(
      LABEL, List.of("Contributor Agent"),
      NAME, List.of("Contributor Agent")
    );
    validateOutgoingEdge(work, CONTRIBUTOR, of(FAMILY), contributorProperties, "Contributor Agent");
  }


  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgents_withLccnAndUncontrolledRoles()
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_lccn_uncontrolled_role_label.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(3);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var creatorLabel = "Creator Agent";
    var contributorLabel = "Contributor Agent";
    var creatorMockLabel = "n2021004098";
    var contributorMockLabel = "n2021004092";
    var creatorProperties = Map.of(LABEL, List.of(creatorLabel), NAME, List.of(creatorLabel));
    validateAgent(work, creatorLabel, creatorMockLabel, CREATOR, of(PERSON, MOCKED_RESOURCE));
    validateOutgoingEdge(work, ISSUING_BODY, of(PERSON, MOCKED_RESOURCE), creatorProperties, creatorMockLabel);
    validateAgent(work, contributorLabel, contributorMockLabel, CONTRIBUTOR, of(FAMILY, MOCKED_RESOURCE));
  }

  @ParameterizedTest
  @MethodSource("noLccnUncontrolledRoleAgentTypeArgs")
  void mapBibframe2RdfToLd_shouldMapAgentTypeWithNoLccnAndUncontrolledRole(String rdfFile,
                                                                           ResourceTypeDictionary agentType)
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(rdfFile);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getOutgoingEdges()).hasSize(3);
    var creatorLabel = "Creator Agent";
    validateAgent(work, creatorLabel, creatorLabel, CREATOR, of(agentType));
    var creatorProperties = Map.of(LABEL, List.of(creatorLabel), NAME, List.of(creatorLabel));
    validateOutgoingEdge(work, ISSUING_BODY, of(agentType), creatorProperties, creatorLabel);
    var contributorLabel = "Contributor Agent";
    validateAgent(work, contributorLabel, contributorLabel, CONTRIBUTOR, of(agentType));
  }

  @ParameterizedTest
  @MethodSource("contributionTypeScenarios")
  void mapBibframe2RdfToLd_shouldMapContributionTypeToCorrectPredicate(String rdfFile,
                                                                       PredicateDictionary expectedPredicate,
                                                                       String lccn,
                                                                       PredicateDictionary expectedRolePredicate)
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(rdfFile);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getIncomingEdges()).isEmpty();
    var agent = mockLccnResource(lccn);
    validateResourceWithGivenEdges(work,
      new ResourceEdge(work, agent, expectedPredicate),
      new ResourceEdge(work, agent, expectedRolePredicate)
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldMapAllRolesFromSingleContribution() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work/agent/work_agent_multi_role_single_contribution.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getIncomingEdges()).isEmpty();
    var agent = mockLccnResource("n2021004098");
    validateResourceWithGivenEdges(work,
      new ResourceEdge(work, agent, CREATOR),
      new ResourceEdge(work, agent, AUTHOR),
      new ResourceEdge(work, agent, PUBLISHING_DIRECTOR),
      new ResourceEdge(work, agent, ILLUSTRATOR)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/work/agent/work_agent_lccn.json",
    "/rdf/work/agent/work_agent_no_lccn.json"
  })
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithAgents(String rdfFile) throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var isCurrent = !rdfFile.contains("no_lccn");
    var creator = createAgent("n2021004098", ID_LCNAF, isCurrent, List.of(PERSON), "Creator Agent");
    var contributor = createAgent("n2021004092", ID_LCNAF, isCurrent, List.of(FAMILY), "Contributor Agent");
    work.addOutgoingEdge(new ResourceEdge(work, creator, CREATOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, AUTHOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, PUBLISHING_DIRECTOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, ILLUSTRATOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, COLLABORATOR));
    var expected = new String(this.getClass().getResourceAsStream(rdfFile).readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + contributor.getId().toString())
      .replaceAll("CREATOR_AGENT_ID", "CREATOR_" + creator.getId().toString() + "_agent")
      .replaceAll("CONTRIBUTOR_AGENT_ID", "CONTRIBUTOR_" + contributor.getId().toString() + "_agent");

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    //then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("noLccnAgentTypeArgs")
  void mapLdToBibframe2Rdf_shouldMapAgentTypeWithNoCurrentLccn(String rdfFile,
                                                               ResourceTypeDictionary agentType)
    throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var creator = createAgent("n2021004098", ID_LCNAF, false, List.of(agentType), "Creator Agent");
    var contributor = createAgent("n2021004092", ID_LCNAF, false, List.of(agentType), "Contributor Agent");
    work.addOutgoingEdge(new ResourceEdge(work, creator, CREATOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, AUTHOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, PUBLISHING_DIRECTOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, ILLUSTRATOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributor, COLLABORATOR));
    var expected = new String(this.getClass().getResourceAsStream(rdfFile).readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + contributor.getId().toString())
      .replaceAll("CREATOR_AGENT_ID", "CREATOR_" + creator.getId().toString() + "_agent")
      .replaceAll("CONTRIBUTOR_AGENT_ID", "CONTRIBUTOR_" + contributor.getId().toString() + "_agent");

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    assertThat(toJsonLdString(model)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("lccnAgentRwoUriScenarios")
  void mapLdToBibframe2Rdf_shouldMapAgentToRwoUri_whenAgentHasCurrentLccn(String lccn,
                                                                            PredicateDictionary predicate,
                                                                            List<ResourceTypeDictionary> agentTypes) {
    // given
    var work = createWork(Map.of(), BOOKS);
    var agent = createAgent(lccn, ID_LCNAF, true, agentTypes, lccn);
    work.addOutgoingEdge(new ResourceEdge(work, agent, predicate));

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    assertThat(toJsonLdString(model)).contains("http://id.loc.gov/rwo/agents/" + lccn);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnWorkWithSameAgentAsCreatorAndContributorCorrectly() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var creator = createAgent("n2021004098", ID_LCNAF, true, List.of(PERSON), "Creator Agent");
    work.addOutgoingEdge(new ResourceEdge(work, creator, CREATOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, CONTRIBUTOR));
    var expected = new String(this.getClass()
      .getResourceAsStream("/rdf/work/agent/work_agent_as_creator_and_contributor.json")
      .readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + creator.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  static Stream<Arguments> contributionTypeScenarios() {
    return Stream.of(
      Arguments.of("/rdf/work/agent/work_agent_single_primary_contribution.json", CREATOR, "n2021004098", AUTHOR),
      Arguments.of("/rdf/work/agent/work_agent_single_contribution.json", CONTRIBUTOR, "n2021004092", ILLUSTRATOR)
    );
  }

  static Stream<Arguments> lccnAgentRwoUriScenarios() {
    return Stream.of(
      Arguments.of("n2021004098", CREATOR, List.of(PERSON)),
      Arguments.of("n2021004092", CONTRIBUTOR, List.of(FAMILY))
    );
  }

  static Stream<Arguments> noLccnAgentTypeArgs() {
    return Stream.of(
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_person.json", PERSON),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_family.json", FAMILY),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_organization.json", ORGANIZATION),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_jurisdiction.json", JURISDICTION),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_meeting.json", MEETING)
    );
  }

  static Stream<Arguments> noLccnUncontrolledRoleAgentTypeArgs() {
    return Stream.of(
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_uncontrolled_role_person.json", PERSON),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_uncontrolled_role_family.json", FAMILY),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_uncontrolled_role_organization.json", ORGANIZATION),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_uncontrolled_role_jurisdiction.json", JURISDICTION),
      Arguments.of("/rdf/work/agent/work_agent_no_lccn_uncontrolled_role_meeting.json", MEETING)
    );
  }
}
