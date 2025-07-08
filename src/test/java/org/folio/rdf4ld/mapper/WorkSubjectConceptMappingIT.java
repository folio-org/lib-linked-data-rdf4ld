package org.folio.rdf4ld.mapper;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createConceptAgent;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createTopic;
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
class WorkSubjectConceptMappingIT {

  private static final String SUBJECT_AGENT_LCCN = "n79026681";
  private static final String SUBJECT_TOPIC_LCCN = "sh85070981";
  private static final String CONCEPT_AGENT_LCCN = "n123456789";
  private static final String AGENT_LABEL = "Subject Agent";
  private static final String TOPIC_LABEL = "Subject Topic";
  private static final String CONCEPT_AGENT_LABEL = "Subject-concept Agent";

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;
  @MockitoBean
  private Function<String, Optional<Resource>> resourceProvider;

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/work_subject.json",
    "/rdf/work_subject_lccn.json"
  })
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSubjects(String rdfFile) throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(rdfFile);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var subjectAgent = createAgent(SUBJECT_AGENT_LCCN, true, List.of(PERSON), AGENT_LABEL);
    var subjectTopic = createTopic(SUBJECT_TOPIC_LCCN, true, TOPIC_LABEL);
    var conceptAgent = createConceptAgent(CONCEPT_AGENT_LCCN, true, List.of(FAMILY),
      CONCEPT_AGENT_LABEL);
    var foundByLccnResources = Map.of(
      SUBJECT_AGENT_LCCN, subjectAgent,
      SUBJECT_TOPIC_LCCN, subjectTopic,
      CONCEPT_AGENT_LCCN, conceptAgent
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
      work -> {
        validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, CONCEPT), Map.of(LABEL, List.of(AGENT_LABEL)),
          AGENT_LABEL, concept ->
            validateResourceWithGivenEdges(concept, new ResourceEdge(concept, subjectAgent, FOCUS))
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT), Map.of(LABEL, List.of(TOPIC_LABEL)), TOPIC_LABEL,
          concept -> validateResourceWithGivenEdges(concept, new ResourceEdge(concept, subjectTopic, FOCUS))
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(FAMILY, CONCEPT), Map.of(LABEL, List.of(CONCEPT_AGENT_LABEL)),
          CONCEPT_AGENT_LABEL, concept ->
            validateResourceWithGivenEdges(concept, new ResourceEdge(concept, conceptAgent, FOCUS))
        );
      });
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/work_subject_lccn.json"
  })
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithSubjects(String rdfFile) throws IOException {
    // given
    var work = createWork("work");
    var isCurrent = !rdfFile.contains("no_lccn");
    var agentConcept = createConceptAgent(SUBJECT_AGENT_LCCN, isCurrent, List.of(PERSON), CONCEPT_AGENT_LABEL);
    var agent = createAgent(CONCEPT_AGENT_LCCN, isCurrent, List.of(FAMILY), AGENT_LABEL);
    var topic = createTopic(SUBJECT_TOPIC_LCCN, isCurrent, TOPIC_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, agentConcept, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, agent, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, topic, SUBJECT));
    var instance = createInstance("instance").setDoc(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream(rdfFile).readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    //then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
