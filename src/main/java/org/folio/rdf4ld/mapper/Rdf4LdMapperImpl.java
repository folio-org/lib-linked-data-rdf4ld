package org.folio.rdf4ld.mapper;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.util.RdfUtil.selectSubjectsByTypes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
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
    var mapped = mappingProfile.getTopResourceMappings().stream()
      .flatMap(tm -> mapSingleRdfTopResourceToLd(model, tm))
      .collect(Collectors.toSet());
    return filterDuplicates(mapped);
  }

  private Set<Resource> filterDuplicates(Set<Resource> all) {
    var instances = all.stream()
      .filter(r -> r.isOfType(ResourceTypeDictionary.INSTANCE))
      .collect(Collectors.toSet());
    var noDuplicatedWorks = all.stream()
      .filter(r -> r.isNotOfType(WORK) || workIsNotLinkedToInstances(r, instances))
      .collect(Collectors.toSet());
    return noDuplicatedWorks.stream()
      .filter(r -> r.isNotOfType(INSTANCE) || instanceIsNotReferencedByWork(r, noDuplicatedWorks))
      .collect(Collectors.toSet());
  }

  private boolean workIsNotLinkedToInstances(Resource work, Set<Resource> instances) {
    return instances.stream()
      .flatMap(i -> i.getOutgoingEdges().stream())
      .noneMatch(edge -> edge.getTarget().equals(work));
  }

  private boolean instanceIsNotReferencedByWork(Resource instance, Set<Resource> noDuplicatedWorks) {
    return noDuplicatedWorks.stream()
      .filter(r -> r.isOfType(WORK))
      .flatMap(work -> work.getIncomingEdges().stream())
      .noneMatch(edge -> edge.getSource().equals(instance));
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
      .filter(tm -> nonNull(tm.getLdResourceDef()))
      .filter(tm -> {
        var expectedTypes = new HashSet<>(tm.getLdResourceDef().getTypeSet());
        return TRUE.equals(tm.getBfResourceDef().getPartialTypesMatch())
          ? resource.getTypes().containsAll(expectedTypes)
          : resource.getTypes().equals(expectedTypes);
      })
      .forEach(tm -> {
        var ldResourceDef = tm.getLdResourceDef();
        var mapper = rdfMapperUnitProvider.getMapper(ldResourceDef.getTypeSet(), ldResourceDef.getPredicate());
        mapper.mapToBibframe(resource, modelBuilder, tm, null);
      });
    return modelBuilder.build();
  }

}
