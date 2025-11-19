package org.folio.rdf4ld.service.lccn;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import java.util.LinkedHashSet;
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
  private static final String LABEL_PREFIX = "LCCN_RESOURCE_MOCK_";
  private final FingerprintHashService fingerprintHashService;
  private final RdfMapperUnitProvider rdfMapperUnitProvider;

  @Override
  public Resource mockLccnResource(String lccn) {
    var label = LABEL_PREFIX + lccn;
    return new Resource()
      .setId((long) label.hashCode())
      .setLabel(label);
  }

  @Override
  public boolean isMockLccnResource(Resource resource) {
    return resource.getLabel().startsWith(LABEL_PREFIX);
  }


  @Override
  public Set<String> gatherLccns(Set<Resource> resources) {
    return gatherMockLccnsRecursive(resources.stream())
      .collect(toSet());
  }

  @Override
  public Set<Resource> unMockLccnResources(Set<Resource> resources, Function<String, Resource> lccnResourceProvider) {
    return resources.stream()
      .map(r -> {
        if (isMockLccnResource(r)) {
          return unMockSingleLccnResource(r, lccnResourceProvider, null);
        }
        unMockLccnResourceEdgesRecursive(r, lccnResourceProvider);
        return r;
      })
      .collect(toCollection(LinkedHashSet::new));
  }

  private Stream<String> gatherMockLccnsRecursive(Stream<Resource> resources) {
    return resources
      .flatMap(r -> isMockLccnResource(r)
        ? Stream.of(getLccn(r))
        : gatherMockLccnsRecursive(r.getOutgoingEdges().stream().map(ResourceEdge::getTarget)));
  }

  private boolean unMockLccnResourceEdgesRecursive(Resource parent,
                                                   Function<String, Resource> lccnResourceProvider) {
    var isUnMocked = new AtomicBoolean(false);
    parent.getOutgoingEdges()
      .forEach(oe -> {
        if (isMockLccnResource(oe.getTarget())) {
          var unMockedTarget = unMockSingleLccnResource(oe.getTarget(), lccnResourceProvider, oe.getPredicate());
          oe.setTarget(unMockedTarget);
          isUnMocked.set(true);
        } else {
          isUnMocked.set(unMockLccnResourceEdgesRecursive(oe.getTarget(), lccnResourceProvider));
        }
      });
    if (isUnMocked.get()) {
      parent.setId(fingerprintHashService.hash(parent));
    }
    return isUnMocked.get();
  }


  private Resource unMockSingleLccnResource(Resource resource,
                                            Function<String, Resource> lccnResourceProvider,
                                            PredicateDictionary predicate) {
    var realResource = lccnResourceProvider.apply(getLccn(resource));
    var mapperUnit = rdfMapperUnitProvider.getMapper(Set.of(), predicate);
    return mapperUnit.enrichUnMockedResource(realResource);
  }

  private String getLccn(Resource resource) {
    return resource.getLabel().replaceAll(LABEL_PREFIX, "");
  }

}
