package org.folio.opt4.mapper;

import static org.folio.opt4.util.ImportMapperUtil.getMapper;
import static org.folio.opt4.util.ImportMapperUtil.selectStatementsByType;

import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.opt4.mapping.LdResourceDef;
import org.folio.opt4.util.MappingProvider;
import org.folio.opt4.mapping.TopMapping;

public class TopMapper {
    private final TopMapping topMapping;

    public TopMapper() {
        this.topMapping = MappingProvider.getTopMapping();
    }

    public Set<Resource> mapToLd(Model model) {
        return selectStatementsByType(model, topMapping.typeIri(), topMapping.bfTypeSet())
                .map(st -> getMapper(new LdResourceDef(topMapping.ldTypeSet(), null))
                        .mapToLd(model, st, false)
                )
                .collect(Collectors.toSet());
    }

    public void mapToBibframe(Resource resource, ModelBuilder modelBuilder) {
        getMapper(new LdResourceDef(resource.getTypes(), null))
                .mapToBibframe(resource, modelBuilder, topMapping.bfNameSpace(), topMapping.bfTypeSet());
    }

}
