package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.rdf4ld.test.MonographUtil.createParallelTitle;
import static org.folio.rdf4ld.test.MonographUtil.createPrimaryTitle;
import static org.folio.rdf4ld.test.MonographUtil.createVariantTitle;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.test.MonographUtil;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class InstanceTitlesMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_titles.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getDoc()).isNotNull();
    validateProperty(instance.getDoc(), DIMENSIONS.getValue(),
      List.of("Instance dimensions 1", "Instance dimensions 2"));
    validateProperty(instance.getDoc(), STATEMENT_OF_RESPONSIBILITY.getValue(),
      List.of("Instance responsibilityStatement 1", "Instance responsibilityStatement 2")
    );
    validateResourceWithTitles(instance, "", "http://test-tobe-changed.folio.com/resources/INSTANCE_ID");
    assertThat(instance.getOutgoingEdges()).hasSize(3);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithTitles() throws IOException {
    // given
    var primaryTitle = createPrimaryTitle("");
    var parallelTitle = createParallelTitle("");
    var variantTitle = createVariantTitle("");
    var properties = Map.of(
      DIMENSIONS, List.of("Instance dimensions 1", "Instance dimensions 2"),
      STATEMENT_OF_RESPONSIBILITY, List.of("Instance responsibilityStatement 1", "Instance responsibilityStatement 2"),
      LINK, List.of(UUID.randomUUID().toString())
    );
    var instance = MonographUtil.createInstance(primaryTitle.getLabel(), properties);
    instance.addOutgoingEdge(new ResourceEdge(instance, primaryTitle, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, parallelTitle, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, variantTitle, TITLE));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/instance_titles.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("PRIMARY_TITLE_ID", primaryTitle.getId().toString())
      .replaceAll("PARALLEL_TITLE_ID", parallelTitle.getId().toString())
      .replaceAll("VARIANT_TITLE_ID", variantTitle.getId().toString())
      .replaceAll("PARALLEL_TITLE_NOTE_ID_1", "NOTE_1_" + parallelTitle.getId().toString())
      .replaceAll("PARALLEL_TITLE_NOTE_ID_2", "NOTE_2_" + parallelTitle.getId().toString())
      .replaceAll("VARIANT_TITLE_NOTE_ID_1", "NOTE_1_" + variantTitle.getId().toString())
      .replaceAll("VARIANT_TITLE_NOTE_ID_2", "NOTE_2_" + variantTitle.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

}
