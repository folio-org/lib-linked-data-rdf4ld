package org.folio.rdf4ld.e2e;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.COLLABORATOR;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.PUBLISHING_DIRECTOR;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.mockLccnResource;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
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
class WorkAgentMappingIT {
  private static final Map<PropertyDictionary, List<String>> EXPECTED_WORK_PROPERTIES = Map.of(
    LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID")
  );

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgentMocks() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_agent_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    var creator = mockLccnResource("n2021004098");
    var contributor = mockLccnResource("n2021004092");
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> validateResourceWithGivenEdges(work,
        new ResourceEdge(work, creator, CREATOR),
        new ResourceEdge(work, creator, AUTHOR),
        new ResourceEdge(work, creator, PUBLISHING_DIRECTOR),
        new ResourceEdge(work, contributor, CONTRIBUTOR),
        new ResourceEdge(work, contributor, ILLUSTRATOR),
        new ResourceEdge(work, contributor, COLLABORATOR)
      ));
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithAgents_withLccnWithBodies() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_agent_lccn_with_body.json");
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
    var expectedCreatorProperties = Map.of(
      LABEL, List.of(creatorLabel),
      NAME, List.of(creatorLabel)
    );
    var expectedContributorProperties = Map.of(
      LABEL, List.of(contributorLabel),
      NAME, List.of(contributorLabel)
    );
    String creatorMockLabel = "n2021004098";
    String contributorMockLabel = "n2021004092";
    validateOutgoingEdge(instance, INSTANTIATES, of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> {
        assertThat(work.getId()).isNotNull();
        assertThat(work.getIncomingEdges()).isEmpty();
        assertThat(work.getOutgoingEdges()).hasSize(6);
        validateAgent(work, creatorLabel, creatorMockLabel, CREATOR, of(PERSON, MOCKED_RESOURCE));
        validateOutgoingEdge(work, AUTHOR, of(PERSON, MOCKED_RESOURCE), expectedCreatorProperties, creatorMockLabel);
        validateOutgoingEdge(work, PUBLISHING_DIRECTOR, of(PERSON, MOCKED_RESOURCE), expectedCreatorProperties,
          creatorMockLabel);
        validateAgent(work, contributorLabel, contributorMockLabel, CONTRIBUTOR, of(FAMILY, MOCKED_RESOURCE));
        validateOutgoingEdge(work, ILLUSTRATOR, of(FAMILY, MOCKED_RESOURCE), expectedContributorProperties,
          contributorMockLabel);
        validateOutgoingEdge(work, COLLABORATOR, of(FAMILY, MOCKED_RESOURCE), expectedContributorProperties,
          contributorMockLabel);
      });
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
    var expectedCreatorProperties = Map.of(
      LABEL, List.of(creatorLabel),
      NAME, List.of(creatorLabel)
    );
    var expectedContributorProperties = Map.of(
      LABEL, List.of(contributorLabel),
      NAME, List.of(contributorLabel)
    );
    validateOutgoingEdge(instance, INSTANTIATES, of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> {
        assertThat(work.getId()).isNotNull();
        assertThat(work.getIncomingEdges()).isEmpty();
        assertThat(work.getOutgoingEdges()).hasSize(6);
        validateAgent(work, creatorLabel, creatorLabel, CREATOR, of(PERSON));
        validateOutgoingEdge(work, AUTHOR, of(PERSON), expectedCreatorProperties, creatorLabel);
        validateOutgoingEdge(work, PUBLISHING_DIRECTOR, of(PERSON), expectedCreatorProperties, creatorLabel);
        validateAgent(work, contributorLabel, contributorLabel, CONTRIBUTOR, of(FAMILY));
        validateOutgoingEdge(work, ILLUSTRATOR, of(FAMILY), expectedContributorProperties, contributorLabel);
        validateOutgoingEdge(work, COLLABORATOR, of(FAMILY), expectedContributorProperties,
          contributorLabel);
      });
  }

  private void validateAgent(Resource work,
                             String labelProperty,
                             String label,
                             PredicateDictionary predicate,
                             Set<ResourceTypeDictionary> types) {
    var expectedProperties = Map.of(
      LABEL, List.of(labelProperty),
      NAME, List.of(labelProperty)
    );
    validateOutgoingEdge(work, predicate, types, expectedProperties, label,
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
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream(rdfFile).readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + contributor.getId().toString())
      .replaceAll("CREATOR_AGENT_ID", "CREATOR_" + creator.getId().toString() + "_agent")
      .replaceAll("CONTRIBUTOR_AGENT_ID", "CONTRIBUTOR_" + contributor.getId().toString() + "_agent");

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    //then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnWorkWithSameAgentAsCreatorAndContributorCorrectly() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var creator = createAgent("n2021004098", ID_LCNAF, true, List.of(PERSON), "Creator Agent");
    work.addOutgoingEdge(new ResourceEdge(work, creator, CREATOR));
    work.addOutgoingEdge(new ResourceEdge(work, creator, CONTRIBUTOR));
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_agent_as_creator_and_contributor.json")
      .readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("CREATOR_ID", "CREATOR_" + creator.getId().toString())
      .replaceAll("CONTRIBUTOR_ID", "CONTRIBUTOR_" + creator.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
