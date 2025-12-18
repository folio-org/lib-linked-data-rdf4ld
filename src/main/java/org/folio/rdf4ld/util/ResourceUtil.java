package org.folio.rdf4ld.util;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.folio.ld.dictionary.PropertyDictionary.CHRONOLOGICAL_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.FORM_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.GENERAL_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_SUBDIVISION;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TEMPORAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.rdf4ld.util.RdfUtil.toAgentRwoLink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;

@UtilityClass
public class ResourceUtil {
  private static final String STATUS_CURRENT = "http://id.loc.gov/vocabulary/mstatus/current";
  private static final Set<ResourceTypeDictionary> AGENT_TYPES = Set.of(
    FAMILY, JURISDICTION, MEETING, ORGANIZATION, PERSON
  );
  private static final Map<ResourceTypeDictionary, PropertyDictionary> SUB_FOCUS_TYPE_TO_PROPERTY = Map.of(
    TOPIC, GENERAL_SUBDIVISION,
    FORM, FORM_SUBDIVISION,
    TEMPORAL, CHRONOLOGICAL_SUBDIVISION,
    PLACE, GEOGRAPHIC_SUBDIVISION
  );

  public static String getPrimaryMainTitle(Resource titledRresource) {
    if (isNull(titledRresource) || isNull(titledRresource.getOutgoingEdges())) {
      return "";
    }
    return titledRresource.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate() == PredicateDictionary.TITLE)
      .map(ResourceEdge::getTarget)
      .filter(t -> t.getTypes().contains(ResourceTypeDictionary.TITLE))
      .map(Resource::getDoc)
      .map(d -> getPropertiesString(d, MAIN_TITLE, SUBTITLE))
      .findFirst()
      .orElse("");
  }

  public static String getPropertiesString(JsonNode doc, PropertyDictionary... properties) {
    return Arrays.stream(properties)
      .map(property -> getPropertyString(doc, property))
      .filter(StringUtils::isNotBlank)
      .collect(joining(", "));
  }

  public static String getPropertyString(JsonNode doc, PropertyDictionary property) {
    return getPropertiesStream(doc, property)
      .collect(joining(", "));
  }

  public static Optional<String> getFirstPropertyValue(JsonNode doc, PropertyDictionary property) {
    return getPropertiesStream(doc, property)
      .findFirst();
  }

  public static JsonNode copyExcluding(Resource resource, PropertyDictionary... exclude) {
    return ofNullable(resource.getDoc())
      .filter(JsonNode::isObject)
      .map(JsonNode::deepCopy)
      .map(ObjectNode.class::cast)
      .map(copiedDoc -> {
        Arrays.stream(exclude).map(PropertyDictionary::getValue).forEach(copiedDoc::remove);
        return copiedDoc;
      })
      .orElse(null);
  }

  public static JsonNode addProperty(JsonNode doc, PropertyDictionary property, String value) {
    if (isNull(property)) {
      return doc;
    }
    return ofNullable(doc)
      .filter(JsonNode::isObject)
      .map(ObjectNode.class::cast)
      .map(d -> {
        if (d.has(property.getValue())) {
          var propertyArray = (ArrayNode) d.get(property.getValue());
          propertyArray.add(value);
        } else {
          d.putArray(property.getValue()).add(value);
        }
        return d;
      })
      .orElse((ObjectNode) doc);
  }

  public static Optional<String> getCurrentLccnLink(Resource resource) {
    var isAgent = isAgent(resource);
    return resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .filter(target -> target.isOfType(ID_LCCN))
      .filter(ResourceUtil::isCurrent)
      .map(Resource::getDoc)
      .map(d -> getPropertiesString(d, LINK))
      .map(lccnLink -> isAgent ? toAgentRwoLink(lccnLink) : lccnLink)
      .findFirst();
  }

  public static void copyLongestLabelToName(Resource resource) {
    getPropertiesStream(resource.getDoc(), LABEL)
      .max(comparingInt(String::length))
      .ifPresent(label -> resource.setDoc(addProperty(resource.getDoc(), NAME, label)));
  }

  public static PropertyDictionary getPropertyForSubFocusType(Set<ResourceTypeDictionary> types) {
    return types.stream()
      .filter(SUB_FOCUS_TYPE_TO_PROPERTY::containsKey)
      .map(SUB_FOCUS_TYPE_TO_PROPERTY::get)
      .findFirst()
      .orElse(null);
  }

  private static boolean isAgent(Resource resource) {
    return AGENT_TYPES
      .stream()
      .anyMatch(resource::isOfType);
  }

  private static boolean isCurrent(Resource resource) {
    if (resource.getOutgoingEdges().isEmpty()) {
      return true;
    }
    return resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .filter(target -> target.isOfType(STATUS))
      .map(Resource::getDoc)
      .map(d -> getPropertiesString(d, LINK))
      .anyMatch(STATUS_CURRENT::equalsIgnoreCase);
  }

  public static Resource enrichResource(Resource resource,
                                        org.eclipse.rdf4j.model.Model model,
                                        org.eclipse.rdf4j.model.Resource rdfResource,
                                        org.folio.ld.fingerprint.service.FingerprintHashService hashService) {
    copyLongestLabelToName(resource);
    RdfUtil.readSupportedExtraTypes(model, rdfResource).forEach(resource::addType);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private static Stream<String> getPropertiesStream(JsonNode doc, PropertyDictionary property) {
    return ofNullable(doc)
      .map(d -> d.get(property.getValue()))
      .map(JsonNode::elements)
      .stream()
      .flatMap(elements -> stream(spliteratorUnknownSize(elements, Spliterator.ORDERED), false))
      .map(JsonNode::asText);
  }

}
