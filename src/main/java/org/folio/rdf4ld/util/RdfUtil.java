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
import java.util.Collection;
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

  private static final ImmutableBiMap<@NotNull ResourceTypeDictionary, @NotNull String> LD_TO_BF_EXTRA_TYPES =
    new ImmutableBiMap.Builder<@NotNull ResourceTypeDictionary, @NotNull String>()
      .put(PERSON, "http://id.loc.gov/ontologies/bibframe/Person")
      .put(FAMILY, "http://id.loc.gov/ontologies/bibframe/Family")
      .put(ORGANIZATION, "http://id.loc.gov/ontologies/bibframe/Organization")
      .put(MEETING, "http://id.loc.gov/ontologies/bibframe/Meeting")
      .put(JURISDICTION, "http://id.loc.gov/ontologies/bibframe/Jurisdiction")
      .put(TOPIC, "http://id.loc.gov/ontologies/bibframe/Topic")
      .put(FORM, "http://id.loc.gov/ontologies/bibframe/GenreForm")
      .put(PLACE, "http://id.loc.gov/ontologies/bibframe/Place")
      .put(BOOKS, "http://id.loc.gov/ontologies/bibframe/Monograph")
      .put(CONTINUING_RESOURCES, "http://id.loc.gov/ontologies/bibframe/Serial")
      .put(TEMPORAL, "http://id.loc.gov/ontologies/bibframe/Temporal")
      .build();

  public static Stream<Value> getByPredicate(Model model,
                                             Resource rdfResource,
                                             String predicate) {
    return model.filter(rdfResource, Values.iri(predicate), null)
      .objects()
      .stream();
  }

  public static Stream<Resource> selectSubjectsByType(Model model,
                                                      Set<String> bfTypeSet) {
    return bfTypeSet.stream()
      .map(type -> model.filter(null, RDF.TYPE, Values.iri(type)))
      .flatMap(Collection::stream)
      .map(Statement::getSubject);
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
      .filter(type -> LD_TO_BF_EXTRA_TYPES.inverse().containsKey(type))
      .map(type -> LD_TO_BF_EXTRA_TYPES.inverse().get(type))
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

}
