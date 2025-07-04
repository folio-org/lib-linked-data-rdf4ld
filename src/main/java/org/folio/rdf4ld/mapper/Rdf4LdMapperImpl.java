package org.folio.rdf4ld.mapper;

import static org.folio.rdf4ld.util.RdfUtil.selectSubjectsByType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.MappingProfileReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rdf4LdMapperImpl implements Rdf4LdMapper {
  private final MappingProfileReader mappingProfileReader;
  private final RdfMapperUnitProvider rdfMapperUnitProvider;

  @Override
  public Set<Resource> mapBibframe2RdfToLd(Model model) {
    return mapRdfToLd(model, mappingProfileReader.getBibframe20Profile());
  }

  @Override
  public Set<Resource> mapRdfToLd(Model model, ResourceMapping resourceMapping) {
    var mapper = rdfMapperUnitProvider.getMapper(resourceMapping.getLdResourceDef());
    var bfTypes = resourceMapping.getBfResourceDef().getTypeSet();
    return selectSubjectsByType(model, bfTypes)
      .map(resource -> mapper.mapToLd(model, resource, resourceMapping, null))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }

  @Override
  public Model mapLdToBibframe2Rdf(Resource resource) {
    return mapLdToRdf(resource, mappingProfileReader.getBibframe20Profile());
  }

  @Override
  public Model mapLdToRdf(Resource resource, ResourceMapping resourceMapping) {
    var modelBuilder = new ModelBuilder();
    rdfMapperUnitProvider.getMapper(resourceMapping.getLdResourceDef())
      .mapToBibframe(resource, modelBuilder, resourceMapping, null);
    return modelBuilder.build();
  }

}
