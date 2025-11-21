package org.folio.rdf4ld.service.lccn;

import java.util.Set;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;

public interface MockLccnResourceService {

  Resource mockLccnResource(String lccn);

  boolean isMockLccnResource(Resource resource);

  Set<String> gatherLccns(Set<Resource> resources);

  Resource unMockLccnResource(Resource resource, Function<String, Resource> lccnResourceProvider);
}
