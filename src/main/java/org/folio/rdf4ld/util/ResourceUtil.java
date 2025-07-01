package org.folio.rdf4ld.util;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;

@UtilityClass
public class ResourceUtil {

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

  public static ResourceMapping getEdgeMapping(ResourceInternalMapping resourceMapping, int number) {
    return ofNullable(resourceMapping)
      .map(ResourceInternalMapping::getOutgoingEdges)
      .filter(oe -> oe.size() > number)
      .map(oe -> oe.toArray(new ResourceMapping[number])[number])
      .orElse(null);
  }

  public static String getEdgePredicate(ResourceInternalMapping resourceMapping, int number) {
    return ofNullable(getEdgeMapping(resourceMapping, number))
      .map(ResourceMapping::getBfResourceDef)
      .map(BfResourceDef::getPredicate)
      .orElse(null);
  }

  public static Stream<Value> getByPredicate(Model model,
                                             org.eclipse.rdf4j.model.Resource resource,
                                             String predicate) {
    return model.filter(resource, Values.iri(predicate), null)
      .objects()
      .stream();
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

}
