package org.folio.rdf4ld.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DefaultMappingProfileReader {

  public static final String BASE_PATH = "src/main/resources/mappingProfile/";
  public static final String INSTANCE_BIBFRAME_2_0_JSON = "Instance_Bibframe_2.0.json";
  private final ObjectMapper objectMapper;

  public ResourceMapping getInstanceBibframe20Profile() {
    try {
      return objectMapper.readValue(new File(BASE_PATH + INSTANCE_BIBFRAME_2_0_JSON), ResourceMapping.class);
    } catch (IOException e) {
      log.error("Default mapping profile reading issue", e);
      return null;
    }
  }
}
