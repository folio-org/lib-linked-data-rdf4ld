package org.folio.rdf4ld.util;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;

@UtilityClass
public class RdfUtil {

  public static final String AGENTS_NAMESPACE = "http://id.loc.gov/rwo/agents/";
  public static final String SUBJECTS_NAMESPACE = "http://id.loc.gov/authorities/subjects/";

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

}
