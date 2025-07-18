package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = CREATOR)
public class CreatorRdfMapperUnit extends AgentRdfMapperUnit {

  public CreatorRdfMapperUnit(Supplier<String> baseUrlProvider,
                              CoreLd2RdfMapper coreLd2RdfMapper,
                              FingerprintHashService hashService,
                              BaseRdfMapperUnit baseRdfMapperUnit,
                              Function<String, Optional<Resource>> resourceProvider) {
    super(baseUrlProvider, coreLd2RdfMapper, hashService, baseRdfMapperUnit, resourceProvider);
  }
}
