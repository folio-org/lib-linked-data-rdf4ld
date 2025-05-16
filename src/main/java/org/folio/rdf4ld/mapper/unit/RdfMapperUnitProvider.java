package org.folio.rdf4ld.mapper.unit;

import org.folio.rdf4ld.model.LdResourceDef;

public interface RdfMapperUnitProvider {

  RdfMapperUnit getMapper(LdResourceDef ldResourceDef);

}
