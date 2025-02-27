package org.folio.opt4.mapping;

import java.util.Set;
import org.folio.ld.dictionary.ResourceTypeDictionary;

public record TopMapping(String typeIri,
                         Set<ResourceTypeDictionary> ldTypeSet,
                         Set<String> bfTypeSet,
                         String bfNameSpace) {
}
