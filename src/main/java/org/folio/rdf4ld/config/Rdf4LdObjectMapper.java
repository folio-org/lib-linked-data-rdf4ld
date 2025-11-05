package org.folio.rdf4ld.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.util.ResourceViewDeserializer;
import org.springframework.stereotype.Component;

@Component
public class Rdf4LdObjectMapper extends ObjectMapper {

  public Rdf4LdObjectMapper() {
    var module = new SimpleModule();
    module.addDeserializer(Resource.class, new ResourceViewDeserializer());
    super.registerModule(module)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  }

}
