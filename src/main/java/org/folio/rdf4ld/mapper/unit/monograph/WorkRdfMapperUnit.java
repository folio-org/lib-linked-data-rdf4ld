package org.folio.rdf4ld.mapper.unit.monograph;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.util.RdfUtil.readAllTypes;
import static org.folio.rdf4ld.util.RdfUtil.readSupportedExtraTypes;
import static org.folio.rdf4ld.util.RdfUtil.writeExtraTypes;

import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.Rdf2LdMappingException;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = WORK, predicate = INSTANTIATES)
public class WorkRdfMapperUnit implements RdfMapperUnit {
  private static final String HUB_TYPE = "http://id.loc.gov/ontologies/bibframe/Hub";
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final FingerprintHashService hashService;
  private final LongFunction<String> resourceUrlProvider;
  private final Supplier<Optional<ResourceTypeDictionary>> defaultWorkTypeProvider;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    var allTypes = readAllTypes(model, resource).collect(toSet());
    if (allTypes.contains(HUB_TYPE)) {
      return Optional.empty();
    }
    return baseRdfMapperUnit.mapToLd(model, resource, mapping, parent)
      .map(work -> {
        setExtraTypes(model, allTypes, resource, work);
        work.setId(hashService.hash(work));
        return work;
      });
  }

  private void setExtraTypes(Model model, Set<String> allTypes,
                             org.eclipse.rdf4j.model.Resource resource, Resource work) {
    var supportedExtraTypes = readSupportedExtraTypes(model, resource);
    if (!supportedExtraTypes.isEmpty()) {
      supportedExtraTypes.forEach(work::addType);
    } else {
      if (allTypes.size() == 1) {
        defaultWorkTypeProvider.get().ifPresent(work::addType);
      } else {
        throw new Rdf2LdMappingException("Not supported Work type set: " + allTypes);
      }
    }
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, parent);
    writeExtraTypes(modelBuilder, resource, iri(resourceUrlProvider.apply(resource.getId())));
  }

}
