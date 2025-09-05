package org.folio.rdf4ld.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.util.ExportedResourceDeserializer;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperConfig implements BeanPostProcessor {
  
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof ObjectMapper mapper) {
      SimpleModule module = new SimpleModule();
      module.addDeserializer(Resource.class, new ExportedResourceDeserializer());
      mapper.registerModule(module);
    }
    return bean;
  }
}
