package org.folio.rdf4ld.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackupBeanConfig {

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  }

  @Bean
  @ConditionalOnMissingBean(name = "lccnResourceProvider")
  public Function<String, Optional<Resource>> dummyResourceProvider() {
    return s -> Optional.empty();
  }

  @Bean
  @ConditionalOnMissingBean(name = "resourceUrlProvider")
  public Function<Long, String> dummyResourceUrlProvider() {
    return id -> "http://test-tobe-changed.folio.com/resources/" + id;
  }

}
