package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class InstanceWorkReferencesMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapToLdInstance_shouldReturnMappedInstanceWithWorkWithReferences() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_work_references.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var genreForm = new Resource().setId(1L).setLabel("genreForm");
    var subjectAgent = new Resource().setId(2L).setLabel("subjectAgent");
    var subjectTopic = new Resource().setId(3L).setLabel("subjectTopic");
    var foundByLccnResources = Map.of(
      "gf2014026339", genreForm,
      "n79026681", subjectAgent,
      "sh85070981", subjectTopic
    );

    // when
    var result = rdf4LdMapper.mapToLdInstance(model, key -> Optional.of(foundByLccnResources.get(key)));

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK), Map.of(), "",
      work -> validateWork(work,
        new ResourceEdge(work, genreForm, GENRE),
        new ResourceEdge(work, subjectAgent, SUBJECT),
        new ResourceEdge(work, subjectTopic, SUBJECT)
      ));
  }

  @Test
  void mapToLdInstance_shouldReturnMappedInstanceWithWorkWithReferenceIds() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_work_references_ids.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);
    var genreForm = new Resource().setId(1L).setLabel("genreForm");
    var subjectAgent = new Resource().setId(2L).setLabel("subjectAgent");
    var subjectTopic = new Resource().setId(3L).setLabel("subjectTopic");
    var foundByLccnResources = Map.of(
      "gf2014026339", genreForm,
      "n79026681", subjectAgent,
      "sh85070981", subjectTopic
    );

    // when
    var result = rdf4LdMapper.mapToLdInstance(model, key -> Optional.of(foundByLccnResources.get(key)));

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK), Map.of(), "",
      work -> validateWork(work,
        new ResourceEdge(work, genreForm, GENRE),
        new ResourceEdge(work, subjectAgent, SUBJECT),
        new ResourceEdge(work, subjectTopic, SUBJECT)
      ));
  }

  private void validateWork(Resource work, ResourceEdge... edges) {
    assertThat(work.getId()).isNotNull();
    assertThat(work.getIncomingEdges()).isEmpty();
    assertThat(work.getOutgoingEdges()).containsOnly(edges);
  }

}
