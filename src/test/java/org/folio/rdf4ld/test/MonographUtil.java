package org.folio.rdf4ld.test;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

  public static final String STATUS_CURRENT = "current";
  public static final String STATUS_CANCELLED = "cancinv";
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

  public static Resource createPrimaryTitle(String prefix) {
    var primaryTitleValue1 = prefix + "Title mainTitle 1";
    var primaryTitleValue2 = prefix + "Title mainTitle 2";
    var subTitleValue1 = prefix + "Title subTitle 1";
    var subTitleValue2 = prefix + "Title subTitle 2";

    return createResource(
      Map.of(
        PART_NAME, List.of(prefix + "Title partName 1", prefix + "Title partName 2"),
        PART_NUMBER, List.of(prefix + "Title partNumber 1", prefix + "Title partNumber 2"),
        MAIN_TITLE, List.of(primaryTitleValue1, primaryTitleValue2),
        NON_SORT_NUM, List.of(prefix + "Title nonSortNum 1", prefix + "Title nonSortNum 2"),
        SUBTITLE, List.of(subTitleValue1, subTitleValue2)
      ),
      Set.of(TITLE),
      emptyMap()
    ).setLabel(primaryTitleValue1 + " " + primaryTitleValue2 + " " + subTitleValue1 + " " + subTitleValue2);
  }

  public static Resource createParallelTitle(String prefix) {
    var mainTitle1 = prefix + "ParallelTitle mainTitle 1";
    var mainTitle2 = prefix + "ParallelTitle mainTitle 2";
    var subTitle1 = prefix + "ParallelTitle subTitle 1";
    var subTitle2 = prefix + "ParallelTitle subTitle 2";
    return createResource(
      Map.of(
        PART_NAME, List.of(prefix + "ParallelTitle partName 1", prefix + "ParallelTitle partName 2"),
        PART_NUMBER, List.of(prefix + "ParallelTitle partNumber 1", prefix + "ParallelTitle partNumber 2"),
        MAIN_TITLE, List.of(mainTitle1, mainTitle2),
        DATE, List.of(prefix + "ParallelTitle date 1", prefix + "ParallelTitle date 2"),
        SUBTITLE, List.of(subTitle1, subTitle2),
        NOTE, List.of(prefix + "ParallelTitle note 1", prefix + "ParallelTitle note 2")
      ),
      Set.of(PARALLEL_TITLE),
      emptyMap()
    ).setLabel(mainTitle1 + " " + mainTitle2 + " " + subTitle1 + " " + subTitle2);
  }

  public static Resource createVariantTitle(String prefix) {
    var mainTitle1 = prefix + "VariantTitle mainTitle 1";
    var mainTitle2 = prefix + "VariantTitle mainTitle 2";
    var subTitle1 = prefix + "VariantTitle subTitle 1";
    var subTitle2 = prefix + "VariantTitle subTitle 2";
    return createResource(
      Map.of(
        PART_NAME, List.of(prefix + "VariantTitle partName 1", prefix + "VariantTitle partName 2"),
        PART_NUMBER, List.of(prefix + "VariantTitle partNumber 1", prefix + "VariantTitle partNumber 2"),
        MAIN_TITLE, List.of(mainTitle1, mainTitle2),
        DATE, List.of(prefix + "VariantTitle date 1", prefix + "VariantTitle date 2"),
        SUBTITLE, List.of(subTitle1, subTitle2),
        VARIANT_TYPE, List.of("0"),
        NOTE, List.of(prefix + "VariantTitle note 1", prefix + "VariantTitle note 2")
      ),
      Set.of(VARIANT_TITLE),
      emptyMap()
    ).setLabel(mainTitle1 + " " + mainTitle2 + " " + subTitle1 + " " + subTitle2);
  }

  public static Resource createWork(String label) {
    var work = createResource(
      Map.of(),
      Set.of(WORK),
      Map.of()
    );
    work.setLabel(label);
    return work;
  }

  public static Resource createAgent(String lccn,
                                     boolean isCurrent,
                                     List<ResourceTypeDictionary> types,
                                     String label) {
    return createResource(
      Map.of(LABEL, List.of(label)),
      new LinkedHashSet<>(types),
      Map.of(MAP, List.of(createLccn(lccn, "http://id.loc.gov/rwo/agents/", isCurrent)))
    ).setLabel(label);
  }

  public static Resource createConceptAgent(String lccn,
                                            boolean isCurrent,
                                            List<ResourceTypeDictionary> types,
                                            String label) {
    return createConcept(types, List.of(createAgent(lccn, isCurrent, types, label)), List.of(), label);
  }

  public static Resource createConceptTopic(String lccn,
                                            boolean isCurrent,
                                            String label) {
    return createConcept(List.of(TOPIC), List.of(createTopic(lccn, isCurrent, label)), List.of(), label);
  }

  public static Resource createConcept(List<ResourceTypeDictionary> types,
                                       List<Resource> focuses,
                                       List<Resource> subFocuses,
                                       String label) {
    var edges = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    edges.put(FOCUS, focuses);
    edges.put(SUB_FOCUS, subFocuses);
    var conceptTypes = new LinkedHashSet<>(types);
    conceptTypes.add(CONCEPT);
    return createResource(
      Map.of(LABEL, List.of(label)),
      conceptTypes,
      edges
    ).setLabel(label);
  }

  public static Resource createTopic(String lccn,
                                     boolean isCurrent,
                                     String label) {
    return createResource(
      Map.of(LABEL, List.of(label)),
      Set.of(TOPIC),
      Map.of(MAP, List.of(createLccn(lccn, "http://id.loc.gov/authorities/subjects/", isCurrent)))
    ).setLabel(label);
  }

  public static Resource createLccn(String lccn, String lccnNameSpace, boolean isCurrent) {
    return createResource(
      Map.of(NAME, List.of(lccn), LINK, List.of(lccnNameSpace + lccn)),
      Set.of(IDENTIFIER, ID_LCCN),
      Map.of(STATUS, List.of(createStatus(isCurrent)))
    ).setLabel(lccn);
  }

  private static Resource createStatus(boolean isCurrent) {
    var status = isCurrent ? STATUS_CURRENT : STATUS_CANCELLED;
    return createResource(
      Map.of(
        LABEL, List.of(status),
        LINK, List.of("http://id.loc.gov/vocabulary/mstatus/" + status)
      ),
      Set.of(ResourceTypeDictionary.STATUS),
      emptyMap()
    ).setLabel(status);
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
    resource.setTypes(types);
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
