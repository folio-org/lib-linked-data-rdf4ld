package org.folio.rdf4ld.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.util.ExportedResourceDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackupBeanConfig {

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Resource.class, new ExportedResourceDeserializer());
    return new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .registerModule(module);
  }

  @Bean
  @ConditionalOnMissingBean(name = "lccnResourceProvider")
  public Function<String, Optional<Resource>> dummyResourceProvider() {
    return s -> Optional.empty();
  }

  @Bean
  @ConditionalOnMissingBean(name = "resourceUrlProvider")
  public LongFunction<String> dummyResourceUrlProvider() {
    return id -> "http://test-tobe-changed.folio.com/resources/" + id;
  }

}
