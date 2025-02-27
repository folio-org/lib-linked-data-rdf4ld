package org.folio.opt4.mapping;

import java.util.Set;
import javax.annotation.Nullable;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;

public record LdResourceDef(Set<ResourceTypeDictionary> typeSet,
                            @Nullable PredicateDictionary predicate) {
}
