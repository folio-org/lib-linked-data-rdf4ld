package org.folio.rdf4ld.mapper;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCSH;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TEMPORAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.AGENTS_NAMESPACE;
import static org.folio.rdf4ld.test.MonographUtil.SUBJECTS_NAMESPACE;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.createConcept;
import static org.folio.rdf4ld.test.MonographUtil.createConceptAgent;
import static org.folio.rdf4ld.test.MonographUtil.createConceptTopic;
import static org.folio.rdf4ld.test.MonographUtil.createIdentifier;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createResource;
import static org.folio.rdf4ld.test.MonographUtil.createTemporal;
import static org.folio.rdf4ld.test.MonographUtil.createTopic;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
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
  private static final Map<PropertyDictionary, List<String>> EXPECTED_WORK_PROPERTIES = Map.of(
    LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID")
  );

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;
  @Autowired
  private MockLccnResourceService mockLccnResourceService;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectMocks() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subjects_simple_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> {
        validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), PERSON_AGENT_LCCN, null);
        validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), TOPIC_LCCN, null);
        validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), FAMILY_AGENT_LCCN, null);
      });
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectMockWithBody() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_simple_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    var expectedPersonProperties = Map.of(
      LABEL, List.of(PERSON_AGENT_LABEL),
      NAME, List.of(PERSON_AGENT_LABEL)
    );
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, MOCKED_RESOURCE), expectedPersonProperties,
        PERSON_AGENT_LCCN, null));
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectsWithNoLccn() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subjects_simple_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    var expectedPersonProperties = Map.of(
      LABEL, List.of(PERSON_AGENT_LABEL),
      NAME, List.of(PERSON_AGENT_LABEL)
    );
    var expectedTopicProperties = Map.of(
      LABEL, List.of(TOPIC_LABEL),
      NAME, List.of(TOPIC_LABEL)
    );
    var expectedFamilyProperties = Map.of(
      LABEL, List.of(FAMILY_AGENT_LABEL),
      NAME, List.of(FAMILY_AGENT_LABEL)
    );
    var expectedTemporalProperties = Map.of(
      LABEL, List.of(TEMPORAL_LABEL),
      NAME, List.of(TEMPORAL_LABEL)
    );
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> {
        validateOutgoingEdge(work, SUBJECT, Set.of(PERSON, CONCEPT), expectedPersonProperties,
          PERSON_AGENT_LABEL, concept ->
            validateOutgoingEdge(concept, FOCUS, Set.of(PERSON), expectedPersonProperties,
              PERSON_AGENT_LABEL, c -> {})
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT), expectedTopicProperties, TOPIC_LABEL,
          concept -> validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC), expectedTopicProperties,
            TOPIC_LABEL, c -> {})
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(FAMILY, CONCEPT), expectedFamilyProperties,
          FAMILY_AGENT_LABEL, concept ->
            validateOutgoingEdge(concept, FOCUS, Set.of(FAMILY), expectedFamilyProperties,
              FAMILY_AGENT_LABEL, c -> {})
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(TEMPORAL, CONCEPT), expectedTemporalProperties,
          TEMPORAL_LABEL, concept ->
            validateOutgoingEdge(concept, FOCUS, Set.of(TEMPORAL), expectedTemporalProperties,
              TEMPORAL_LABEL, c -> {})
        );
      });
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithSimpleSubjectMock() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_simple_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> {
        validateOutgoingEdge(work, SUBJECT, Set.of(MOCKED_RESOURCE), Map.of(), SIMPLE_SUBJECT_LCCN, null);
      });
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithComplexMixedSubject() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_complex_mixed_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);

    var expectedConceptLabel = TOPIC_LCCN + " -- " + PERSON_AGENT_LCCN + " -- " + FAMILY_AGENT_LABEL;

    var expectedConceptProperties = Map.of(
      LABEL, List.of(expectedConceptLabel),
      NAME, List.of(COMPLEX_SUBJECT_TOPIC_LABEL)
    );

    var expectedTopicProperties = Map.of(
      LABEL, List.of(COMPLEX_SUBJECT_TOPIC_LABEL),
      NAME, List.of(COMPLEX_SUBJECT_TOPIC_LABEL)
    );

    var expectedFamilyProperties = Map.of(
      LABEL, List.of(FAMILY_AGENT_LABEL),
      NAME, List.of(FAMILY_AGENT_LABEL)
    );

    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "",
      work -> validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT, MOCKED_RESOURCE),
        expectedConceptProperties, expectedConceptLabel, concept -> {
          validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC, MOCKED_RESOURCE), expectedTopicProperties, TOPIC_LCCN,
            null);
          validateOutgoingEdge(concept, SUB_FOCUS, Set.of(MOCKED_RESOURCE), Map.of(), PERSON_AGENT_LCCN,
            null);
          validateOutgoingEdge(concept, SUB_FOCUS, Set.of(FAMILY), expectedFamilyProperties, FAMILY_AGENT_LABEL,
            null);
        })
    );
  }

  @Test
  void mapBibframe2RdfToLdAndUnMock_shouldReturnMappedInstanceWithWorkWithComplexMixedSubjectUnmocked()
    throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_complex_mixed_lccn_with_body.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var mapped = rdf4LdMapper.mapBibframe2RdfToLd(model);
    assertThat(mapped).hasSize(1);
    var mappedInstance = mapped.iterator().next();
    var unmockedInstance = mockLccnResourceService.unMockLccnEdges(mappedInstance, lccn -> {
      if (lccn.equals(TOPIC_LCCN)) {
        return of(createTopic(TOPIC_LCCN, true, TOPIC_LABEL));
      }
      if (lccn.equals(PERSON_AGENT_LCCN)) {
        return of(createAgent(PERSON_AGENT_LCCN, ID_LCNAF, true, List.of(PERSON), PERSON_AGENT_LABEL));
      }
      return empty();
    });

    // then
    assertThat(unmockedInstance.getId()).isNotNull();
    assertThat(unmockedInstance.getIncomingEdges()).isEmpty();
    assertThat(unmockedInstance.getOutgoingEdges()).hasSize(1);

    var expectedConceptLabel = TOPIC_LABEL + " -- " + FAMILY_AGENT_LABEL + " -- " + PERSON_AGENT_LABEL;
    var expectedConceptProperties = Map.of(
      LABEL, List.of(expectedConceptLabel),
      NAME, List.of(TOPIC_LABEL)
    );

    var expectedTopicProperties = Map.of(
      LABEL, List.of(TOPIC_LABEL),
      NAME, List.of(TOPIC_LABEL)
    );

    var expectedPersonProperties = Map.of(
      LABEL, List.of(PERSON_AGENT_LABEL),
      NAME, List.of(PERSON_AGENT_LABEL)
    );

    var expectedFamilyProperties = Map.of(
      LABEL, List.of(FAMILY_AGENT_LABEL),
      NAME, List.of(FAMILY_AGENT_LABEL)
    );

    var expectedTopicLccnProperties = Map.of(
      NAME, List.of(TOPIC_LCCN),
      LINK, List.of(SUBJECTS_NAMESPACE + TOPIC_LCCN)
    );

    var expectedPersonLccnProperties = Map.of(
      NAME, List.of(PERSON_AGENT_LCCN),
      LINK, List.of(AGENTS_NAMESPACE + PERSON_AGENT_LCCN)
    );

    validateOutgoingEdge(unmockedInstance,
      INSTANTIATES,
      Set.of(WORK, BOOKS),
      EXPECTED_WORK_PROPERTIES,
      "",
      work -> validateOutgoingEdge(work, SUBJECT, Set.of(TOPIC, CONCEPT), expectedConceptProperties,
        expectedConceptLabel, concept -> {
          validateOutgoingEdge(concept, FOCUS, Set.of(TOPIC), expectedTopicProperties, TOPIC_LABEL, topic ->
            validateOutgoingEdge(topic, MAP, Set.of(IDENTIFIER, ID_LCSH), expectedTopicLccnProperties, TOPIC_LCCN)
          );
          validateOutgoingEdge(concept, SUB_FOCUS, Set.of(PERSON), expectedPersonProperties, PERSON_AGENT_LABEL,
            person -> validateOutgoingEdge(person, MAP, Set.of(IDENTIFIER, ID_LCNAF), expectedPersonLccnProperties,
              PERSON_AGENT_LCCN));
          validateOutgoingEdge(concept, SUB_FOCUS, Set.of(FAMILY), expectedFamilyProperties, FAMILY_AGENT_LABEL);
        })
    );
  }

  @Test
  void mapBibframe2RdfToLdAndUnMock_shouldReturnUnmodifiedComplexSubjectIfPreferred() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_subject_simple_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    var preferredSubjectProperties = Map.of(
      RESOURCE_PREFERRED, List.of("true"),
      LABEL, List.of("Some label"),
      GEOGRAPHIC_SUBDIVISION, List.of("Some geo subdivision")
    );
    var preferredComplexSubject = createResource(preferredSubjectProperties, Set.of(CONCEPT, TOPIC), Map.of());

    // when
    var mapped = rdf4LdMapper.mapBibframe2RdfToLd(model);
    assertThat(mapped).hasSize(1);
    var mappedInstance = mapped.iterator().next();
    var unmockedInstance = mockLccnResourceService.unMockLccnEdges(mappedInstance, lccn -> {
      if (lccn.equals(SIMPLE_SUBJECT_LCCN)) {
        return of(preferredComplexSubject);
      }
      return empty();
    });

    // then
    var work = unmockedInstance.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate().equals(INSTANTIATES))
      .map(ResourceEdge::getTarget)
      .findFirst().orElseThrow();

    validateOutgoingEdge(
      work,
      SUBJECT,
      preferredComplexSubject.getTypes(),
      preferredSubjectProperties,
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
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subjects_simple_lccn.json").readAllBytes())
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
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subjects_simple_no_lccn.json")
      .readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("PERSON_AGENT_ID", "_" + personAgent.getId().toString())
      .replaceAll("FAMILY_AGENT_ID", "_" + familyAgent.getId().toString())
      .replaceAll("TOPIC_ID", "_" + topic.getId().toString())
      .replaceAll("TEMPORAL_ID", "_" + temporal.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

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
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subject_complex_mixed_lccn.json")
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
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_subject_simple_lccn.json")
      .readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString());


    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
