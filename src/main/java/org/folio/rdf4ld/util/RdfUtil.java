package org.folio.rdf4ld.util;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeMapping;
import static org.folio.rdf4ld.util.MappingUtil.getEdgePredicate;

import com.google.common.collect.ImmutableBiMap;
import java.util.Collection;
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
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;

@UtilityClass
public class RdfUtil {

  public static final ImmutableBiMap<ResourceTypeDictionary, String> AUTHORITY_LD_TO_BF_TYPES =
    new ImmutableBiMap.Builder<ResourceTypeDictionary, String>()
      .put(PERSON, "http://id.loc.gov/ontologies/bibframe/Person")
      .put(FAMILY, "http://id.loc.gov/ontologies/bibframe/Family")
      .put(ORGANIZATION, "http://id.loc.gov/ontologies/bibframe/Organization")
      .put(MEETING, "http://id.loc.gov/ontologies/bibframe/Meeting")
      .put(JURISDICTION, "http://id.loc.gov/ontologies/bibframe/Jurisdiction")
      .put(TOPIC, "http://id.loc.gov/ontologies/bibframe/Topic")
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

  public static void writeLccn(org.folio.ld.dictionary.model.Resource resource,
                               BNode node,
                               ModelBuilder modelBuilder,
                               ResourceMapping mapping,
                               int lccnEdgeNumber,
                               BaseRdfMapperUnit baseRdfMapperUnit,
                               CoreLd2RdfMapper coreLd2RdfMapper) {
    var lccnEdgeMapping = getEdgeMapping(mapping.getResourceMapping(), lccnEdgeNumber);
    var lccnEdgePredicate = getEdgePredicate(mapping.getResourceMapping(), lccnEdgeNumber);
    resource.getOutgoingEdges()
      .stream()
      .filter(oe -> oe.getPredicate() == MAP)
      .forEach(oe -> {
        baseRdfMapperUnit.mapToBibframe(oe.getTarget(), modelBuilder, lccnEdgeMapping, null);
        coreLd2RdfMapper.linkResources(modelBuilder, node,
          coreLd2RdfMapper.getResourceIri(oe.getTarget().getId().toString()), lccnEdgePredicate);
      });
  }

}
