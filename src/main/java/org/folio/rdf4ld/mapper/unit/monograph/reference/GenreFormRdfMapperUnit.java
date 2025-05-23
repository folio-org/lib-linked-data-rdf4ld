package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;

import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(types = FORM, predicate = GENRE)
public class GenreFormRdfMapperUnit extends ReferenceRdfMapperUnit {

  public GenreFormRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit) {
    super(baseRdfMapperUnit);
  }
}
