package org.folio.rdf4ld.mapper.unit.monograph.title;

import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;

@RequiredArgsConstructor
public class VariantTitleRdfMapperUnit implements RdfMapperUnit {

  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final FingerprintHashService hashService;


  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceMapping resourceMapping,
                          Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, parent);
  }

  @Override
  public void mapToBibframe(Resource resource, ModelBuilder modelBuilder, ResourceMapping resourceMapping) {

  }
}
