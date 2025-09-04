package org.folio.rdf4ld.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceDeserializerTest {

  @Test
  @SneakyThrows
  void deserializeExportedResource() {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/exported.json");

    // when
    var om = new ObjectMapper();
    var sm = new SimpleModule();
    sm.addDeserializer(Resource.class, new ResourceDeserializer());
    om.registerModule(sm);
    
    var actualResource = om.readValue(input, Resource.class);

    // then
    var expectedResource = createResource();
    assertThat(actualResource).isEqualTo(expectedResource);
    validateWork(actualResource);
    validateWorkTitle(actualResource.getOutgoingEdges().iterator().next().getTarget());
  }

  private Resource createResource() {
    var resource = new Resource()
      .setId(832794024323664921L)
      .setLabel("Resilience Interventions for Youth in Diverse Populations")
      .addType(ResourceTypeDictionary.WORK);
    var doc = JsonNodeFactory.instance.objectNode();
    doc.put(PropertyDictionary.LABEL.getValue(), "Resilience Interventions for Youth in Diverse Populations");
    resource.setDoc(doc);
    var title = new Resource()
      .setId(-3971230252524682729L)
      .setLabel("Resilience Interventions for Youth in Diverse Populations")
      .addType(ResourceTypeDictionary.TITLE);
    var titleDoc = JsonNodeFactory.instance.objectNode();
    titleDoc.put(PropertyDictionary.MAIN_TITLE.getValue(), "Resilience Interventions for Youth in Diverse Populations");
    resource.addOutgoingEdge(new ResourceEdge(
      resource, title, PredicateDictionary.TITLE));

    return resource;
  }

  private void validateWork(Resource resource) {
    assertThat(resource.getId()).isEqualTo(832794024323664921L);
    assertThat(resource.getLabel()).isEqualTo("Resilience Interventions for Youth in Diverse Populations");
    assertThat(resource.getTypes().iterator().next().getUri()).isEqualTo(ResourceTypeDictionary.WORK.getUri());
    assertThat(resource.getDoc().size()).isEqualTo(1);
    assertThat(resource.getOutgoingEdges().size()).isEqualTo(1);
  }

  private void validateWorkTitle(Resource resource) {
    assertThat(resource.getId()).isEqualTo(-3971230252524682729L);
    assertThat(resource.getLabel()).isEqualTo("Resilience Interventions for Youth in Diverse Populations");
    assertThat(resource.getTypes().iterator().next().getUri()).isEqualTo(ResourceTypeDictionary.TITLE.getUri());
    assertThat(resource.getDoc().size()).isEqualTo(1);
    assertThat(resource.getOutgoingEdges().size()).isEqualTo(0);
  }
}
