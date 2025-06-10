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
    return mapToLd(model, mappingProfileReader.getBibframe20Profile());
  }

  @Override
  public Set<Resource> mapToLd(Model model, ResourceMapping resourceMapping) {
    var mapper = rdfMapperUnitProvider.getMapper(resourceMapping.getLdResourceDef());
    var bfTypes = resourceMapping.getBfResourceDef().getTypeSet();
    return coreRdf2LdMapper.selectSubjectsByType(model, bfTypes)
      .map(resource -> mapper.mapToLd(model, resource, resourceMapping, null))
      .collect(Collectors.toSet());
  }

  @Override
  public Model mapToBibframeRdfInstance(Resource resource) {
    return mapToBibframeRdf(resource, mappingProfileReader.getBibframe20Profile());
  }

  @Override
  public Model mapToBibframeRdf(Resource resource, ResourceMapping resourceMapping) {
    var modelBuilder = new ModelBuilder();
    rdfMapperUnitProvider.getMapper(resourceMapping.getLdResourceDef())
      .mapToBibframe(resource, modelBuilder, resourceMapping);
    return modelBuilder.build();
  }

}
