package org.folio.rdf4ld.mapper.unit;

import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceMapping;

public interface RdfMapperUnit {

  Optional<Resource> mapToLd(Model model,
                             org.eclipse.rdf4j.model.Resource resource,
                             ResourceMapping resourceMapping,
                             Resource parent);

  void mapToBibframe(Resource resource,
                     ModelBuilder modelBuilder,
                     ResourceMapping resourceMapping,
                     Resource parent);


  default Resource enrichUnMockedResource(Resource resource) {
    return resource;
  }
}
