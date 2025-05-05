package org.folio.rdf4ld.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rdf4ld.model.MappingProfile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DefaultMappingProfileReader {

  public static final String PATH = "src/main/resources/mappingProfile/default.json";
  private final ObjectMapper objectMapper;

  public MappingProfile get() {
    try {
      return objectMapper.readValue(new File(PATH), MappingProfile.class);
    } catch (IOException e) {
      log.error("Default mapping profile reading issue", e);
      return null;
    }
  }
}
