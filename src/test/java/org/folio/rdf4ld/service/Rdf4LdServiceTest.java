package org.folio.rdf4ld.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Set;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.config.Rdf4LdObjectMapper;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.model.MappingProfile;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class Rdf4LdServiceTest {

  @Mock
  private Rdf4LdMapper rdf4LdMapper;
  @Mock
  private Rdf4LdObjectMapper objectMapper;
  @InjectMocks
  private Rdf4LdServiceImpl rdf4LdService;

  @Test
  void mapRdfToLd_returnsMappedResources_forValidInput() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/instance_titles.json");
    var contentType = "application/ld+json";
    var resources = Set.of(mock(Resource.class));
    var mappingProfile = mock(MappingProfile.class);
    when(rdf4LdMapper.mapRdfToLd(any(), eq(mappingProfile))).thenReturn(resources);

    // when
    var result = rdf4LdService.mapRdfToLd(inputStream, contentType, mappingProfile);

    // then
    assertThat(result).isEqualTo(resources);
  }

  @Test
  void mapRdfToLd_returnsEmptySet_forNullInput() {
    // given
    InputStream inputStream = null;
    var contentType = "application/ld+json";
    var mappingProfile = mock(MappingProfile.class);

    // when
    assertThatThrownBy(() -> rdf4LdService.mapRdfToLd(inputStream, contentType, mappingProfile))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Input stream is null");
  }

  @Test
  void mapRdfToLd_throwsException_forUnsupportedContentType() {
    // given
    var inputStream = mock(InputStream.class);
    var contentType = "unsupported/type";
    var mappingProfile = mock(MappingProfile.class);

    // when
    assertThatThrownBy(() -> rdf4LdService.mapRdfToLd(inputStream, contentType, mappingProfile))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unsupported RDF format");
  }

  @Test
  void mapRdfToLd_returnsEmptySet_whenExceptionOccursDuringParsing() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/invalid.json");
    var contentType = "application/ld+json";
    var mappingProfile = mock(MappingProfile.class);

    // when
    assertThatThrownBy(() -> rdf4LdService.mapRdfToLd(inputStream, contentType, mappingProfile))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RDF parsing error");
  }

  @Test
  void mapBibframe2RdfToLd_returnsMappedResources_forValidInput() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/instance_titles.json");
    var contentType = "application/ld+json";
    var resources = Set.of(mock(Resource.class));
    when(rdf4LdMapper.mapBibframe2RdfToLd(any())).thenReturn(resources);

    // when
    var result = rdf4LdService.mapBibframe2RdfToLd(inputStream, contentType);

    // then
    assertThat(result).isEqualTo(resources);
  }

  @Test
  void mapBibframe2RdfToLd_returnsEmptySet_forNullInput() {
    // given
    InputStream inputStream = null;
    var contentType = "application/ld+json";

    // when
    assertThatThrownBy(() -> rdf4LdService.mapBibframe2RdfToLd(inputStream, contentType))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Input stream is null");
  }

  @Test
  void mapBibframe2RdfToLd_throwsException_forUnsupportedContentType() {
    // given
    var inputStream = mock(InputStream.class);
    var contentType = "unsupported/type";

    // when
    assertThatThrownBy(() -> rdf4LdService.mapBibframe2RdfToLd(inputStream, contentType))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unsupported RDF format");
  }

  @Test
  void mapBibframe2RdfToLd_returnsEmptySet_whenExceptionOccursDuringParsing() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/invalid.json");
    var contentType = "application/ld+json";

    // when
    assertThatThrownBy(() -> rdf4LdService.mapBibframe2RdfToLd(inputStream, contentType))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RDF parsing error");
  }

  @Test
  void mapLdToRdf_returnsSerializedModel_forValidInput() {
    // given
    var resource = new Resource().setTypes(Set.of(ResourceTypeDictionary.INSTANCE));
    var mappingProfile = mock(MappingProfile.class);
    var rdfFormat = RDFFormat.JSONLD;
    var model = new ModelBuilder().build();
    when(rdf4LdMapper.mapLdToRdf(resource, mappingProfile)).thenReturn(model);

    // when
    var result = rdf4LdService.mapLdToRdf(resource, rdfFormat, mappingProfile);

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThan(0);
  }

  @Test
  void mapLdToBibframe2Rdf_returnsSerializedModel_forValidInput() {
    // given
    var resource = new Resource().setTypes(Set.of(ResourceTypeDictionary.INSTANCE));
    var rdfFormat = RDFFormat.JSONLD;
    var model = new ModelBuilder().build();
    when(rdf4LdMapper.mapLdToBibframe2Rdf(resource)).thenReturn(model);

    // when
    var result = rdf4LdService.mapLdToBibframe2Rdf(resource, rdfFormat);

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThan(0);
  }

}
