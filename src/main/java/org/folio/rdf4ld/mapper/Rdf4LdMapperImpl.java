package org.folio.rdf4ld.mapper;

import static org.folio.rdf4ld.util.RdfUtil.selectSubjectsByTypes;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.MappingProfile;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.MappingProfileReader;
import org.springframework.stereotype.Component;

@Log4j2
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
  public Set<Resource> mapRdfToLd(Model model, MappingProfile mappingProfile) {
    return mappingProfile.getTopResourceMappings().stream()
      .flatMap(tm -> mapSingleRdfTopResourceToLd(model, tm))
      .collect(Collectors.toSet());
  }

  private Stream<Resource> mapSingleRdfTopResourceToLd(Model model, ResourceMapping topMapping) {
    var ldResourceDef = topMapping.getLdResourceDef();
    var mapper = rdfMapperUnitProvider.getMapper(ldResourceDef.getTypeSet(), ldResourceDef.getPredicate());
    var bfTypes = topMapping.getBfResourceDef().getTypeSet();
    return selectSubjectsByTypes(model, bfTypes)
      .map(resource -> mapper.mapToLd(model, resource, topMapping, null))
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

  @Override
  public Model mapLdToBibframe2Rdf(Resource resource) {
    return mapLdToRdf(resource, mappingProfileReader.getBibframe20Profile());
  }

  @Override
  public Model mapLdToRdf(Resource resource, MappingProfile mappingProfile) {
    var modelBuilder = new ModelBuilder();
    mappingProfile.getTopResourceMappings().stream()
      .filter(tm -> resource.getTypes().equals(tm.getLdResourceDef().getTypeSet()))
      .forEach(tm -> {
        var ldResourceDef = tm.getLdResourceDef();
        var mapper = rdfMapperUnitProvider.getMapper(ldResourceDef.getTypeSet(), ldResourceDef.getPredicate());
        mapper.mapToBibframe(resource, modelBuilder, tm, null);
      });
    return modelBuilder.build();
  }

}
