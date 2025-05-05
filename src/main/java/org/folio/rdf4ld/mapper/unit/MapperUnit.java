package org.folio.rdf4ld.mapper.unit;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceMapping;

public interface MapperUnit {

  Resource mapToLd(Model model,
                   Statement statement,
                   ResourceMapping resourceMapping,
                   Set<ResourceTypeDictionary> ldTypes,
                   String typeIri,
                   Boolean fetchRemote);

  void mapToBibframe(Resource resource,
                     ModelBuilder modelBuilder,
                     ResourceMapping resourceMapping,
                     String nameSpace,
                     Set<String> bfTypeSet);
}
