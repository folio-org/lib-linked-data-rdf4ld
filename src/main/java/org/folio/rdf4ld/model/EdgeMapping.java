package org.folio.rdf4ld.model;

public record EdgeMapping(ResourceMapping resourceMapping,
                          boolean fetchRemote,
                          String bfNameSpace,
                          LdResourceDef ldResourceDef,
                          BfResourceDef bfResourceDef) {
}
