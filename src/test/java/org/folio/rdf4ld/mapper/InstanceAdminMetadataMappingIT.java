package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;

import java.io.IOException;
import java.util.UUID;
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
