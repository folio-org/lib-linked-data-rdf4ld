package org.folio.rdf4ld.mapper;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceMapping;

public interface Rdf4LdMapper {

  Set<Resource> mapToLdInstance(Model model, Function<String, Optional<Resource>> resourceProvider);

  Set<Resource> mapToLd(Model model, Function<String, Optional<Resource>> resourceProvider, ResourceMapping mapping);

  Model mapToBibframeRdfInstance(Resource resource);

  Model mapToBibframeRdf(Resource resource, ResourceMapping mapping);
}
