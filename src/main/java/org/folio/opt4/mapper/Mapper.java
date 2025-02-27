package org.folio.opt4.mapper;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;

public interface Mapper {

    Resource mapToLd(Model model, Statement statement, boolean fetchRemote);

    void mapToBibframe(Resource resource, ModelBuilder modelBuilder, String nameSpace, Set<String> bfTypeSet);
}
