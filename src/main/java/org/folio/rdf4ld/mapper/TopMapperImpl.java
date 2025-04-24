package org.folio.rdf4ld.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.MapperUnitProvider;
import org.folio.rdf4ld.model.MappingProfile;
import org.folio.rdf4ld.util.DefaultMappingProfile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopMapperImpl implements TopMapper {
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final MapperUnitProvider mapperUnitProvider;

  @Override
  public Set<Resource> mapToLd(Model model) {
    return mapToLd(model, DefaultMappingProfile.get());
  }

  @Override
  public Set<Resource> mapToLd(Model model, MappingProfile mappingProfile) {
    return coreRdf2LdMapper.selectStatementsByType(model, mappingProfile.typeIri(), mappingProfile.topBfTypeSet())
      .map(st -> mapperUnitProvider.getMapper(mappingProfile.topLdDef())
        .mapToLd(model, st, mappingProfile.topMapping(), mappingProfile.topLdDef().typeSet(), mappingProfile.typeIri(),
          false)
      )
      .collect(Collectors.toSet());
  }

  @Override
  public void mapToBibframe(Resource resource, ModelBuilder modelBuilder) {
    mapToBibframe(resource, modelBuilder, DefaultMappingProfile.get());
  }

  @Override
  public void mapToBibframe(Resource resource, ModelBuilder modelBuilder, MappingProfile mappingProfile) {
    mapperUnitProvider.getMapper(mappingProfile.topLdDef())
      .mapToBibframe(resource, modelBuilder, mappingProfile.topMapping(), mappingProfile.topBfNameSpace(),
        mappingProfile.topBfTypeSet());
  }

}
