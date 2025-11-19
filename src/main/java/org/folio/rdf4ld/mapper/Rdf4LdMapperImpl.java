package org.folio.rdf4ld.mapper;

import static org.folio.rdf4ld.util.RdfUtil.selectSubjectsByType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
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
  public Set<Resource> mapRdfToLd(Model model, ResourceMapping rm) {
    var ldResourceDef = rm.getLdResourceDef();
    var mapper = rdfMapperUnitProvider.getMapper(ldResourceDef.getTypeSet(), ldResourceDef.getPredicate());
    var bfTypes = rm.getBfResourceDef().getTypeSet();
    return selectSubjectsByType(model, bfTypes)
      .map(resource -> mapper.mapToLd(model, resource, rm, null))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }

  @Override
  public Model mapLdToBibframe2Rdf(Resource resource) {
    return mapLdToRdf(resource, mappingProfileReader.getBibframe20Profile());
  }

  @Override
  public Model mapLdToRdf(Resource resource, ResourceMapping rm) {
    var modelBuilder = new ModelBuilder();
    rdfMapperUnitProvider.getMapper(rm.getLdResourceDef().getTypeSet(), rm.getLdResourceDef().getPredicate())
      .mapToBibframe(resource, modelBuilder, rm, null);
    return modelBuilder.build();
  }

}
