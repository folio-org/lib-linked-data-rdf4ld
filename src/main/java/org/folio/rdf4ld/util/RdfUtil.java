package org.folio.rdf4ld.util;

import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;

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

}
