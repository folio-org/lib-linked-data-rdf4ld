package org.folio.rdf4ld.mapper.unit;

import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;

public interface RdfMapperUnitProvider {

  RdfMapperUnit getMapper(Set<ResourceTypeDictionary> typeSet, PredicateDictionary predicate);

}
