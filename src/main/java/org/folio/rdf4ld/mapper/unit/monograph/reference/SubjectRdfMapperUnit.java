package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;

import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(types = CONCEPT, predicate = SUBJECT)
public class SubjectRdfMapperUnit extends ReferenceRdfMapperUnit {

  public SubjectRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit) {
    super(baseRdfMapperUnit);
  }
}
