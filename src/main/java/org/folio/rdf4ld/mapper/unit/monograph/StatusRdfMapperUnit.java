package org.folio.rdf4ld.mapper.unit.monograph;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertyString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = STATUS, predicate = PredicateDictionary.STATUS)
public class StatusRdfMapperUnit implements RdfMapperUnit {

  private final Supplier<String> baseUrlProvider;
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final FingerprintHashService hashService;
  private final BaseRdfMapperUnit baseRdfMapperUnit;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping resourceMapping,
                                    Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, parent)
      .map(status -> {
        var label = ((SimpleIRI) resource).getLocalName();
        status.setDoc(coreRdf2LdMapper.toJson(Map.of(
          LABEL.getValue(), List.of(label),
          LINK.getValue(), List.of(resource.stringValue())
        )));
        status.setLabel(label);
        status.setId(hashService.hash(status));
        return status;
      });
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping mapping,
                            Resource parent) {
    var statusLink = getPropertyString(resource.getDoc(), LINK);
    linkResources(iri(baseUrlProvider.get(), String.valueOf(parent.getId())),
      iri(statusLink), mapping.getBfResourceDef().getPredicate(), modelBuilder);

  }

}
