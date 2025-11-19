package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createParallelTitle;
import static org.folio.rdf4ld.test.MonographUtil.createPrimaryTitle;
import static org.folio.rdf4ld.test.MonographUtil.createVariantTitle;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.getTitleLabel;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkTitlesMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_titles.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    validateResourceWithTitles(instance, "", "http://test-tobe-changed.folio.com/resources/INSTANCE_ID");
    assertThat(instance.getOutgoingEdges()).hasSize(4);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, CONTINUING_RESOURCES),
      Map.of(), getTitleLabel("Work ", "Title"), r -> validateResourceWithTitles(r, "Work ",
        "http://test-tobe-changed.folio.com/resources/WORK_ID")
    );
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithTitles() throws IOException {
    // given
    var primaryTitle = createPrimaryTitle("");
    var parallelTitle = createParallelTitle("");
    var variantTitle = createVariantTitle("");
    var instance = createInstance(primaryTitle.getLabel(), null);
    instance.addOutgoingEdge(new ResourceEdge(instance, primaryTitle, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, parallelTitle, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, variantTitle, TITLE));
    var primaryWorkTitle = createPrimaryTitle("Work ");
    var parallelWorkTitle = createParallelTitle("Work ");
    var variantWorkTitle = createVariantTitle("Work ");
    var work = createWork(primaryWorkTitle.getLabel(), CONTINUING_RESOURCES);
    work.addOutgoingEdge(new ResourceEdge(work, primaryWorkTitle, TITLE));
    work.addOutgoingEdge(new ResourceEdge(work, parallelWorkTitle, TITLE));
    work.addOutgoingEdge(new ResourceEdge(work, variantWorkTitle, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_titles.json").readAllBytes())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("WORK_PRIMARY_TITLE_ID", primaryWorkTitle.getId().toString())
      .replaceAll("WORK_PARALLEL_TITLE_ID", parallelWorkTitle.getId().toString())
      .replaceAll("WORK_VARIANT_TITLE_ID", variantWorkTitle.getId().toString())
      .replaceAll("WORK_PARALLEL_TITLE_NOTE_ID_1", "NOTE_1_" + parallelWorkTitle.getId().toString())
      .replaceAll("WORK_PARALLEL_TITLE_NOTE_ID_2", "NOTE_2_" + parallelWorkTitle.getId().toString())
      .replaceAll("WORK_VARIANT_TITLE_NOTE_ID_1", "NOTE_1_" + variantWorkTitle.getId().toString())
      .replaceAll("WORK_VARIANT_TITLE_NOTE_ID_2", "NOTE_2_" + variantWorkTitle.getId().toString())
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
