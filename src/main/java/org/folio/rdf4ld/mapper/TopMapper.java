package org.folio.rdf4ld.mapper;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;

public interface TopMapper {

  Set<Resource> mapToLd(Model model);
}
