package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;

import java.util.function.LongFunction;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(types = FORM, predicate = GENRE)
public class GenreFormRdfMapperUnit extends ReferenceRdfMapperUnit {

  public GenreFormRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                                FingerprintHashService hashService,
                                MockLccnResourceService mockLccnResourceService,
                                LongFunction<String> resourceUrlProvider,
                                CoreLd2RdfMapper coreLd2RdfMapper) {
    super(baseRdfMapperUnit, hashService, mockLccnResourceService, resourceUrlProvider, coreLd2RdfMapper);
  }
}
