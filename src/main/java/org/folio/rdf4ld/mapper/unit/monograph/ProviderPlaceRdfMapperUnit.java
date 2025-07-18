package org.folio.rdf4ld.mapper.unit.monograph;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.ResourceUtil.addProperty;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertyString;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.PlaceDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = PLACE, predicate = PROVIDER_PLACE)
public class ProviderPlaceRdfMapperUnit implements RdfMapperUnit {

  private final ObjectMapper objectMapper;
  private final Supplier<String> baseUrlProvider;
  private final FingerprintHashService hashService;
  private final BaseRdfMapperUnit baseRdfMapperUnit;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    if (resource instanceof IRI iri) {
      return toProviderPlace(iri, model, resource, mapping, parent);
    }
    return empty();
  }

  private Optional<Resource> toProviderPlace(IRI iri,
                                             Model model,
                                             org.eclipse.rdf4j.model.Resource resource,
                                             ResourceMapping mapping,
                                             Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, resource, mapping, parent)
      .map(r -> {
          if (isNull(r.getDoc())) {
            r.setDoc(objectMapper.createObjectNode());
          }
          PlaceDictionary.getName(iri.getLocalName())
            .ifPresent(name -> r
              .setDoc(addProperty(r.getDoc(), NAME, name))
              .setDoc(addProperty(r.getDoc(), LABEL, name))
              .setLabel(name)
            );
          return r
            .setDoc(addProperty(r.getDoc(), CODE, iri.getLocalName()))
            .setDoc(addProperty(r.getDoc(), LINK, iri.stringValue()))
            .setId(hashService.hash(r));
        }
      );
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping mapping,
                            Resource parent) {
    var parentIri = iri(baseUrlProvider.get(), parent.getId().toString());
    var link = getPropertyString(resource.getDoc(), LINK);
    linkResources(parentIri, iri(link), mapping.getBfResourceDef().getPredicate(), modelBuilder);
  }

}
