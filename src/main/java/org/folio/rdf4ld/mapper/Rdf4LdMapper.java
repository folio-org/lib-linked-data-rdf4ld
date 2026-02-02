package org.folio.rdf4ld.mapper;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.MappingProfile;

public interface Rdf4LdMapper {

  Set<Resource> mapBibframe2RdfToLd(Model model);

  Set<Resource> mapRdfToLd(Model model, MappingProfile mappingProfile);

  Model mapLdToBibframe2Rdf(Resource resource);

  Model mapLdToRdf(Resource resource, MappingProfile mappingProfile);
}
