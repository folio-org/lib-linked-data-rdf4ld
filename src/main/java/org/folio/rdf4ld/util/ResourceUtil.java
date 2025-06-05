package org.folio.rdf4ld.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
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
    var props = new ArrayList<>();
    for (PropertyDictionary property : properties) {
      var propertyString = getPropertiesString(doc, property);
      if (StringUtils.isNotBlank(propertyString)) {
        props.add(propertyString);
      }
    }
    return StringUtils.join(props, ", ");
  }

  public static String getPropertiesString(JsonNode doc, PropertyDictionary property) {
    if (nonNull(doc) && doc.has(property.getValue())) {
      Iterator<JsonNode> elements = doc.get(property.getValue()).elements();
      return StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(elements, Spliterator.ORDERED),
          false)
        .map(JsonNode::asText)
        .collect(Collectors.joining(", "));
    }
    return "";
  }

  public static String getPredicate(ResourceInternalMapping resourceMapping, int number) {
    return ofNullable(resourceMapping)
      .map(ResourceInternalMapping::getOutgoingEdges)
      .filter(oe -> oe.size() >= number)
      .map(oe -> oe.toArray(new ResourceMapping[number])[number])
      .map(ResourceMapping::getBfResourceDef)
      .map(BfResourceDef::getPredicate)
      .orElse(null);
  }

  public static Stream<SimpleIRI> getIrisByPredicate(Model model,
                                                     org.eclipse.rdf4j.model.Resource contributionResource,
                                                     String predicate) {
    return model.filter(contributionResource, Values.iri(predicate), null)
      .objects()
      .stream()
      .map(SimpleIRI.class::cast);
  }

}
