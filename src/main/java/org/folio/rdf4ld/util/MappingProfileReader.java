package org.folio.rdf4ld.util;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.function.IOFunction;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.rdf4ld.model.MappingProfile;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MappingProfileReader {

  public static final String BASE_PATH = "mappingProfile/";
  public static final String INSTANCE_BIBFRAME_2_0_JSON = "Instance_Bibframe_2.0.json";
  public static final String ROLE_CODES_BIBFRAME_2_0_JSON = "Roles_Bibframe_2.0.json";
  private final ObjectMapper objectMapper;

  public MappingProfile getInstanceBibframe20Profile() {
    var resourceMapping = getInstanceResourceMappingBibframe20();
    var roleMapping = getRoleMappingBibframe20();
    if (isNull(resourceMapping) && isNull(roleMapping)) {
      return null;
    }
    return new MappingProfile()
      .resourceMapping(resourceMapping)
      .roleMapping(roleMapping);
  }

  private ResourceMapping getInstanceResourceMappingBibframe20() {
    return readFile(INSTANCE_BIBFRAME_2_0_JSON, r -> objectMapper.readValue(r.getInputStream(), ResourceMapping.class));
  }

  private Map<String, PredicateDictionary> getRoleMappingBibframe20() {
    var type = new TypeReference<Map<String, PredicateDictionary>>() {};
    return readFile(ROLE_CODES_BIBFRAME_2_0_JSON, r -> objectMapper.readValue(r.getInputStream(), type));
  }

  private <T> T readFile(String fileName, IOFunction<ClassPathResource, T> ioFunction) {
    try {
      var resource = new ClassPathResource(BASE_PATH + fileName);
      return ioFunction.apply(resource);
    } catch (IOException e) {
      log.error("Mapping profile reading issue for file: {}", fileName, e);
      return null;
    }
  }
}
