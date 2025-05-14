package org.folio.rdf4ld.mapper.unit;

import org.folio.rdf4ld.model.LdResourceDef;

public interface MapperUnitProvider {

  MapperUnit getMapper(LdResourceDef ldResourceDef);

}
