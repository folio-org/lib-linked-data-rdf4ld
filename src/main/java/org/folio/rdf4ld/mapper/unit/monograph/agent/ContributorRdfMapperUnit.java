package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;

import java.util.Optional;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = CONTRIBUTOR)
public class ContributorRdfMapperUnit extends AgentRdfMapperUnit {

  public ContributorRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                                  Function<String, Optional<Resource>> resourceProvider) {
    super(baseRdfMapperUnit, resourceProvider);
  }
}
