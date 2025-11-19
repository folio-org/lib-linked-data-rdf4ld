package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;

import java.util.function.LongFunction;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = CONTRIBUTOR)
public class ContributorRdfMapperUnit extends AgentRdfMapperUnit {

  public ContributorRdfMapperUnit(LongFunction<String> resourceUrlProvider,
                                  CoreLd2RdfMapper coreLd2RdfMapper,
                                  FingerprintHashService hashService,
                                  BaseRdfMapperUnit baseRdfMapperUnit,
                                  MockLccnResourceService mockLccnResourceService) {
    super(coreLd2RdfMapper, hashService, baseRdfMapperUnit, resourceUrlProvider, mockLccnResourceService);
  }
}
