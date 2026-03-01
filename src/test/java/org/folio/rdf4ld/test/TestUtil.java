package org.folio.rdf4ld.test;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import tools.jackson.databind.JsonNode;

@UtilityClass
public class TestUtil {

  public static void validateProperty(JsonNode doc, String property, List<String> expected) {
    assertThat(doc.has(property)).isTrue();
    assertThat(doc.get(property).size()).isEqualTo(expected.size());
    for (int i = 0; i < expected.size(); i++) {
      assertThat(doc.get(property).get(i).asString()).isEqualTo(expected.get(i));
    }
  }

  public static void validateOutgoingEdge(Resource parentResource,
                                          PredicateDictionary expectedPredicate,
                                          Set<ResourceTypeDictionary> expectedTypeSet,
                                          Map<PropertyDictionary, List<String>> expectedProperties,
                                          String expectedLabel) {
    validateEdge(parentResource, expectedPredicate, expectedTypeSet, expectedProperties, expectedLabel, true, null);
  }

  public static void validateOutgoingEdge(Resource parentResource,
                                          PredicateDictionary expectedPredicate,
                                          Set<ResourceTypeDictionary> expectedTypeSet,
                                          Map<PropertyDictionary, List<String>> expectedProperties,
                                          String expectedLabel,
                                          Consumer<Resource> extraValidator) {
    validateEdge(parentResource, expectedPredicate, expectedTypeSet, expectedProperties, expectedLabel, true,
      extraValidator);
  }

  private static void validateEdge(Resource parentResource,
                                   PredicateDictionary expectedPredicate,
                                   Set<ResourceTypeDictionary> expectedTypeSet,
                                   Map<PropertyDictionary, List<String>> expectedProperties,
                                   String expectedLabel,
                                   boolean outgoingOrIncoming,
                                   Consumer<Resource> extraValidator) {
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
    if (expectedProperties.isEmpty()) {
      assertThat(isNull(resource.getDoc()) || resource.getDoc().isEmpty()).isTrue();
    } else {
      assertThat(resource.getDoc().size()).isEqualTo(expectedProperties.size());
    }
    expectedProperties.forEach((key, value) -> validateProperty(resource.getDoc(), key.getValue(), value));
    if (nonNull(extraValidator)) {
      extraValidator.accept(resource);
    }
  }

  public static RdfMapperUnit emptyMapper() {
    return new RdfMapperUnit() {
      @Override
      public Optional<Resource> mapToLd(Model model, org.eclipse.rdf4j.model.Resource resource,
                                        ResourceMapping resourceMapping,
                                        Resource parent) {
        return Optional.of(new Resource());
      }

      @Override
      public void mapToBibframe(Resource resource,
                                ModelBuilder modelBuilder,
                                ResourceMapping resourceMapping,
                                Resource parent) {
        modelBuilder.add("http://test_subject.com", "http://test_predicate.com", "test_object");
      }
    };
  }

  public static void validateResourceWithTitles(Resource resource, String prefix, String expectedLink) {
    assertThat(resource.getId()).isNotNull();
    validateProperty(resource.getDoc(), LINK.getValue(), List.of(expectedLink));
    assertThat(resource.getIncomingEdges()).isEmpty();
    validateOutgoingEdge(resource, TITLE, Set.of(ResourceTypeDictionary.TITLE),
      Map.of(
        MAIN_TITLE, List.of(prefix + "Title mainTitle 1", prefix + "Title mainTitle 2"),
        PART_NAME, List.of(prefix + "Title partName 1", prefix + "Title partName 2"),
        PART_NUMBER, List.of(prefix + "Title partNumber 1", prefix + "Title partNumber 2"),
        SUBTITLE, List.of(prefix + "Title subTitle 1", prefix + "Title subTitle 2"),
        NON_SORT_NUM, List.of(prefix + "Title nonSortNum 1", prefix + "Title nonSortNum 2")
      ), getTitleLabel(prefix, "Title")
    );
    validateOutgoingEdge(resource, TITLE, Set.of(PARALLEL_TITLE),
      Map.of(
        MAIN_TITLE, List.of(prefix + "ParallelTitle mainTitle 1", prefix + "ParallelTitle mainTitle 2"),
        PART_NAME, List.of(prefix + "ParallelTitle partName 1", prefix + "ParallelTitle partName 2"),
        PART_NUMBER, List.of(prefix + "ParallelTitle partNumber 1", prefix + "ParallelTitle partNumber 2"),
        SUBTITLE, List.of(prefix + "ParallelTitle subTitle 1", prefix + "ParallelTitle subTitle 2"),
        DATE, List.of(prefix + "ParallelTitle date 1", prefix + "ParallelTitle date 2"),
        NOTE, List.of(prefix + "ParallelTitle note 1", prefix + "ParallelTitle note 2")
      ), getTitleLabel(prefix, "ParallelTitle")
    );
    validateOutgoingEdge(resource, TITLE, Set.of(VARIANT_TITLE),
      Map.of(
        MAIN_TITLE, List.of(prefix + "VariantTitle mainTitle 1", prefix + "VariantTitle mainTitle 2"),
        PART_NAME, List.of(prefix + "VariantTitle partName 1", prefix + "VariantTitle partName 2"),
        PART_NUMBER, List.of(prefix + "VariantTitle partNumber 1", prefix + "VariantTitle partNumber 2"),
        SUBTITLE, List.of(prefix + "VariantTitle subTitle 1", prefix + "VariantTitle subTitle 2"),
        DATE, List.of(prefix + "VariantTitle date 1", prefix + "VariantTitle date 2"),
        NOTE, List.of(prefix + "VariantTitle note 1", prefix + "VariantTitle note 2"),
        VARIANT_TYPE, List.of("0")
      ), getTitleLabel(prefix, "VariantTitle")
    );
  }

  public static String getTitleLabel(String prefix, String titleType) {
    return prefix + titleType + " mainTitle 1, " + prefix + titleType + " mainTitle 2, "
      + prefix + titleType + " subTitle 1, " + prefix + titleType + " subTitle 2, "
      + prefix + titleType + " partNumber 1, " + prefix + titleType + " partNumber 2, "
      + prefix + titleType + " partName 1, " + prefix + titleType + " partName 2";
  }

  public static void validateResourceWithGivenEdges(Resource resource, ResourceEdge... edges) {
    assertThat(resource.getId()).isNotNull();
    assertThat(resource.getIncomingEdges()).isEmpty();
    assertThat(resource.getOutgoingEdges()).containsOnly(edges);
  }

  public static String toJsonLdString(Model model) {
    try (var out = new ByteArrayOutputStream()) {
      Rio.write(model, out, RDFFormat.JSONLD);
      return out.toString();
    } catch (IOException e) {
      fail("Failed to write a model to output stream: " + e.getMessage());
      return null;
    }
  }

  public static Resource mockLccnResource(String lccn) {
    return new Resource()
      .setId((long) lccn.hashCode())
      .setLabel(lccn)
      .addType(MOCKED_RESOURCE);
  }

  public static void validateAgent(Resource parentResource,
                                    String labelProperty,
                                    String label,
                                    PredicateDictionary predicate,
                                    Set<ResourceTypeDictionary> types) {
    var expectedProperties = Map.of(
      LABEL, List.of(labelProperty),
      NAME, List.of(labelProperty)
    );
    validateOutgoingEdge(parentResource, predicate, types, expectedProperties, label,
      agent -> {
        assertThat(agent.getId()).isNotNull();
        assertThat(agent.getIncomingEdges()).isEmpty();
        assertThat(agent.getOutgoingEdges()).isEmpty();
      }
    );
  }
}
