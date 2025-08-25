package org.folio.rdf4ld.mapper.unit.monograph.title;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.rdf4ld.util.RdfUtil.getAllTypes;
import static org.folio.rdf4ld.util.ResourceUtil.addProperty;

import com.google.common.collect.ImmutableBiMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = VARIANT_TITLE, predicate = TITLE)
public class VariantTitleRdfMapperUnit implements RdfMapperUnit {

  private static final ImmutableBiMap<String, String> TYPES_BI_MAP = new ImmutableBiMap.Builder<String, String>()
    .put("http://id.loc.gov/vocabulary/vartitletype/por", "0")
    .put("http://id.loc.gov/vocabulary/vartitletype/dis", "2")
    .put("http://id.loc.gov/vocabulary/vartitletype/cov", "4")
    .put("http://id.loc.gov/vocabulary/vartitletype/atp", "5")
    .put("http://id.loc.gov/vocabulary/vartitletype/cap", "6")
    .put("http://id.loc.gov/vocabulary/vartitletype/run", "7")
    .put("http://id.loc.gov/vocabulary/vartitletype/spi", "8")
    .build();
  private final FingerprintHashService hashService;
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final Function<Long, String> resourceUrlProvider;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping resourceMapping,
                                    Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, parent)
      .map(variantTitle -> {
        mapVariantTypeToProperty(model, variantTitle, resource);
        variantTitle.setId(hashService.hash(variantTitle));
        return variantTitle;
      });
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping mapping,
                            Resource parent) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, mapping, parent);
    addVariantTypes(resource, modelBuilder);
    removeVariantTypeProperty(modelBuilder, mapping, resource.getId());
  }

  private void mapVariantTypeToProperty(Model model, Resource variantTitle,
                                        org.eclipse.rdf4j.model.Resource rdfResource) {
    getAllTypes(model, rdfResource)
      .stream()
      .filter(TYPES_BI_MAP::containsKey)
      .map(TYPES_BI_MAP::get)
      .forEach(type -> addProperty(variantTitle.getDoc(), VARIANT_TYPE, type));
    variantTitle.setId(hashService.hash(variantTitle));
  }

  private void addVariantTypes(Resource resource, ModelBuilder modelBuilder) {
    if (resource.getDoc().has(VARIANT_TYPE.getValue())) {
      var resourceIri = iri(resourceUrlProvider.apply(resource.getId()));
      resource.getDoc().get(VARIANT_TYPE.getValue()).iterator().forEachRemaining(
        textNode -> {
          if (TYPES_BI_MAP.inverse().containsKey(textNode.asText())) {
            modelBuilder.add(resourceIri, RDF.TYPE, iri(TYPES_BI_MAP.inverse().get(textNode.asText())));
          }
        }
      );
    }
  }

  private void removeVariantTypeProperty(ModelBuilder modelBuilder, ResourceMapping mapping, Long id) {
    var property = ((LinkedHashSet<PropertyMapping>) mapping.getResourceMapping().getProperties()).getLast()
      .getBfProperty();
    modelBuilder.build().remove(iri(resourceUrlProvider.apply(id)), iri(property), null);
  }

}
