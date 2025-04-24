package org.folio.rdf4ld.model;

import org.folio.ld.dictionary.PropertyDictionary;
import org.springframework.lang.Nullable;

public record PropertyMapping(PropertyDictionary ldProperty,
                              String bfProperty,
                              @Nullable LdResourceDef outgoingEdgeParentLdDef,
                              @Nullable LdResourceDef incomingEdgeParentLdDef,
                              @Nullable BfResourceDef edgeParentBfDef) {
}
