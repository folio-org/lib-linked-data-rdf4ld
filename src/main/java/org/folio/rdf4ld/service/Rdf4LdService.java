package org.folio.rdf4ld.service;

import java.io.InputStream;
import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.model.MappingProfile;

public interface Rdf4LdService {

  Set<Resource> mapToLd(InputStream inputStream, String contentType, MappingProfile mappingProfile);

  Set<Resource> mapToLdInstance(InputStream inputStream, String contentType);

}
