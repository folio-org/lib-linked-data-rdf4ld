package org.folio.rdf4ld.mapper.unit;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceInternalMapping;

public interface RdfMapperUnit {

  Resource mapToLd(Model model,
                   org.eclipse.rdf4j.model.Resource resource,
                   ResourceInternalMapping resourceMapping,
                   Set<ResourceTypeDictionary> ldTypes,
                   Boolean localOnly,
                   Function<String, Optional<Resource>> resourceProvider);

  void mapToBibframe(Resource resource,
                     ModelBuilder modelBuilder,
                     ResourceInternalMapping resourceMapping,
                     String nameSpace,
                     Set<String> bfTypeSet);
}
