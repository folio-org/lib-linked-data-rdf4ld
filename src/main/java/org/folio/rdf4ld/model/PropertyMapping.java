package org.folio.rdf4ld.model;

import org.folio.ld.dictionary.PropertyDictionary;
import org.springframework.lang.Nullable;

public record PropertyMapping(PropertyDictionary ldProperty,
                              String bfProperty,
                              // nullable because we don't need to always map a property to a different resource
                              @Nullable LdResourceDef outgoingEdgeParentLdDef,
                              @Nullable LdResourceDef incomingEdgeParentLdDef,
                              @Nullable BfResourceDef edgeParentBfDef) {
}
