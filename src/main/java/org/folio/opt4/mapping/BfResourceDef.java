package org.folio.opt4.mapping;

import java.util.Set;
import javax.annotation.Nullable;

public record BfResourceDef(Set<String> typeSet,
                            @Nullable String predicate) {
}
