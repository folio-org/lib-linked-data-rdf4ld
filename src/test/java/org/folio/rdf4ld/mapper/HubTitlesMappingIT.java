package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.rdf4ld.test.MonographUtil.createAbbreviatedTitle;
import static org.folio.rdf4ld.test.MonographUtil.createParallelTitle;
import static org.folio.rdf4ld.test.MonographUtil.createPrimaryTitle;
import static org.folio.rdf4ld.test.MonographUtil.createVariantTitle;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithTitles;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.ResourceTypeDictionary;
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
class HubTitlesMappingIT {

  private static final String HUB_AAP = "Hub AAP";
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedHubWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/hub_titles.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var hub = result.iterator().next();
    assertThat(hub.getDoc()).isNotNull();
    validateProperty(hub.getDoc(), LABEL.getValue(), List.of(HUB_AAP));
    assertThat(hub.getLabel()).isEqualTo(HUB_AAP);
    validateResourceWithTitles(hub, "", "http://test-tobe-changed.folio.com/resources/HUB_ID");
    validateOutgoingEdge(hub, TITLE, Set.of(ResourceTypeDictionary.ABBREVIATED_TITLE),
      Map.of(
        MAIN_TITLE, List.of("AbbreviatedTitle mainTitle 1", "AbbreviatedTitle mainTitle 2"),
        PART_NAME, List.of("AbbreviatedTitle partName 1", "AbbreviatedTitle partName 2"),
        PART_NUMBER, List.of("AbbreviatedTitle partNumber 1", "AbbreviatedTitle partNumber 2")
      ), "AbbreviatedTitle mainTitle 1, AbbreviatedTitle mainTitle 2"
    );
    assertThat(hub.getOutgoingEdges()).hasSize(4);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfHubWithTitles() throws IOException {
    // given
    var primaryTitle = createPrimaryTitle("");
    var parallelTitle = createParallelTitle("");
    var variantTitle = createVariantTitle("");
    var abbreviatedTitle = createAbbreviatedTitle("");
    var properties = Map.of(
      LABEL, List.of(HUB_AAP),
      LINK, List.of(UUID.randomUUID().toString())
    );
    var hub = MonographUtil.createHub(properties);
    hub.addOutgoingEdge(new ResourceEdge(hub, primaryTitle, TITLE));
    hub.addOutgoingEdge(new ResourceEdge(hub, parallelTitle, TITLE));
    hub.addOutgoingEdge(new ResourceEdge(hub, variantTitle, TITLE));
    hub.addOutgoingEdge(new ResourceEdge(hub, abbreviatedTitle, TITLE));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/hub_titles.json").readAllBytes())
      .replaceAll("HUB_ID", hub.getId().toString())
      .replaceAll("PRIMARY_TITLE_ID", primaryTitle.getId().toString())
      .replaceAll("PARALLEL_TITLE_ID", parallelTitle.getId().toString())
      .replaceAll("VARIANT_TITLE_ID", variantTitle.getId().toString())
      .replaceAll("PARALLEL_TITLE_NOTE_ID_1", "NOTE_1_" + parallelTitle.getId().toString())
      .replaceAll("PARALLEL_TITLE_NOTE_ID_2", "NOTE_2_" + parallelTitle.getId().toString())
      .replaceAll("VARIANT_TITLE_NOTE_ID_1", "NOTE_1_" + variantTitle.getId().toString())
      .replaceAll("VARIANT_TITLE_NOTE_ID_2", "NOTE_2_" + variantTitle.getId().toString())
      .replaceAll("ABBREVIATED_TITLE_ID", abbreviatedTitle.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(hub);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

}
