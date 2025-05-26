package org.folio.rdf4ld.service;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Rdf4LdServiceImpl implements Rdf4LdService {

  private final Rdf4LdMapper rdf4LdMapper;

  @Override
  public Set<Resource> mapToLd(InputStream input,
                               String contentType,
                               Function<String, Optional<Resource>> resourceProvider,
                               ResourceMapping mappingProfile) {
    var model = readModel(input, contentType);
    return rdf4LdMapper.mapToLd(model, resourceProvider, mappingProfile);
  }

  @Override
  public Set<Resource> mapToLdInstance(InputStream input,
                                       String contentType,
                                       Function<String, Optional<Resource>> resourceProvider) {
    var model = readModel(input, contentType);
    return rdf4LdMapper.mapToLdInstance(model, resourceProvider);
  }

  private static Model readModel(InputStream input, String contentType) {
    if (isNull(input)) {
      throw new IllegalArgumentException("Input stream is null");
    }

    var rdfFormat = Rio.getParserFormatForMIMEType(contentType)
      .orElseThrow(() -> new IllegalArgumentException("Unsupported RDF format: " + contentType));

    Model model;
    try {
      model = Rio.parse(input, "", rdfFormat);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unreadable RDF data", e);
    } catch (RDFParseException e) {
      throw new IllegalArgumentException("RDF parsing error", e);
    }
    return model;
  }

}
