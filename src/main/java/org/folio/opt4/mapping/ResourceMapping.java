package org.folio.opt4.mapping;

import java.util.Set;
import javax.annotation.Nullable;
import org.folio.ld.dictionary.PropertyDictionary;

public record ResourceMapping(Set<PropertyMapping> properties,
                              Set<EdgeMapping> outgoingEdges,
                              Set<EdgeMapping> incomingEdges) {

    public record PropertyMapping(PropertyDictionary ldProperty,
                                  String bfProperty,
                                  // nullable because we don't need to always map a property to a different resource
                                  @Nullable LdResourceDef outgoingEdgeParentLdDef,
                                  @Nullable LdResourceDef incomingEdgeParentLdDef,
                                  @Nullable BfResourceDef edgeParentBfDef) {
    }

    public record EdgeMapping(boolean fetchRemote,
                              String bfNameSpace,
                              LdResourceDef ldResourceDef,
                              BfResourceDef bfResourceDef) {
    }

}
