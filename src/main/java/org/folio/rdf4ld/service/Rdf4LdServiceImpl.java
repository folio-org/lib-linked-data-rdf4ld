package org.folio.rdf4ld.service;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
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
  private final ObjectMapper objectMapper;

  @Override
  public Set<Resource> mapRdfToLd(InputStream input, String contentType, ResourceMapping resourceMapping) {
    var model = readModel(input, contentType);
    return rdf4LdMapper.mapRdfToLd(model, resourceMapping);
  }

  @Override
  public Set<Resource> mapBibframe2RdfToLd(InputStream input, String contentType) {
    var model = readModel(input, contentType);
    return rdf4LdMapper.mapBibframe2RdfToLd(model);
  }

  @Override
  public ByteArrayOutputStream mapLdToRdf(Resource resource, RDFFormat rdfFormat, ResourceMapping resourceMapping) {
    var model = rdf4LdMapper.mapLdToRdf(resource, resourceMapping);
    return writeModel(model, rdfFormat);
  }

  @Override
  public ByteArrayOutputStream mapLdToBibframe2Rdf(Resource resource, RDFFormat rdfFormat) {
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(resource);
    return writeModel(model, rdfFormat);
  }

  @Override
  public ByteArrayOutputStream mapLdToBibframe2Rdf(String input, RDFFormat rdfFormat)
    throws JsonProcessingException {
    var resource = objectMapper.readValue(input, Resource.class);
    return mapLdToBibframe2Rdf(resource, rdfFormat);
  }

  private Model readModel(InputStream input, String contentType) {
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

  private ByteArrayOutputStream writeModel(Model model, RDFFormat rdfFormat) {
    var out = new ByteArrayOutputStream();
    Rio.write(model, out, rdfFormat);
    return out;
  }

}
