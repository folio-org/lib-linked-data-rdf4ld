package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.ResourceTypeDictionary;
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
  void mapToLd_InstanceTitles_shouldReturnMappedInstanceWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_titles.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapToLdInstance(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getLabel()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(3);
    validateOutgoingEdge(instance, TITLE, Set.of(ResourceTypeDictionary.TITLE),
      Map.of(
        MAIN_TITLE, List.of("Title mainTitle 1", "Title mainTitle 2"),
        PART_NAME, List.of("Title partName 1", "Title partName 2"),
        PART_NUMBER, List.of("Title partNumber 1", "Title partNumber 2"),
        SUBTITLE, List.of("Title subTitle 1", "Title subTitle 2"),
        NON_SORT_NUM, List.of("Title nonSortNum 1", "Title nonSortNum 2")
      ), "Title mainTitle 1, Title mainTitle 2, Title subTitle 1, Title subTitle 2"
    );
    validateOutgoingEdge(instance, TITLE, Set.of(PARALLEL_TITLE),
      Map.of(
        MAIN_TITLE, List.of("ParallelTitle mainTitle 1", "ParallelTitle mainTitle 2"),
        PART_NAME, List.of("ParallelTitle partName 1", "ParallelTitle partName 2"),
        PART_NUMBER, List.of("ParallelTitle partNumber 1", "ParallelTitle partNumber 2"),
        SUBTITLE, List.of("ParallelTitle subTitle 1", "ParallelTitle subTitle 2"),
        DATE, List.of("ParallelTitle date 1", "ParallelTitle date 2"),
        NOTE, List.of("ParallelTitle note 1", "ParallelTitle note 2")
      ), "ParallelTitle mainTitle 1, ParallelTitle mainTitle 2, ParallelTitle subTitle 1, ParallelTitle subTitle 2"
    );
    validateOutgoingEdge(instance, TITLE, Set.of(VARIANT_TITLE),
      Map.of(
        MAIN_TITLE, List.of("VariantTitle mainTitle 1", "VariantTitle mainTitle 2"),
        PART_NAME, List.of("VariantTitle partName 1", "VariantTitle partName 2"),
        PART_NUMBER, List.of("VariantTitle partNumber 1", "VariantTitle partNumber 2"),
        SUBTITLE, List.of("VariantTitle subTitle 1", "VariantTitle subTitle 2"),
        DATE, List.of("VariantTitle date 1", "VariantTitle date 2"),
        NOTE, List.of("VariantTitle note 1", "VariantTitle note 2"),
        VARIANT_TYPE, List.of("VariantTitle variantType 1", "VariantTitle variantType 2")
      ), "VariantTitle mainTitle 1, VariantTitle mainTitle 2, VariantTitle subTitle 1, VariantTitle subTitle 2"
    );
  }

}
