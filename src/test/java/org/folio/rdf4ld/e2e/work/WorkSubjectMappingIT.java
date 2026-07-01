package org.folio.rdf4ld.e2e.work;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.PropertyDictionary.GENERAL_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCSH;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TEMPORAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.rdf4ld.test.MonographUtil.AGENTS_NAMESPACE;
import static org.folio.rdf4ld.test.MonographUtil.SUBJECTS_NAMESPACE;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createConcept;
import static org.folio.rdf4ld.test.MonographUtil.createConceptAgent;
import static org.folio.rdf4ld.test.MonographUtil.createConceptTopic;
import static org.folio.rdf4ld.test.MonographUtil.createGenreForm;
import static org.folio.rdf4ld.test.MonographUtil.createIdentifier;
import static org.folio.rdf4ld.test.MonographUtil.createResource;
import static org.folio.rdf4ld.test.MonographUtil.createSubjectPlace;
import static org.folio.rdf4ld.test.MonographUtil.createTemporal;
import static org.folio.rdf4ld.test.MonographUtil.createTopic;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkSubjectMappingIT {

  private static final String FAMILY_AGENT_LCCN = "n123456789";
  private static final String PERSON_AGENT_LCCN = "n79026681";
  private static final String TOPIC_LCCN = "sh85070981";
  private static final String TEMPORAL_LCCN = "sh85070982";
  private static final String FAMILY_AGENT_LABEL = "Family Agent";
  private static final String PERSON_AGENT_LABEL = "Person Agent";
  private static final String TOPIC_LABEL = "Subject Topic";
  private static final String TEMPORAL_LABEL = "Subject Temporal";
  private static final String COMPLEX_SUBJECT_TOPIC_LABEL = "Topic name";
  private static final String SIMPLE_SUBJECT_LCCN = "sh111222333";
  private static final String FOCUS_LABEL = "Subject Focus";
  private static final String SUB_FOCUS_LABEL = "Sub Focus Topic";
  private static final String COMPLEX_CONCEPT_LABEL = FOCUS_LABEL + " -- " + SUB_FOCUS_LABEL;
  private static final String BASE_PATH = "/rdf/work/subject/";

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;
  @Autowired
  private MockLccnResourceService mockLccnResourceService;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectMocks() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subjects_simple_lccn.json");
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
    validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), PERSON_AGENT_LCCN, null);
    validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), TOPIC_LCCN, null);
    validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), FAMILY_AGENT_LCCN, null);
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectMockWithBody() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_simple_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(1);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var personProperties = Map.of(
      LABEL, List.of(PERSON_AGENT_LABEL),
      NAME, List.of(PERSON_AGENT_LABEL)
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, MOCKED_RESOURCE), personProperties, PERSON_AGENT_LCCN, null);
  }

  @Test
  void mapBibframe2RdfToLd_shouldMapRdfsLabelToNameAndAuthoritativeLabelToLabel() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_label_name_distinction.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(1);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var personProperties = Map.of(
      LABEL, List.of("Person Agent,", "Person Agent"),
      NAME, List.of("Person Agent,")
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, MOCKED_RESOURCE), personProperties, PERSON_AGENT_LCCN, null);
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectsWithNoLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subjects_simple_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(4);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var personProperties = Map.of(
      LABEL, List.of(PERSON_AGENT_LABEL),
      NAME, List.of(PERSON_AGENT_LABEL)
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, CONCEPT), personProperties, PERSON_AGENT_LABEL, concept ->
      validateOutgoingEdge(concept, FOCUS, Set.of(PERSON), personProperties, PERSON_AGENT_LABEL, c -> {})
    );
    var topicProperties = Map.of(
      LABEL, List.of(TOPIC_LABEL),
      NAME, List.of(TOPIC_LABEL)
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT), topicProperties, TOPIC_LABEL, concept ->
        validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC), topicProperties, TOPIC_LABEL, c -> {})
    );
    var familyProperties = Map.of(
      LABEL, List.of(FAMILY_AGENT_LABEL),
      NAME, List.of(FAMILY_AGENT_LABEL)
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(FAMILY, CONCEPT), familyProperties, FAMILY_AGENT_LABEL, concept ->
        validateOutgoingEdge(concept, FOCUS, Set.of(FAMILY), familyProperties, FAMILY_AGENT_LABEL, c -> {})
    );
    var temporalProperties = Map.of(
      LABEL, List.of(TEMPORAL_LABEL),
      NAME, List.of(TEMPORAL_LABEL)
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(TEMPORAL, CONCEPT), temporalProperties, TEMPORAL_LABEL, concept ->
        validateOutgoingEdge(concept, FOCUS, Set.of(TEMPORAL), temporalProperties, TEMPORAL_LABEL, c -> {})
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectMock() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_simple_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(1);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), SIMPLE_SUBJECT_LCCN, null);
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithComplexMixedSubject() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_complex_mixed_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).hasSize(1);
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    var conceptLabel = TOPIC_LCCN + " -- " + FAMILY_AGENT_LABEL + " -- " + PERSON_AGENT_LCCN;
    var conceptProperties = Map.of(
      LABEL, List.of(conceptLabel),
      NAME, List.of(COMPLEX_SUBJECT_TOPIC_LABEL)
    );
    var topicProperties = Map.of(
      LABEL, List.of(COMPLEX_SUBJECT_TOPIC_LABEL),
      NAME, List.of(COMPLEX_SUBJECT_TOPIC_LABEL)
    );
    var familyProperties = Map.of(
      LABEL, List.of(FAMILY_AGENT_LABEL),
      NAME, List.of(FAMILY_AGENT_LABEL)
    );
    validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT, MOCKED_RESOURCE), conceptProperties, conceptLabel,
      concept -> {
        validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC, MOCKED_RESOURCE), topicProperties, TOPIC_LCCN, null);
        validateOutgoingEdge(concept, SUB_FOCUS, Set.of(MOCKED_RESOURCE), Map.of(), PERSON_AGENT_LCCN, null);
        validateOutgoingEdge(concept, SUB_FOCUS, Set.of(FAMILY), familyProperties, FAMILY_AGENT_LABEL, null);
      });
  }

  @Test
  void mapBibframe2RdfToLdAndUnMock_shouldReturnMappedInstanceWithWorkWithComplexMixedSubjectUnmocked()
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_complex_mixed_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var mapped = rdf4LdMapper.mapBibframe2RdfToLd(model);
    assertThat(mapped).hasSize(1);
    var mappedWork = mapped.iterator().next();
    var unmockedWork = mockLccnResourceService.unMockLccnEdges(mappedWork, lccn -> {
      if (lccn.equals(TOPIC_LCCN)) {
        return of(createTopic(TOPIC_LCCN, true, TOPIC_LABEL));
      }
      if (lccn.equals(PERSON_AGENT_LCCN)) {
        return of(createAgent(PERSON_AGENT_LCCN, ID_LCNAF, true, List.of(PERSON), PERSON_AGENT_LABEL));
      }
      return empty();
    });

    // then
    assertThat(unmockedWork.getId()).isNotNull();
    assertThat(unmockedWork.getIncomingEdges()).isEmpty();
    assertThat(unmockedWork.getOutgoingEdges()).hasSize(1);
    validateProperty(unmockedWork.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));

    var conceptLabel = TOPIC_LABEL + " -- " + FAMILY_AGENT_LABEL + " -- " + PERSON_AGENT_LABEL;
    var conceptProperties = Map.of(
      LABEL, List.of(conceptLabel),
      NAME, List.of(TOPIC_LABEL)
    );
    var topicProperties = Map.of(
      LABEL, List.of(TOPIC_LABEL),
      NAME, List.of(TOPIC_LABEL)
    );
    var personProperties = Map.of(
      LABEL, List.of(PERSON_AGENT_LABEL),
      NAME, List.of(PERSON_AGENT_LABEL)
    );
    var familyProperties = Map.of(
      LABEL, List.of(FAMILY_AGENT_LABEL),
      NAME, List.of(FAMILY_AGENT_LABEL)
    );
    var topicLccnProperties = Map.of(
      NAME, List.of(TOPIC_LCCN),
      LINK, List.of(SUBJECTS_NAMESPACE + TOPIC_LCCN)
    );
    var personLccnProperties = Map.of(
      NAME, List.of(PERSON_AGENT_LCCN),
      LINK, List.of(AGENTS_NAMESPACE + PERSON_AGENT_LCCN)
    );
    validateOutgoingEdge(unmockedWork, SUBJECT, Set.of(TOPIC, CONCEPT), conceptProperties, conceptLabel, concept -> {
      validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC), topicProperties, TOPIC_LABEL, topic ->
          validateOutgoingEdge(topic, MAP, Set.of(IDENTIFIER, ID_LCSH), topicLccnProperties, TOPIC_LCCN)
      );
      validateOutgoingEdge(concept, SUB_FOCUS, Set.of(PERSON), personProperties, PERSON_AGENT_LABEL, person ->
        validateOutgoingEdge(person, MAP, Set.of(IDENTIFIER, ID_LCNAF), personLccnProperties, PERSON_AGENT_LCCN)
      );
      validateOutgoingEdge(concept, SUB_FOCUS, Set.of(FAMILY), familyProperties, FAMILY_AGENT_LABEL);
    });
  }

  @Test
  void mapBibframe2RdfToLdAndUnMock_shouldReturnUnmodifiedComplexSubjectIfWithFolioMetadata() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_simple_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var subjectProperties = Map.of(
      LABEL, List.of("Some label"),
      GEOGRAPHIC_SUBDIVISION, List.of("Some geo subdivision")
    );
    var preferredComplexSubject = createResource(subjectProperties, Set.of(CONCEPT, TOPIC), Map.of())
      .setFolioMetadata(new FolioMetadata());

    // when
    var mapped = rdf4LdMapper.mapBibframe2RdfToLd(model);
    assertThat(mapped).hasSize(1);
    var mappedWork = mapped.iterator().next();
    var unmockedWork = mockLccnResourceService.unMockLccnEdges(mappedWork, lccn -> {
      if (lccn.equals(SIMPLE_SUBJECT_LCCN)) {
        return of(preferredComplexSubject);
      }
      return empty();
    });

    // then
    validateProperty(unmockedWork.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    validateOutgoingEdge(
      unmockedWork,
      SUBJECT,
      preferredComplexSubject.getTypes(),
      subjectProperties,
      preferredComplexSubject.getLabel()
    );
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithSimpleSubjectsWithLccn() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var personAgent = createConceptAgent(PERSON_AGENT_LCCN, true, List.of(PERSON), FAMILY_AGENT_LABEL);
    var familyAgent = createConceptAgent(FAMILY_AGENT_LCCN, true, List.of(FAMILY), PERSON_AGENT_LABEL);
    var topic = createConceptTopic(TOPIC_LCCN, true, TOPIC_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, personAgent, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, familyAgent, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, topic, SUBJECT));
    var expected = new String(this.getClass()
      .getResourceAsStream(BASE_PATH + "work_subjects_simple_lccn.json").readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithSimpleSubjectsWithoutLccn() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var personAgent = createAgent(PERSON_AGENT_LCCN, ID_LCNAF, false, List.of(PERSON), PERSON_AGENT_LABEL);
    var familyAgent = createAgent(FAMILY_AGENT_LCCN, ID_LCNAF, false, List.of(FAMILY), FAMILY_AGENT_LABEL);
    var topic = createTopic(TOPIC_LCCN, false, TOPIC_LABEL);
    var temporal = createTemporal(TEMPORAL_LCCN, false, TEMPORAL_LABEL);
    var personConcept = createConcept(List.of(PERSON), List.of(personAgent), List.of(), PERSON_AGENT_LABEL);
    var familyConcept = createConcept(List.of(FAMILY), List.of(familyAgent), List.of(), FAMILY_AGENT_LABEL);
    var topicConcept = createConcept(List.of(TOPIC), List.of(topic), List.of(), TOPIC_LABEL);
    var temporalConcept = createConcept(List.of(TEMPORAL), List.of(temporal), List.of(), TEMPORAL_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, personConcept, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, familyConcept, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, topicConcept, SUBJECT));
    work.addOutgoingEdge(new ResourceEdge(work, temporalConcept, SUBJECT));
    var expected = new String(this.getClass().getResourceAsStream(BASE_PATH + "work_subjects_simple_no_lccn.json")
      .readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("PERSON_AGENT_ID", "_" + personAgent.getId().toString())
      .replaceAll("FAMILY_AGENT_ID", "_" + familyAgent.getId().toString())
      .replaceAll("TOPIC_ID", "_" + topic.getId().toString())
      .replaceAll("TEMPORAL_ID", "_" + temporal.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithComplexSubjectMixed() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var topic = createTopic(TOPIC_LCCN, true, TOPIC_LABEL);
    var personAgent = createAgent(PERSON_AGENT_LCCN, ID_LCNAF, true, List.of(PERSON), PERSON_AGENT_LABEL);
    var familyAgent = createAgent(FAMILY_AGENT_LCCN, ID_LCNAF, false, List.of(FAMILY), FAMILY_AGENT_LABEL);
    var concept = createConcept(List.of(TOPIC), List.of(topic), List.of(personAgent, familyAgent),
      COMPLEX_SUBJECT_TOPIC_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, concept, SUBJECT));
    var inputStream = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_complex_mixed_lccn.json");
    var expected = new String(inputStream.readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("COMPLEX_SUBJECT_ID", concept.getId().toString())
      .replaceAll("FAMILY_AGENT_ID", "_" + familyAgent.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithSimpleSubject_withLccn() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var personAgent = createAgent(PERSON_AGENT_LCCN, ID_LCNAF, true, List.of(PERSON), PERSON_AGENT_LABEL);
    var familyAgent = createAgent(FAMILY_AGENT_LCCN, ID_LCNAF, false, List.of(FAMILY), FAMILY_AGENT_LABEL);
    var topic = createTopic(TOPIC_LCCN, true, TOPIC_LABEL);
    var concept = createConcept(List.of(TOPIC), List.of(topic), List.of(personAgent, familyAgent), "subject label");
    var conceptLccn = createIdentifier(SIMPLE_SUBJECT_LCCN, ID_LCSH, SUBJECTS_NAMESPACE, true);
    concept.addOutgoingEdge(new ResourceEdge(concept, conceptLccn, MAP));
    work.addOutgoingEdge(new ResourceEdge(work, concept, SUBJECT));
    var expected = new String(this.getClass().getResourceAsStream(BASE_PATH + "work_subject_simple_lccn.json")
      .readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnUncontrolledConceptForComplexBlankNodeSubject() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(BASE_PATH + "work_subject_concept_person_complex_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getOutgoingEdges()).hasSize(1);
    var subFocusProperties = Map.of(LABEL, List.of(SUB_FOCUS_LABEL), NAME, List.of(SUB_FOCUS_LABEL));
    var conceptProperties = Map.of(
      LABEL, List.of(COMPLEX_CONCEPT_LABEL),
      NAME, List.of(FOCUS_LABEL),
      GENERAL_SUBDIVISION, List.of(SUB_FOCUS_LABEL)
    );
    var focusProperties = Map.of(LABEL, List.of(FOCUS_LABEL), NAME, List.of(FOCUS_LABEL));
    validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, CONCEPT), conceptProperties, COMPLEX_CONCEPT_LABEL, concept -> {
      assertThat(concept.getOutgoingEdges()).hasSize(2);
      validateOutgoingEdge(concept, FOCUS, Set.of(PERSON), focusProperties, FOCUS_LABEL, c -> {});
      validateOutgoingEdge(concept, SUB_FOCUS, Set.of(TOPIC), subFocusProperties, SUB_FOCUS_LABEL, c -> {});
    });
  }

  @ParameterizedTest
  @MethodSource("subjectConceptTypeArgs")
  void mapBibframe2RdfToLd_shouldMapSubjectIntermediateConceptNode(
    String fixturePath, ResourceTypeDictionary expectedType, String label) throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(fixturePath);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var work = result.iterator().next();
    validateProperty(work.getDoc(), LINK.getValue(), List.of("http://test-tobe-changed.folio.com/resources/WORK_ID"));
    assertThat(work.getOutgoingEdges()).hasSize(1);
    var subjectProperties = Map.of(LABEL, List.of(label), NAME, List.of(label));
    validateOutgoingEdge(work, SUBJECT, Set.of(expectedType, CONCEPT), subjectProperties, label, concept -> {
      assertThat(concept.getOutgoingEdges()).hasSize(1);
      validateOutgoingEdge(concept, FOCUS, Set.of(expectedType), subjectProperties, label, c -> {});
    });
  }

  @ParameterizedTest
  @MethodSource("subjectConceptTypeArgs")
  void mapLdToBibframe2Rdf_shouldMapSubjectIntermediateConceptNode(
    String fixturePath, ResourceTypeDictionary subjectType, String label) throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var focus = createSubjectFocusByType(subjectType, label);
    var concept = createConcept(List.of(subjectType), List.of(focus), List.of(), label);
    work.addOutgoingEdge(new ResourceEdge(work, concept, SUBJECT));
    var expected = new String(this.getClass().getResourceAsStream(fixturePath).readAllBytes())
      .replace("WORK_ID", work.getId().toString())
      .replace("SUBJECT_ID", "_" + focus.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    assertThat(toJsonLdString(model)).isEqualTo(expected);
  }

  private static Resource createSubjectFocusByType(ResourceTypeDictionary type, String label) {
    return createSubjectFocusByType(type, label, false);
  }

  private static Resource createSubjectFocusByType(ResourceTypeDictionary type, String label, boolean isCurrent) {
    if (type == TOPIC) {
      return createTopic("subject-lccn", isCurrent, label);
    }
    if (type == FORM) {
      return createGenreForm("subject-lccn", isCurrent, label);
    }
    if (type == PLACE) {
      return createSubjectPlace("subject-lccn", isCurrent, label);
    }
    return createAgent("subject-lccn", ID_LCNAF, isCurrent, List.of(type), label);
  }

  static Stream<Arguments> subjectConceptTypeArgs() {
    return Stream.of(
      Arguments.of(BASE_PATH + "work_subject_concept_person.json", PERSON, "Subject Person"),
      Arguments.of(BASE_PATH + "work_subject_concept_family.json", FAMILY, "Subject Family"),
      Arguments.of(BASE_PATH + "work_subject_concept_organization.json", ORGANIZATION, "Subject Organization"),
      Arguments.of(BASE_PATH + "work_subject_concept_meeting.json", MEETING, "Subject Meeting"),
      Arguments.of(BASE_PATH + "work_subject_concept_topic.json", TOPIC, "Subject Topic"),
      Arguments.of(BASE_PATH + "work_subject_concept_place.json", PLACE, "Subject Place"),
      Arguments.of(BASE_PATH + "work_subject_concept_form.json", FORM, "Subject Form")
    );
  }

  @ParameterizedTest
  @MethodSource("subjectConceptTypeWithLccnArgs")
  void mapLdToBibframe2Rdf_shouldMapSubjectIntermediateConceptNodeWithLccn(
    String fixturePath, ResourceTypeDictionary subjectType, String label) throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var focus = createSubjectFocusByType(subjectType, label, true);
    var concept = createConcept(List.of(subjectType), List.of(focus), List.of(), label);
    work.addOutgoingEdge(new ResourceEdge(work, concept, SUBJECT));
    var inputStream = this.getClass().getResourceAsStream(BASE_PATH + fixturePath);
    var expected = new String(inputStream.readAllBytes())
      .replace("WORK_ID", work.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    assertThat(toJsonLdString(model)).isEqualTo(expected);
  }

  static Stream<Arguments> subjectConceptTypeWithLccnArgs() {
    return Stream.of(
      Arguments.of("work_subject_concept_person_with_lccn.json", PERSON, "Subject Person"),
      Arguments.of("work_subject_concept_organization_with_lccn.json", ORGANIZATION, "Subject Organization"),
      Arguments.of("work_subject_concept_meeting_with_lccn.json", MEETING, "Subject Meeting"),
      Arguments.of("work_subject_concept_topic_with_lccn.json", TOPIC, "Subject Topic"),
      Arguments.of("work_subject_concept_place_with_lccn.json", PLACE, "Subject Place"),
      Arguments.of("work_subject_concept_form_with_lccn.json", FORM, "Subject Form")
    );
  }

  @ParameterizedTest
  @MethodSource("subjectComplexConceptTypeArgs")
  void mapLdToBibframe2Rdf_shouldMapComplexSubjectConceptNodeNoLccn(
    String fixturePath, ResourceTypeDictionary focusType) throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var focus = createSubjectFocusByType(focusType, FOCUS_LABEL, false);
    var subFocus = createTopic("sub-focus-lccn", false, SUB_FOCUS_LABEL);
    var concept = createConcept(List.of(focusType), List.of(focus), List.of(subFocus), COMPLEX_CONCEPT_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, concept, SUBJECT));
    var expected = new String(this.getClass().getResourceAsStream(fixturePath).readAllBytes())
      .replace("WORK_ID", work.getId().toString())
      .replace("COMPLEX_SUBJECT_ID", concept.getId().toString())
      .replace("SUBFOCUS_ID", "_" + subFocus.getId().toString())
      .replace("FOCUS_ID", "_" + focus.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(work);

    // then
    assertThat(toJsonLdString(model)).isEqualTo(expected);
  }

  static Stream<Arguments> subjectComplexConceptTypeArgs() {
    return Stream.of(
      Arguments.of(BASE_PATH + "work_subject_concept_person_complex_no_lccn.json", PERSON),
      Arguments.of(BASE_PATH + "work_subject_concept_organization_complex_no_lccn.json", ORGANIZATION),
      Arguments.of(BASE_PATH + "work_subject_concept_meeting_complex_no_lccn.json", MEETING),
      Arguments.of(BASE_PATH + "work_subject_concept_topic_complex_no_lccn.json", TOPIC),
      Arguments.of(BASE_PATH + "work_subject_concept_place_complex_no_lccn.json", PLACE),
      Arguments.of(BASE_PATH + "work_subject_concept_form_complex_no_lccn.json", FORM)
    );
  }

}
