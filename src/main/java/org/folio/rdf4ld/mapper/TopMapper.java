package org.folio.rdf4ld.mapper;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceMapping;

public interface TopMapper {

  Set<Resource> mapToLd(Model model);

  Set<Resource> mapToLd(Model model, ResourceMapping mappingProfile);

  Model mapToBibframeRdf(Resource resource);

  Model mapToBibframeRdf(Resource resource, ResourceMapping mappingProfile);
}
