package org.folio.rdf4ld.util;

import static java.util.Optional.ofNullable;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TEMPORAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;

import com.google.common.collect.ImmutableBiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.model.ResourceMapping;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class RdfUtil {

  public static final String IRI = "@iri";

  private static final ImmutableBiMap<@NotNull ResourceTypeDictionary, @NotNull List<String>> LD_TO_BF_EXTRA_TYPES =
    new ImmutableBiMap.Builder<@NotNull ResourceTypeDictionary, @NotNull List<String>>()
      .put(PERSON, List.of("http://id.loc.gov/ontologies/bibframe/Person"))
      .put(FAMILY, List.of("http://id.loc.gov/ontologies/bibframe/Family"))
      .put(ORGANIZATION, List.of("http://id.loc.gov/ontologies/bibframe/Organization"))
      .put(MEETING, List.of("http://id.loc.gov/ontologies/bibframe/Meeting"))
      .put(JURISDICTION, List.of("http://id.loc.gov/ontologies/bibframe/Jurisdiction"))
      .put(TOPIC, List.of(
        "http://id.loc.gov/ontologies/bibframe/Topic",
        "http://www.loc.gov/mads/rdf/v1#Topic")
      )
      .put(FORM, List.of(
        "http://id.loc.gov/ontologies/bibframe/GenreForm",
        "http://www.loc.gov/mads/rdf/v1#GenreForm")
      )
      .put(PLACE, List.of(
        "http://id.loc.gov/ontologies/bibframe/Place",
        "http://www.loc.gov/mads/rdf/v1#Geographic")
      )
      .put(TEMPORAL, List.of(
        "http://id.loc.gov/ontologies/bibframe/Temporal",
        "http://www.loc.gov/mads/rdf/v1#Temporal")
      )
      .put(BOOKS, List.of("http://id.loc.gov/ontologies/bibframe/Monograph"))
      .put(CONTINUING_RESOURCES, List.of("http://id.loc.gov/ontologies/bibframe/Serial"))
      .build();

  public static Stream<Value> getByPredicate(Model model,
                                             Resource rdfResource,
                                             String predicate) {
    return model.filter(rdfResource, Values.iri(predicate), null)
      .objects()
      .stream();
  }

  public static Stream<Resource> selectSubjectsByTypes(Model model,
                                                       Set<String> bfTypeSet) {
    var allSubjects = model.subjects();
    return allSubjects.stream()
      .filter(subject -> bfTypeSet.stream()
        .allMatch(type -> model.contains(subject, RDF.TYPE, Values.iri(type)))
      );
  }

  public static Set<String> getAllTypes(Model model, Resource resource) {
    return model.filter(resource, RDF.TYPE, null)
      .stream()
      .map(Statement::getObject)
      .map(Value::stringValue)
      .collect(Collectors.toSet());
  }

  public static void linkResources(Resource from,
                                   Resource to,
                                   String bfPredicate,
                                   ModelBuilder modelBuilder) {
    modelBuilder.subject(from);
    modelBuilder.add(bfPredicate, to);
  }

  public static String toAgentRwoLink(String lccnLink) {
    return "http://id.loc.gov/rwo/agents/" + lccnLink.substring(lccnLink.lastIndexOf("/") + 1);
  }

  public static Set<ResourceTypeDictionary> readSupportedExtraTypes(Model model, Resource rdfResource) {
    return readAllTypes(model, rdfResource)
      .flatMap(type -> LD_TO_BF_EXTRA_TYPES.inverse()
        .entrySet()
        .stream()
        .filter(e -> e.getKey().contains(type))
        .map(Map.Entry::getValue)
      )
      .collect(Collectors.toSet());
  }

  public static Stream<String> readAllTypes(Model model, Resource rdfResource) {
    return model.filter(rdfResource, RDF.TYPE, null)
      .stream()
      .map(Statement::getObject)
      .map(Value::stringValue);
  }

  public static void writeExtraTypes(ModelBuilder modelBuilder,
                                     org.folio.ld.dictionary.model.Resource resource,
                                     Resource rdfResource) {
    modelBuilder.subject(rdfResource);
    resource.getTypes()
      .stream()
      .filter(LD_TO_BF_EXTRA_TYPES::containsKey)
      .map(LD_TO_BF_EXTRA_TYPES::get)
      .filter(Objects::nonNull)
      .map(List::getFirst)
      .forEach(t -> modelBuilder.add(RDF.TYPE, iri(t)));
  }

  public static BNode writeBlankNode(BNode node,
                                     org.folio.ld.dictionary.model.Resource resource,
                                     ModelBuilder modelBuilder,
                                     ResourceMapping mapping,
                                     CoreLd2RdfMapper coreLd2RdfMapper) {
    modelBuilder.subject(node);
    ofNullable(mapping.getBfResourceDef().getTypeSet())
      .stream()
      .flatMap(Collection::stream)
      .forEach(type -> modelBuilder.add(RDF.TYPE, iri(type)));
    coreLd2RdfMapper.mapProperties(resource, modelBuilder, mapping);
    return node;
  }

  public static List<Resource> extractRdfList(Model model, Resource listHead) {
    var result = new ArrayList<Resource>();
    var current = listHead;

    while (current != null && !RDF.NIL.equals(current)) {
      var firstStatements = model.filter(current, RDF.FIRST, null);
      if (firstStatements.isEmpty()) {
        break;
      }
      var first = firstStatements.iterator().next().getObject();
      if (first.isResource()) {
        result.add((Resource) first);
      }
      var restStatements = model.filter(current, RDF.REST, null);
      if (restStatements.isEmpty()) {
        break;
      }
      var rest = restStatements.iterator().next().getObject();
      current = rest.isResource() ? (Resource) rest : null;
    }
    return result;
  }

}
