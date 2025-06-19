package org.folio.rdf4ld.mapper.unit.monograph.title;

import static java.util.Collections.emptyList;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.rdf4ld.util.ResourceUtil.addProperty;

import com.fasterxml.jackson.databind.node.TextNode;
import java.util.LinkedHashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.IteratorUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
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

  private static final Map<String, String> TYPES_MAP = Map.of(
    "http://id.loc.gov/vocabulary/vartitletype/por", "0",
    "http://id.loc.gov/vocabulary/vartitletype/dis", "2",
    "http://id.loc.gov/vocabulary/vartitletype/cov", "4",
    "http://id.loc.gov/vocabulary/vartitletype/atp", "5",
    "http://id.loc.gov/vocabulary/vartitletype/cap", "6",
    "http://id.loc.gov/vocabulary/vartitletype/run", "7",
    "http://id.loc.gov/vocabulary/vartitletype/spi", "8"
  );
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceMapping resourceMapping,
                          Resource parent) {
    var variantTitle = baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, parent);
    mapVariantTypeToProperty(model, resourceMapping, variantTitle, resource);
    variantTitle.setId(hashService.hash(variantTitle));
    return variantTitle;
  }

  @Override
  public void mapToBibframe(Resource resource, ModelBuilder modelBuilder, ResourceMapping mapping) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, mapping);
    removeRedundantVariantTypes(resource, modelBuilder, mapping);
  }

  private void mapVariantTypeToProperty(Model model, ResourceMapping mapping, Resource variantTitle,
                                        org.eclipse.rdf4j.model.Resource rdfResource) {
    mapping.getBfResourceDef().getTypeSet().stream()
      .filter(TYPES_MAP::containsKey)
      .filter(type -> model.contains(rdfResource, RDF.TYPE, Values.iri(type)))
      .map(TYPES_MAP::get)
      .forEach(type -> addProperty(variantTitle.getDoc(), VARIANT_TYPE, type));
    variantTitle.setId(hashService.hash(variantTitle));
  }

  private void removeRedundantVariantTypes(Resource resource, ModelBuilder modelBuilder, ResourceMapping mapping) {
    var resourceIri = coreLd2RdfMapper.getResourceIri(mapping.getBfResourceDef().getNameSpace(),
      resource.getId().toString());
    var values = resource.getDoc().has(VARIANT_TYPE.getValue())
      ? IteratorUtils.toList(resource.getDoc().get(VARIANT_TYPE.getValue()).iterator())
      : emptyList();
    TYPES_MAP.forEach((key, value) -> {
      if (!values.contains(TextNode.valueOf(value))) {
        modelBuilder.build().remove(resourceIri, RDF.TYPE, Values.iri(key));
      }
    });
    modelBuilder.build().remove(resourceIri, Values.iri(getVariantTypePropertyName(mapping)), null);
  }

  private String getVariantTypePropertyName(ResourceMapping mapping) {
    return ((LinkedHashSet<PropertyMapping>) mapping.getResourceMapping().getProperties()).getLast().getBfProperty();
  }

}
