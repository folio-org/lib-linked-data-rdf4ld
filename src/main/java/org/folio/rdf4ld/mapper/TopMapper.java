package org.folio.rdf4ld.mapper;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.MappingProfile;

public interface TopMapper {

  Set<Resource> mapToLd(Model model);

  Set<Resource> mapToLd(Model model, MappingProfile mappingProfile);

  void mapToBibframe(Resource resource, ModelBuilder modelBuilder);

  void mapToBibframe(Resource resource, ModelBuilder modelBuilder, MappingProfile mappingProfile);
}
