package org.folio.rdf4ld.test;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;

public class MonographUtil {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  private static final Random RANDOM = new Random();

  public static Resource createInstance(String label) {
    var instance = createResource(
      Map.of(
        DIMENSIONS, List.of("Instance dimensions 1", "Instance dimensions 2"),
        STATEMENT_OF_RESPONSIBILITY, List.of("Instance responsibilityStatement 1", "Instance responsibilityStatement 2")
      ),
      Set.of(INSTANCE),
      Map.of()
    );
    instance.setLabel(label);
    return instance;
  }

  public static Resource createPrimaryTitle() {
    var primaryTitleValue1 = "Title mainTitle 1";
    var primaryTitleValue2 = "Title mainTitle 2";
    var subTitleValue1 = "Title subTitle 1";
    var subTitleValue2 = "Title subTitle 2";

    return createResource(
      Map.of(
        PART_NAME, List.of("Title partName 1", "Title partName 2"),
        PART_NUMBER, List.of("Title partNumber 1", "Title partNumber 2"),
        MAIN_TITLE, List.of(primaryTitleValue1, primaryTitleValue2),
        NON_SORT_NUM, List.of("Title nonSortNum 1", "Title nonSortNum 2"),
        SUBTITLE, List.of(subTitleValue1, subTitleValue2)
      ),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel(primaryTitleValue1 + " " + primaryTitleValue2 + " " + subTitleValue1 + " " + subTitleValue2);
  }

  public static Resource createParallelTitle() {
    var mainTitle1 = "ParallelTitle mainTitle 1";
    var mainTitle2 = "ParallelTitle mainTitle 2";
    var subTitle1 = "ParallelTitle subTitle 1";
    var subTitle2 = "ParallelTitle subTitle 2";
    return createResource(
      Map.of(
        PART_NAME, List.of("ParallelTitle partName 1", "ParallelTitle partName 2"),
        PART_NUMBER, List.of("ParallelTitle partNumber 1", "ParallelTitle partNumber 2"),
        MAIN_TITLE, List.of(mainTitle1, mainTitle2),
        DATE, List.of("ParallelTitle date 1", "ParallelTitle date 2"),
        SUBTITLE, List.of(subTitle1, subTitle2),
        NOTE, List.of("ParallelTitle note 1", "ParallelTitle note 2")
      ),
      Set.of(PARALLEL_TITLE),
      emptyMap()
    ).setLabel(mainTitle1 + " " + mainTitle2 + " " + subTitle1 + " " + subTitle2);
  }

  public static Resource createVariantTitle() {
    var mainTitle1 = "VariantTitle mainTitle 1";
    var mainTitle2 = "VariantTitle mainTitle 2";
    var subTitle1 = "VariantTitle subTitle 1";
    var subTitle2 = "VariantTitle subTitle 2";
    return createResource(
      Map.of(
        PART_NAME, List.of("VariantTitle partName 1", "VariantTitle partName 2"),
        PART_NUMBER, List.of("VariantTitle partNumber 1", "VariantTitle partNumber 2"),
        MAIN_TITLE, List.of(mainTitle1, mainTitle2),
        DATE, List.of("VariantTitle date 1", "VariantTitle date 2"),
        SUBTITLE, List.of(subTitle1, subTitle2),
        VARIANT_TYPE, List.of("0"),
        NOTE, List.of("VariantTitle note 1", "VariantTitle note 2")
      ),
      Set.of(VARIANT_TITLE),
      emptyMap()
    ).setLabel(mainTitle1 + " " + mainTitle2 + " " + subTitle1 + " " + subTitle2);
  }

  public static Resource createResource(Map<PropertyDictionary, List<String>> propertiesDic,
                                        Set<ResourceTypeDictionary> types,
                                        Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    var resource = new Resource();
    pred2OutgoingResources.keySet()
      .stream()
      .flatMap(pred -> pred2OutgoingResources.get(pred)
        .stream()
        .map(target -> new ResourceEdge(resource, target, pred)))
      .forEach(resource::addOutgoingEdge);

    var properties = propertiesDic.entrySet().stream().collect(toMap(e -> e.getKey().getValue(), Map.Entry::getValue));
    resource.setDoc(getJsonNode(properties));
    types.forEach(t -> resource.getTypes().add(t));
    resource.setId(randomLong());
    return resource;
  }

  private static long randomLong() {
    return RANDOM.nextLong();
  }

  public static JsonNode getJsonNode(Map<String, ?> map) {
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }

}
