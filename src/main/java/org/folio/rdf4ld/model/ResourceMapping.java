package org.folio.rdf4ld.model;

import java.util.Set;

public record ResourceMapping(Set<PropertyMapping> properties,
                              Set<EdgeMapping> outgoingEdges,
                              Set<EdgeMapping> incomingEdges) {

}
