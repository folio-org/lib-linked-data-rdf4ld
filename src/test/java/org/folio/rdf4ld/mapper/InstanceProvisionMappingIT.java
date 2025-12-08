package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.rdf4ld.test.MonographUtil.createProvision;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Resource;
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
class InstanceProvisionMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithProvisions() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_provisions.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getOutgoingEdges()).hasSize(4);
    validateProvision(instance, PE_DISTRIBUTION);
    validateProvision(instance, PE_MANUFACTURE);
    validateProvision(instance, PE_PRODUCTION);
    validateProvision(instance, PE_PUBLICATION);
  }

  private static void validateProvision(Resource instance, PredicateDictionary predicate) {
    var prefix = predicate.getUri().substring(predicate.getUri().lastIndexOf("/") + 1);
    validateOutgoingEdge(instance, predicate, Set.of(PROVIDER_EVENT),
      Map.of(
        PROVIDER_DATE, List.of(prefix + " provider date 1", prefix + " provider date 2"),
        DATE, List.of(prefix + " simple date 1", prefix + " simple date 2"),
        NAME, List.of(prefix + " simple agent 1", prefix + " simple agent 2"),
        SIMPLE_PLACE, List.of(prefix + " simple place 1", prefix + " simple place 2")
      ), prefix + " simple agent 1, " + prefix + " simple agent 2",
      publication -> {
        validateOutgoingEdge(publication, PROVIDER_PLACE, Set.of(PLACE),
          Map.of(
            LINK, List.of("http://id.loc.gov/vocabulary/countries/kz"),
            CODE, List.of("kz"),
            NAME, List.of("Kazakhstan"),
            LABEL, List.of("Kazakhstan")
          ), "Kazakhstan");
        validateOutgoingEdge(publication, PROVIDER_PLACE, Set.of(PLACE),
          Map.of(
            LINK, List.of("http://id.loc.gov/vocabulary/countries/ru"),
            CODE, List.of("ru"),
            NAME, List.of("Russia (Federation)"),
            LABEL, List.of("Russia (Federation)")
          ), "Russia (Federation)");
      }
    );
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithProvisions() throws IOException {
    // given
    var instance = MonographUtil.createInstance(null);
    var distribution = createProvision("distribution");
    var manufacture = createProvision("manufacture");
    var production = createProvision("production");
    var publication = createProvision("publication");
    instance.addOutgoingEdge(new ResourceEdge(instance, distribution, PE_DISTRIBUTION));
    instance.addOutgoingEdge(new ResourceEdge(instance, manufacture, PE_MANUFACTURE));
    instance.addOutgoingEdge(new ResourceEdge(instance, production, PE_PRODUCTION));
    instance.addOutgoingEdge(new ResourceEdge(instance, publication, PE_PUBLICATION));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/instance_provisions.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("DISTRIBUTION_ID", distribution.getId().toString())
      .replaceAll("MANUFACTURE_ID", manufacture.getId().toString())
      .replaceAll("PRODUCTION_ID", production.getId().toString())
      .replaceAll("PUBLICATION_ID", publication.getId().toString());
    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

}
