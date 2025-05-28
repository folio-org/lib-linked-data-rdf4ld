package org.folio.rdf4ld.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.MappingProfileReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rdf4LdMapperImpl implements Rdf4LdMapper {
  private final MappingProfileReader mappingProfileReader;
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final RdfMapperUnitProvider rdfMapperUnitProvider;

  @Override
  public Set<Resource> mapToLdInstance(Model model) {
    return mapToLd(model, mappingProfileReader.getInstanceBibframe20Profile());
  }

  @Override
  public Set<Resource> mapToLd(Model model, ResourceMapping mappingProfile) {
    var mapper = rdfMapperUnitProvider.getMapper(mappingProfile.getLdResourceDef());
    var ldTypes = mappingProfile.getLdResourceDef().getTypeSet();
    var bfTypes = mappingProfile.getBfResourceDef().getTypeSet();
    return coreRdf2LdMapper.selectSubjectsByType(model, bfTypes)
      .map(resource -> mapper.mapToLd(model, resource, mappingProfile.getResourceMapping(), ldTypes, true))
      .collect(Collectors.toSet());
  }

  @Override
  public Model mapToBibframeRdfInstance(Resource resource) {
    return mapToBibframeRdf(resource, mappingProfileReader.getInstanceBibframe20Profile());
  }

  @Override
  public Model mapToBibframeRdf(Resource resource, ResourceMapping mappingProfile) {
    var modelBuilder = new ModelBuilder();
    rdfMapperUnitProvider.getMapper(mappingProfile.getLdResourceDef())
      .mapToBibframe(resource, modelBuilder, mappingProfile.getResourceMapping(), mappingProfile.getBfNameSpace(),
        mappingProfile.getBfResourceDef().getTypeSet());
    return modelBuilder.build();
  }

}
