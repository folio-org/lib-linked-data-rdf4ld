package org.folio.rdf4ld.util;

import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.util.ResourceViewDeserializer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@UtilityClass
public class JsonUtil {

  public static JsonMapper getJsonMapper() {
    return JsonMapper.builder()
      .addModule(new SimpleModule().addDeserializer(Resource.class, new ResourceViewDeserializer()))
      .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();
  }
}
