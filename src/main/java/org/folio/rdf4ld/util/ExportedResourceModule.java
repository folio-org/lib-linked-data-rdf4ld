package org.folio.rdf4ld.util;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.ld.dictionary.model.Resource;

public class ExportedResourceModule extends SimpleModule {
  public ExportedResourceModule() {
    super("ExportedResourceModule");
    addDeserializer(Resource.class, new ExportedResourceDeserializer());
  }
}
