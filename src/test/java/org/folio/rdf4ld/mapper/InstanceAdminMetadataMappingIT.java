package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;

import java.io.IOException;
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
class InstanceAdminMetadataMappingIT {

  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithTitles() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance_admin_metadata.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isNotEmpty().hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getDoc()).isNull();
    assertThat(instance.getOutgoingEdges().stream().anyMatch(edge ->
      edge.getPredicate().equals(ADMIN_METADATA)
    )).isFalse();
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithAdminMetadata() throws IOException {
    // given
    var instance = MonographUtil.createInstance(null, null);
    var folioUuid = UUID.randomUUID().toString();
    var adminMetadata = MonographUtil.createAdminMetadata("a", folioUuid);
    instance.addOutgoingEdge(new ResourceEdge(instance, adminMetadata, ADMIN_METADATA));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/instance_admin_metadata.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("ADMIN_METADATA_ID", adminMetadata.getId().toString())
      .replaceAll("FOLIO_UUID", folioUuid);

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }
}
