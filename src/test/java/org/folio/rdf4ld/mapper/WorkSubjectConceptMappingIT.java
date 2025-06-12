package org.folio.rdf4ld.mapper;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.create;
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

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;
  @MockitoBean
  private Function<String, Optional<Resource>> resourceProvider;

  @ParameterizedTest
  @ValueSource(strings = {
    "/rdf/work_subject.json",
    "/rdf/work_subject_id.json"
  })
  void mapToLdInstance_shouldReturnMappedInstanceWithWorkWithSubjects(String rdfFile) throws IOException {
    // given
    var input = this.getClass().getResourceAsStream(rdfFile);
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var agentName = "agentName";
    var subjectAgent = create(2L, agentName, Set.of(PERSON), Map.of(
      RESOURCE_PREFERRED.getValue(), List.of("true"),
      NAME.getValue(), List.of(agentName))
    );
    var subjectTerm = "subjectTerm";
    var subjectTopic = create(3L, subjectTerm, Set.of(TOPIC), Map.of(
      RESOURCE_PREFERRED.getValue(), List.of("false"),
      TERM.getValue(), List.of(subjectTerm))
    );
    var foundByLccnResources = Map.of(
      "n79026681", subjectAgent,
      "sh85070981", subjectTopic
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
      work -> {
        validateOutgoingEdge(work, SUBJECT, Set.of(CONCEPT, PERSON), Map.of(NAME, List.of(agentName)), agentName,
          concept -> validateResourceWithGivenEdges(concept, new ResourceEdge(concept, subjectAgent, FOCUS))
        );
        validateOutgoingEdge(work, SUBJECT, Set.of(CONCEPT, TOPIC), Map.of(TERM, List.of(subjectTerm)), subjectTerm,
          concept -> validateResourceWithGivenEdges(concept, new ResourceEdge(concept, subjectTopic, FOCUS))
        );
      });
  }

}
