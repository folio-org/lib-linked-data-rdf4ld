package org.folio.rdf4ld.util;

import static org.folio.rdf4ld.util.JsonUtil.getJsonMapper;

import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rdf4ld.model.MappingProfile;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Component
@RequiredArgsConstructor
public class MappingProfileReader {

  public static final String BASE_PATH = "mappingProfile/bibframe2.0/";
  public static final String INSTANCE = "instance.json";
  public static final String HUB = "hub.json";
  public static final String WORK = "work.json";
  public static final String TITLE = "title/title.json";
  public static final String TITLE_ABBREVIATED = "title/title_abbreviated.json";
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
  public static final String ADMIN_METADATA = "admin_metadata/admin_metadata.json";
  private final JsonMapper jsonMapper = getJsonMapper();

  public MappingProfile getBibframe20Profile() {
    var mappingProfile = new MappingProfile();
    getInstanceMapping().ifPresent(mappingProfile::addTopResourceMappingsItem);
    getHubMapping().ifPresent(mappingProfile::addTopResourceMappingsItem);
    return mappingProfile;
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
        readResourceMapping(ADMIN_METADATA).ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        getWorkMapping().ifPresent(im.getResourceMapping()::addOutgoingEdgesItem);
        return im;
      });
  }

  private Optional<ResourceMapping> getWorkMapping() {
    return readResourceMapping(WORK)
      .map(wm -> {
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

  private Optional<ResourceMapping> getHubMapping() {
    return readResourceMapping(HUB)
      .map(hm -> {
        readResourceMapping(TITLE).ifPresent(hm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_ABBREVIATED).ifPresent(hm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_PARALLEL).ifPresent(hm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(TITLE_VARIANT).ifPresent(hm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(CONTRIBUTOR).ifPresent(hm.getResourceMapping()::addOutgoingEdgesItem);
        readResourceMapping(CREATOR).ifPresent(hm.getResourceMapping()::addOutgoingEdgesItem);
        return hm;
      });
  }

  private Optional<ResourceMapping> readResourceMapping(String fileName) {
    try {
      var resource = new ClassPathResource(BASE_PATH + fileName);
      return Optional.of(jsonMapper.readValue(resource.getInputStream(), ResourceMapping.class));
    } catch (IOException e) {
      log.error("Mapping profile reading issue for file: {}", fileName, e);
      return Optional.empty();
    }
  }
}
