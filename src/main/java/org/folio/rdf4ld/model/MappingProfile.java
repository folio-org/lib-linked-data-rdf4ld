package org.folio.rdf4ld.model;

import java.util.Set;

public record MappingProfile(String typeIri,
                             Set<String> topBfTypeSet,
                             String topBfNameSpace,
                             LdResourceDef topLdDef,
                             ResourceMapping topMapping) {
}
