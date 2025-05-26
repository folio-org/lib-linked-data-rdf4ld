package org.folio.rdf4ld.mapper;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
  public Set<Resource> mapToLdInstance(Model model, Function<String, Optional<Resource>> resourceProvider) {
    return mapToLd(model, resourceProvider, mappingProfileReader.getInstanceBibframe20Profile());
  }

  @Override
  public Set<Resource> mapToLd(Model model,
                               Function<String, Optional<Resource>> resourceProvider,
                               ResourceMapping mapping) {
    var mapper = rdfMapperUnitProvider.getMapper(mapping.getLdResourceDef());
    var ldTypes = mapping.getLdResourceDef().getTypeSet();
    var bfTypes = mapping.getBfResourceDef().getTypeSet();
    return coreRdf2LdMapper.selectSubjectsByType(model, bfTypes)
      .map(resource -> mapper.mapToLd(model, resource, mapping.getResourceMapping(), ldTypes,
        true, resourceProvider))
      .collect(Collectors.toSet());
  }

  @Override
  public Model mapToBibframeRdfInstance(Resource resource) {
    return mapToBibframeRdf(resource, mappingProfileReader.getInstanceBibframe20Profile());
  }

  @Override
  public Model mapToBibframeRdf(Resource resource, ResourceMapping mapping) {
    var modelBuilder = new ModelBuilder();
    rdfMapperUnitProvider.getMapper(mapping.getLdResourceDef())
      .mapToBibframe(resource, modelBuilder, mapping.getResourceMapping(), mapping.getBfNameSpace(),
        mapping.getBfResourceDef().getTypeSet());
    return modelBuilder.build();
  }

}
