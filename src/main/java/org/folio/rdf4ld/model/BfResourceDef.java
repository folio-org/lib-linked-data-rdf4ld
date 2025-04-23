package org.folio.rdf4ld.model;

import java.util.Set;
import org.springframework.lang.Nullable;

public record BfResourceDef(Set<String> typeSet,
                            @Nullable String predicate) {
}
