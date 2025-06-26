package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;

import java.util.Optional;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = CREATOR)
public class CreatorRdfMapperUnit extends AgentRdfMapperUnit {

  public CreatorRdfMapperUnit(Function<String, Optional<Resource>> resourceProvider,
                              CoreLd2RdfMapper coreLd2RdfMapper) {
    super(resourceProvider, coreLd2RdfMapper);
  }
}
