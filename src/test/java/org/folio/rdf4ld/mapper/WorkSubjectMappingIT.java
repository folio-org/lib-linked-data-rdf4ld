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
import static org.folio.rdf4ld.test.MonographUtil.createConcept;
import static org.folio.rdf4ld.test.MonographUtil.createConceptAgent;
import static org.folio.rdf4ld.test.MonographUtil.createConceptTopic;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkSubjectMappingIT {

  private static final String FAMILY_AGENT_LCCN = "n123456789";
  private static final String PERSON_AGENT_LCCN = "n79026681";
  private static final String TOPIC_LCCN = "sh85070981";
  private static final String FAMILY_AGENT_LABEL = "Family Agent";
  private static final String PERSON_AGENT_LABEL = "Person Agent";
  private static final String TOPIC_LABEL = "Subject Topic";
  private static final String COMPLEX_SUBJECT_LABEL = "Complex Subject Label";

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;
  @MockitoBean
  private Function<String, Optional<Resource>> resourceProvider;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectsWithLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_simple_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var subjectAgent = createAgent(PERSON_AGENT_LCCN, true, List.of(PERSON), PERSON_AGENT_LABEL);
    var subjectTopic = createTopic(TOPIC_LCCN, true, TOPIC_LABEL);
    var conceptAgent = createConceptAgent(FAMILY_AGENT_LCCN, true, List.of(FAMILY), FAMILY_AGENT_LABEL);
    var foundByLccnResources = Map.of(
      PERSON_AGENT_LCCN, subjectAgent,
      TOPIC_LCCN, subjectTopic,
      FAMILY_AGENT_LCCN, conceptAgent
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
        validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, CONCEPT), Map.of(LABEL, List.of(PERSON_AGENT_LABEL)),
          PERSON_AGENT_LABEL, concept ->
            validateResourceWithGivenEdges(concept, new ResourceEdge(concept, subjectAgent, FOCUS))
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT), Map.of(LABEL, List.of(TOPIC_LABEL)), TOPIC_LABEL,
          concept -> validateResourceWithGivenEdges(concept, new ResourceEdge(concept, subjectTopic, FOCUS))
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(FAMILY, CONCEPT), Map.of(LABEL, List.of(FAMILY_AGENT_LABEL)),
          FAMILY_AGENT_LABEL, concept -> assertThat(concept).isEqualTo(conceptAgent));
      });
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectsWithNoLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_simple_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

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
        validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, CONCEPT), Map.of(LABEL, List.of(PERSON_AGENT_LABEL)),
          PERSON_AGENT_LABEL, concept ->
            validateOutgoingEdge(concept, FOCUS, Set.of(PERSON), Map.of(LABEL, List.of(PERSON_AGENT_LABEL)),
              PERSON_AGENT_LABEL, c -> {})
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT), Map.of(LABEL, List.of(TOPIC_LABEL)), TOPIC_LABEL,
          concept -> validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC), Map.of(LABEL, List.of(TOPIC_LABEL)),
            TOPIC_LABEL, c -> {})
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(FAMILY, CONCEPT), Map.of(LABEL, List.of(FAMILY_AGENT_LABEL)),
          FAMILY_AGENT_LABEL, concept ->
            validateOutgoingEdge(concept, FOCUS, Set.of(FAMILY), Map.of(LABEL, List.of(FAMILY_AGENT_LABEL)),
              FAMILY_AGENT_LABEL, c -> {})
        );
      });
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithSimpleSubjectsWithLccn() throws IOException {
    // given
    var work = createWork("work");
    var personAgent = createConceptAgent(PERSON_AGENT_LCCN, true, List.of(PERSON), FAMILY_AGENT_LABEL);
    var familyAgent = createConceptAgent(FAMILY_AGENT_LCCN, true, List.of(FAMILY), PERSON_AGENT_LABEL);
    var topic = createConceptTopic(TOPIC_LCCN, true, TOPIC_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, personAgent, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, familyAgent, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, topic, SUBJECT));
    var instance = createInstance("instance").setDoc(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subject_simple_lccn.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithSimpleSubjectsWithoutLccn() throws IOException {
    // given
    var work = createWork("work");
    var personAgent = createAgent(PERSON_AGENT_LCCN, false, List.of(PERSON), PERSON_AGENT_LABEL);
    var familyAgent = createAgent(FAMILY_AGENT_LCCN, false, List.of(FAMILY), FAMILY_AGENT_LABEL);
    var topic = createTopic(TOPIC_LCCN, false, TOPIC_LABEL);
    var personConcept = createConcept(List.of(PERSON), List.of(personAgent), List.of(), PERSON_AGENT_LABEL);
    var familyConcept = createConcept(List.of(FAMILY), List.of(familyAgent), List.of(), FAMILY_AGENT_LABEL);
    var topicConcept = createConcept(List.of(TOPIC), List.of(topic), List.of(), TOPIC_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, personConcept, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, familyConcept, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, topicConcept, SUBJECT));
    var instance = createInstance("instance").setDoc(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subject_simple_no_lccn.json")
      .readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("PERSON_AGENT_ID", "_" + personAgent.getId().toString())
      .replaceAll("FAMILY_AGENT_ID", "_" + familyAgent.getId().toString())
      .replaceAll("TOPIC_ID", "_" + topic.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithComplexSubject() throws IOException {
    // given
    var work = createWork("work");
    var personAgent = createAgent(PERSON_AGENT_LCCN, true, List.of(PERSON), PERSON_AGENT_LABEL);
    var familyAgent = createAgent(FAMILY_AGENT_LCCN, false, List.of(FAMILY), FAMILY_AGENT_LABEL);
    var topic = createTopic(TOPIC_LCCN, true, TOPIC_LABEL);
    var concept = createConcept(List.of(TOPIC), List.of(topic), List.of(personAgent, familyAgent),
      COMPLEX_SUBJECT_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, concept, SUBJECT));
    var instance = createInstance("instance").setDoc(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subject_complex.json")
      .readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("COMPLEX_SUBJECT_ID", concept.getId().toString())
      .replaceAll("FAMILY_AGENT_ID", "_" + familyAgent.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
