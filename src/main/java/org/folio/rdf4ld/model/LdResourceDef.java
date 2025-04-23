package org.folio.rdf4ld.model;

import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.springframework.lang.Nullable;

public record LdResourceDef(Set<ResourceTypeDictionary> typeSet,
                            @Nullable PredicateDictionary predicate) {
}
