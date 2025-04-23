package org.folio.rdf4ld.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.TopMapping;
import org.folio.rdf4ld.util.MappingProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopMapperImpl {
  private final TopMapping topMapping = MappingProvider.getTopMapping();
  private final CoreRdf2LdMapper coreRdf2LdMapper;

  public Set<Resource> mapToLd(Model model) {
    return coreRdf2LdMapper.selectStatementsByType(model, topMapping.typeIri(), topMapping.bfTypeSet())
      .map(st -> coreRdf2LdMapper.getMapper(new LdResourceDef(topMapping.ldTypeSet(), null))
        .mapToLd(model, st, false)
      )
      .collect(Collectors.toSet());
  }

  public void mapToBibframe(Resource resource, ModelBuilder modelBuilder) {
    coreRdf2LdMapper.getMapper(new LdResourceDef(resource.getTypes(), null))
      .mapToBibframe(resource, modelBuilder, topMapping.bfNameSpace(), topMapping.bfTypeSet());
  }

}
