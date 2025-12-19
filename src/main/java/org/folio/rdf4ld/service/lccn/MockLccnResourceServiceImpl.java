package org.folio.rdf4ld.service.lccn;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.rdf4ld.util.ResourceUtil.getFirstPropertyValue;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockLccnResourceServiceImpl implements MockLccnResourceService {
  private final FingerprintHashService fingerprintHashService;
  private final RdfMapperUnitProvider rdfMapperUnitProvider;

  @Override
  public Resource mockLccnResource(Resource mappedResource, String lccn) {
    return ofNullable(mappedResource)
      .orElseGet(Resource::new)
      .setId((long) lccn.hashCode())
      .setLabel(lccn)
      .addType(MOCKED_RESOURCE);
  }

  @Override
  public boolean isMockLccnResource(Resource resource) {
    return resource.isOfType(MOCKED_RESOURCE);
  }


  @Override
  public Set<String> gatherLccns(Set<Resource> resources) {
    return gatherMockLccnsRecursive(resources.stream())
      .collect(toSet());
  }

  @Override
  public Resource unMockLccnEdges(Resource resource, Function<String, Optional<Resource>> lccnResourceProvider) {
    unMockLccnResourceEdgesRecursive(resource, lccnResourceProvider);
    return resource;
  }

  private Stream<String> gatherMockLccnsRecursive(Stream<Resource> resources) {
    return resources
      .flatMap(r -> isMockLccnResource(r)
        ? Stream.of(r.getLabel())
        : gatherMockLccnsRecursive(r.getOutgoingEdges().stream().map(ResourceEdge::getTarget)));
  }

  private boolean unMockLccnResourceEdgesRecursive(Resource parent,
                                                   Function<String, Optional<Resource>> lccnResourceProvider) {
    var isUnMocked = new AtomicBoolean(false);
    var newOutgoingEdges = parent.getOutgoingEdges().stream()
      .map(oe -> {
        var target = oe.getTarget();
        boolean isUnmockedEdges = unMockLccnResourceEdgesRecursive(target, lccnResourceProvider);
        if (isMockLccnResource(target)) {
          isUnMocked.set(true);
          return unMockSingleLccnResource(target, lccnResourceProvider, oe.getPredicate())
            .map(unMockedTarget -> new ResourceEdge(parent, unMockedTarget, oe.getPredicate()));
        } else {
          isUnMocked.set(isUnmockedEdges);
          return isUnMocked.get() ? of(new ResourceEdge(parent, target, oe.getPredicate())) : of(oe);
        }
      })
      .flatMap(Optional::stream)
      .collect(toSet());
    if (isUnMocked.get() || !newOutgoingEdges.equals(parent.getOutgoingEdges())) {
      parent.setOutgoingEdges(newOutgoingEdges);
      parent.setId(fingerprintHashService.hash(parent));
    }
    return isUnMocked.get();
  }


  private Optional<Resource> unMockSingleLccnResource(Resource resource,
                                                      Function<String, Optional<Resource>> lccnResourceProvider,
                                                      PredicateDictionary predicate) {
    return lccnResourceProvider.apply(resource.getLabel())
      .or(() -> unMockWithLocalData(resource))
      .map(realResource -> rdfMapperUnitProvider.getMapper(Set.of(), predicate)
        .enrichUnMockedResource(realResource));
  }

  private Optional<Resource> unMockWithLocalData(Resource resource) {
    resource.getTypes().remove(MOCKED_RESOURCE);
    if (resource.getTypes().isEmpty()) {
      return empty();
    }
    getFirstPropertyValue(resource.getDoc(), LABEL).ifPresent(resource::setLabel);
    resource.setId(fingerprintHashService.hash(resource));
    return of(resource);
  }

}
