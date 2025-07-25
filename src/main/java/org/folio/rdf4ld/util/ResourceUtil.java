package org.folio.rdf4ld.util;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.rdf4ld.util.RdfUtil.toAgentRwoLink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Optional;
import java.util.Spliterator;
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
    return ofNullable(doc)
      .map(d -> d.get(property.getValue()))
      .map(JsonNode::elements)
      .stream()
      .flatMap(elements -> stream(spliteratorUnknownSize(elements, Spliterator.ORDERED), false))
      .map(JsonNode::asText)
      .collect(joining(", "));
  }

  public static JsonNode copyWithoutPreferred(Resource resource) {
    return ofNullable(resource.getDoc())
      .filter(JsonNode::isObject)
      .map(JsonNode::deepCopy)
      .map(ObjectNode.class::cast)
      .map(copiedDoc -> {
        copiedDoc.remove(RESOURCE_PREFERRED.getValue());
        return copiedDoc;
      })
      .orElse(null);
  }

  public static JsonNode addProperty(JsonNode doc, PropertyDictionary property, String value) {
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

  private static boolean isAgent(Resource resource) {
    return resource.isOfType(FAMILY)
      || resource.isOfType(JURISDICTION)
      || resource.isOfType(MEETING)
      || resource.isOfType(ORGANIZATION)
      || resource.isOfType(PERSON);
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

}
