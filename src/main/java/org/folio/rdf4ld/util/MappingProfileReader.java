package org.folio.rdf4ld.util;

import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rdf4ld.config.Rdf4LdObjectMapper;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MappingProfileReader {

  public static final String BASE_PATH = "mappingProfile/bibframe2.0/";
  public static final String INSTANCE = "instance.json";
  public static final String WORK = "work.json";
  public static final String TITLE = "title/title.json";
  public static final String TITLE_PARALLEL = "title/title_parallel.json";
  public static final String TITLE_VARIANT = "title/title_variant.json";
  public static final String CONTRIBUTOR = "authority/contributor.json";
  public static final String CREATOR = "authority/creator.json";
  public static final String GENRE_FORM = "authority/genre_form.json";
  public static final String SUBJECT_CONCEPT = "authority/subject_concept.json";
  public static final String LCCN = "identifier/lccn.json";
  public static final String ISBN = "identifier/isbn.json";
  public static final String EAN = "identifier/ean.json";
  public static final String DISTRIBUTION = "provision/distribution.json";
  public static final String MANUFACTURE = "provision/manufacture.json";
  public static final String PRODUCTION = "provision/production.json";
  public static final String PUBLICATION = "provision/publication.json";
  private final Rdf4LdObjectMapper objectMapper;

  public ResourceMapping getBibframe20Profile() {
    return getInstanceMapping()
      .orElse(null);
  }

  private Optional<ResourceMapping> getInstanceMapping() {
    return readResourceMapping(INSTANCE)
      .map(im -> {
        readResourceMapping(TITLE).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_PARALLEL).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_VARIANT).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(LCCN).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(ISBN).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(EAN).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(DISTRIBUTION).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(MANUFACTURE).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(PRODUCTION).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(PUBLICATION).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        getWorkMapping().ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        return im;
      });
  }

  private Optional<ResourceMapping> getWorkMapping() {
    return readResourceMapping(WORK)
      .map(wm -> {
        wm.setResourceMapping(new ResourceInternalMapping());
        readResourceMapping(TITLE).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_PARALLEL).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_VARIANT).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(CONTRIBUTOR).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(CREATOR).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(GENRE_FORM).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(SUBJECT_CONCEPT).ifPresent(wm.getResourceMapping()::addOutgoingEdgesItem);
        return wm;
      });
  }

  private Optional<ResourceMapping> readResourceMapping(String fileName) {
    try {
      var resource = new ClassPathResource(BASE_PATH + fileName);
      return Optional.of(objectMapper.readValue(resource.getInputStream(), ResourceMapping.class));
    } catch (IOException e) {
      log.error("Mapping profile reading issue for file: {}", fileName, e);
      return Optional.empty();
    }
  }
}
