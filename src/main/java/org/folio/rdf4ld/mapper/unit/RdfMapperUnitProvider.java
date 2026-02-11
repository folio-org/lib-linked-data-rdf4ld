package org.folio.rdf4ld.mapper.unit;

import java.util.Collection;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;

public interface RdfMapperUnitProvider {

  RdfMapperUnit getMapper(Collection<ResourceTypeDictionary> typeSet, PredicateDictionary predicate);

}
