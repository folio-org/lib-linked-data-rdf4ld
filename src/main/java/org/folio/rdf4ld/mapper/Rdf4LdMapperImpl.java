package org.folio.rdf4ld.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.MapperUnitProvider;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.DefaultMappingProfileReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rdf4LdMapperImpl implements Rdf4LdMapper {
  private final DefaultMappingProfileReader defaultMappingProfileReader;
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final MapperUnitProvider mapperUnitProvider;

  @Override
  public Set<Resource> mapToLdInstance(Model model) {
    return mapToLd(model, defaultMappingProfileReader.getInstanceBibframe20Profile());
  }

  @Override
  public Set<Resource> mapToLd(Model model, ResourceMapping mappingProfile) {
    return coreRdf2LdMapper.selectStatementsByType(model, mappingProfile.getBfResourceDef().getTypeSet())
      .map(st -> mapperUnitProvider.getMapper(mappingProfile.getLdResourceDef())
        .mapToLd(model, st, mappingProfile.getResourceMapping(), mappingProfile.getLdResourceDef().getTypeSet(),
                true)
      )
      .collect(Collectors.toSet());
  }

  @Override
  public Model mapToBibframeRdfInstance(Resource resource) {
    return mapToBibframeRdf(resource, defaultMappingProfileReader.getInstanceBibframe20Profile());
  }

  @Override
  public Model mapToBibframeRdf(Resource resource, ResourceMapping mappingProfile) {
    var modelBuilder = new ModelBuilder();
    mapperUnitProvider.getMapper(mappingProfile.getLdResourceDef())
      .mapToBibframe(resource, modelBuilder, mappingProfile.getResourceMapping(), mappingProfile.getBfNameSpace(),
        mappingProfile.getBfResourceDef().getTypeSet());
    return modelBuilder.build();
  }

}
