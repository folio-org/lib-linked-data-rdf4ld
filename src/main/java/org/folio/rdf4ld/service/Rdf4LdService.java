package org.folio.rdf4ld.service;

import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.ResourceMapping;

public interface Rdf4LdService {

  Set<Resource> mapToLd(InputStream inputStream,
                        String contentType,
                        Function<String, Resource> resourceProvider,
                        ResourceMapping mappingProfile);

  Set<Resource> mapToLdInstance(InputStream inputStream,
                                String contentType, Function<String, Resource> resourceProvider);

}
