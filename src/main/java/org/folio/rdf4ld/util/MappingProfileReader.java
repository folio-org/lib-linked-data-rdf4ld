package org.folio.rdf4ld.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MappingProfileReader {

  public static final String BASE_PATH = "mappingProfile/";
  public static final String INSTANCE_BIBFRAME_2_0_JSON = "Instance_Bibframe_2.0.json";
  private final ObjectMapper objectMapper;

  public ResourceMapping getInstanceBibframe20Profile() {
    try {
      var resource = new ClassPathResource(BASE_PATH + INSTANCE_BIBFRAME_2_0_JSON);
      return objectMapper.readValue(resource.getInputStream(), ResourceMapping.class);
    } catch (IOException e) {
      log.error("Default mapping profile reading issue", e);
      return null;
    }
  }
}
