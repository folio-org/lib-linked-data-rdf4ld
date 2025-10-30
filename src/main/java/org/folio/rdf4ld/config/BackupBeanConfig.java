package org.folio.rdf4ld.config;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
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
    log.warn("No lccnResourceProvider bean has been found, using the dummy one");
    return s -> empty();
  }

  @Bean
  @ConditionalOnMissingBean(name = "resourceUrlProvider")
  public LongFunction<String> dummyResourceUrlProvider() {
    log.warn("No resourceUrlProvider bean has been found, using the dummy one");
    return id -> "http://test-tobe-changed.folio.com/resources/" + id;
  }

  @Bean
  @ConditionalOnMissingBean(name = "defaultWorkTypeProvider")
  public Supplier<Optional<ResourceTypeDictionary>> dummyDefaultWorkTypeProvider() {
    log.warn("No defaultWorkTypeProvider bean has been found, using the dummy one");
    return () -> of(BOOKS);
  }

}
