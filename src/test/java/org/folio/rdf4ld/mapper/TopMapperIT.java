package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.getSampleInstanceResource;
import static org.folio.rdf4ld.test.TestUtil.validateIncomingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateProperty;

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
public class TopMapperIT {

  @Autowired
  private TopMapper topMapper;

  @Test
  public void mapToLd_shouldReturnMappedInstance() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/loc_example.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = topMapper.mapToLd(model);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getDoc()).isNotNull();
    validateProperty(instance.getDoc(), DIMENSIONS.getValue(), List.of("21 cm"));
    validateProperty(instance.getDoc(), STATEMENT_OF_RESPONSIBILITY.getValue(),
      List.of("Bratʹi︠a︡ Shvalʹnery", "Братья Швальнеры")
    );
    assertThat(instance.getLabel()).isEqualTo("Gogolʹ, Viĭ, Гоголь, Вий");
    assertThat(instance.getIncomingEdges()).hasSize(1);
    var workLabel = "Bratʹi︠a︡ Shvalʹnery Gogolʹ, Viĭ";
    validateIncomingEdge(instance, INSTANTIATES, Set.of(WORK), Map.of(LABEL, List.of(workLabel)), workLabel);
    assertThat(instance.getOutgoingEdges()).hasSize(3);
    validateOutgoingEdge(instance, TITLE, Set.of(ResourceTypeDictionary.TITLE),
      Map.of(MAIN_TITLE, List.of("Gogolʹ, Viĭ", "Гоголь, Вий")), "Gogolʹ, Viĭ, Гоголь, Вий"
    );
    validateOutgoingEdge(instance, TITLE, Set.of(ResourceTypeDictionary.TITLE),
      Map.of(
        MAIN_TITLE, List.of("Gogolʹ, Viĭ", "Гоголь, Вий"),
        SUBTITLE, List.of("ne vykhodi iz kruga", "не выходи из круга")
      ), "Gogolʹ, Viĭ, Гоголь, Вий, ne vykhodi iz kruga, не выходи из круга"
    );
    validateOutgoingEdge(instance, TITLE, Set.of(VARIANT_TITLE),
      Map.of(MAIN_TITLE, List.of("Вий")), "Вий"
    );
  }

  @Test
  public void mapToBibframeRdf_shouldReturnMappedRdf() throws IOException {
    // given
    var instance = getSampleInstanceResource();

    // when
    var model = topMapper.mapToBibframeRdf(instance);

    //then
    assertThat(model).hasSize(15);
  }
}
