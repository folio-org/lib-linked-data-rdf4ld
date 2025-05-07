package org.folio.rdf4ld.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;

@UtilityClass
public class TestUtil {

  public static void validateProperty(JsonNode doc, String property, List<String> expected) {
    assertThat(doc.has(property)).isTrue();
    assertThat(doc.get(property).size()).isEqualTo(expected.size());
    for (int i = 0; i < expected.size(); i++) {
      assertThat(doc.get(property).get(i).asText()).isEqualTo(expected.get(i));
    }
  }

  public static void validateIncomingEdge(Resource parentResource,
                                          PredicateDictionary expectedPredicate,
                                          Set<ResourceTypeDictionary> expectedTypeSet,
                                          Map<PropertyDictionary, List<String>> expectedProperties,
                                          String expectedLabel) {
    validateEdge(parentResource, expectedPredicate, expectedTypeSet, expectedProperties, expectedLabel, false);
  }

  public static void validateOutgoingEdge(Resource parentResource,
                                          PredicateDictionary expectedPredicate,
                                          Set<ResourceTypeDictionary> expectedTypeSet,
                                          Map<PropertyDictionary, List<String>> expectedProperties,
                                          String expectedLabel) {
    validateEdge(parentResource, expectedPredicate, expectedTypeSet, expectedProperties, expectedLabel, true);
  }

  private static void validateEdge(Resource parentResource,
                                   PredicateDictionary expectedPredicate,
                                   Set<ResourceTypeDictionary> expectedTypeSet,
                                   Map<PropertyDictionary, List<String>> expectedProperties,
                                   String expectedLabel,
                                   boolean outgoingOrIncoming) {
    var edges = (outgoingOrIncoming ? parentResource.getOutgoingEdges() : parentResource.getIncomingEdges())
      .stream()
      .filter(e -> expectedPredicate.equals(e.getPredicate())
        && expectedTypeSet.equals((outgoingOrIncoming ? e.getTarget() : e.getSource()).getTypes())
        && expectedLabel.equals((outgoingOrIncoming ? e.getTarget() : e.getSource()).getLabel()))
      .collect(Collectors.toSet());
    assertThat(edges).hasSize(1);
    var edge = edges.iterator().next();
    assertThat(edge.getId()).isNull();
    assertThat(outgoingOrIncoming ? edge.getSource() : edge.getTarget()).isEqualTo(parentResource);
    var resource = outgoingOrIncoming ? edge.getTarget() : edge.getSource();
    assertThat(resource.getId()).isNotNull();
    assertThat(resource.getDoc()).isNotNull();
    for (Map.Entry<PropertyDictionary, List<String>> entry : expectedProperties.entrySet()) {
      validateProperty(resource.getDoc(), entry.getKey().getValue(), entry.getValue());
    }
  }

  public static MapperUnit emptyMapper() {
    return new MapperUnit() {
      @Override
      public Resource mapToLd(Model model, org.eclipse.rdf4j.model.Resource resource,
                              ResourceInternalMapping resourceMapping,
                              Set<ResourceTypeDictionary> ldTypes, Boolean localOnly) {
        return new Resource();
      }

      @Override
      public void mapToBibframe(Resource resource, ModelBuilder modelBuilder,
                                ResourceInternalMapping resourceMapping,
                                String nameSpace, Set<String> bfTypeSet) {
        modelBuilder.add("http://test_subject.com", "http://test_predicate.com", "test_object");
      }
    };
  }
}
