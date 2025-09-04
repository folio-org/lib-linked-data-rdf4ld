package org.folio.rdf4ld.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;

/**
 * Convert database export JSON to Resource.
 */
public class ResourceDeserializer extends StdDeserializer<Resource> {

  private static final String FIELD_ID = "id";
  private static final String FIELD_LABEL = "label";
  private static final String FIELD_DOC = "doc";
  private static final String FIELD_TYPES = "types";
  private static final String FIELD_OUTGOING_EDGES = "outgoingEdges";

  public ResourceDeserializer() {
    this(null);
  }

  public ResourceDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Resource deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return deserializeNode(node);
  }

  private Resource deserializeNode(JsonNode node) {
    var resource = new Resource();

    deserializeId(node, resource);
    deserializeLabel(node, resource);
    deserializeDoc(node, resource);
    deserializeTypes(node, resource);
    deserializeEdges(node, resource);

    return resource;
  }

  private void deserializeId(JsonNode node, Resource resource) {
    if (node.has(FIELD_ID)) {
      resource.setId(node.get(FIELD_ID).asLong());
    }
  }

  private void deserializeLabel(JsonNode node, Resource resource) {
    if (node.has(FIELD_LABEL)) {
      resource.setLabel(node.get(FIELD_LABEL).asText());
    }
  }

  private void deserializeDoc(JsonNode node, Resource resource) {
    if (node.has(FIELD_DOC) && node.get(FIELD_DOC).isObject()) {
      var doc = node.get(FIELD_DOC);
      resource.setDoc(doc);
    }
  }

  private void deserializeTypes(JsonNode node, Resource resource) {
    if (node.has(FIELD_TYPES) && node.get(FIELD_TYPES).isArray()) {
      var types = node.withArray(FIELD_TYPES);
      StreamSupport.stream(types.spliterator(), false) 
        .map(type -> ResourceTypeDictionary.fromUri(type.asText()))
        .filter(Optional::isPresent)
        .forEach(dictType -> resource.addType(dictType.get()));
    }
  }

  private void deserializeEdges(JsonNode node, Resource resource) {
    if (node.has(FIELD_OUTGOING_EDGES) && node.get(FIELD_OUTGOING_EDGES).isObject()) {
      var edges = node.withObject(FIELD_OUTGOING_EDGES);
      edges.propertyStream()
        .filter(entry -> entry.getValue().isArray())
        .forEach(entry -> makeEdges(entry.getKey(), entry.getValue(), resource));
    }
  }

  private void makeEdges(String predicate, JsonNode targets, Resource resource) {
    StreamSupport.stream(targets.spliterator(), false)
      .forEach(value -> {
        var target = deserializeNode(value);
        var dictPred = PredicateDictionary.fromUri(predicate);
        if (dictPred.isPresent()) {
          resource.addOutgoingEdge(new ResourceEdge(resource, target, dictPred.get()));
        }
      });
  }
}
