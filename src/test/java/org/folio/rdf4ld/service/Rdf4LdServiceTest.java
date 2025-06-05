package org.folio.rdf4ld.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
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
  @InjectMocks
  private Rdf4LdServiceImpl rdf4LdService;

  @Test
  void mapToLd_returnsMappedResources_forValidInput() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/instance.json");
    var contentType = "application/ld+json";
    var resources = Set.of(mock(Resource.class));
    var mapping = mock(MappingProfile.class);
    when(rdf4LdMapper.mapToLd(any(), eq(mapping))).thenReturn(resources);

    // when
    var result = rdf4LdService.mapToLd(inputStream, contentType, mapping);

    // then
    assertThat(result).isEqualTo(resources);
  }

  @Test
  void mapToLd_returnsEmptySet_forNullInput() {
    // given
    InputStream inputStream = null;
    var contentType = "application/ld+json";
    var mapping = mock(MappingProfile.class);

    // when
    assertThatThrownBy(() -> rdf4LdService.mapToLd(inputStream, contentType, mapping))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Input stream is null");
  }

  @Test
  void mapToLd_throwsException_forUnsupportedContentType() {
    // given
    var inputStream = mock(InputStream.class);
    var contentType = "unsupported/type";
    var mapping = mock(MappingProfile.class);

    // when
    assertThatThrownBy(() -> rdf4LdService.mapToLd(inputStream, contentType, mapping))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unsupported RDF format");
  }

  @Test
  void mapToLd_returnsEmptySet_whenExceptionOccursDuringParsing() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/invalid.json");
    var contentType = "application/ld+json";
    var mapping = mock(MappingProfile.class);

    // when
    assertThatThrownBy(() -> rdf4LdService.mapToLd(inputStream, contentType, mapping))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RDF parsing error");
  }

  @Test
  void mapToLdInstance_returnsMappedResources_forValidInput() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/instance.json");
    var contentType = "application/ld+json";
    var resources = Set.of(mock(Resource.class));
    when(rdf4LdMapper.mapToLdInstance(any())).thenReturn(resources);

    // when
    var result = rdf4LdService.mapToLdInstance(inputStream, contentType);

    // then
    assertThat(result).isEqualTo(resources);
  }

  @Test
  void mapToLdInstance_returnsEmptySet_forNullInput() {
    // given
    InputStream inputStream = null;
    var contentType = "application/ld+json";

    // when
    assertThatThrownBy(() -> rdf4LdService.mapToLdInstance(inputStream, contentType))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Input stream is null");
  }

  @Test
  void mapToLdInstance_throwsException_forUnsupportedContentType() {
    // given
    var inputStream = mock(InputStream.class);
    var contentType = "unsupported/type";

    // when
    assertThatThrownBy(() -> rdf4LdService.mapToLdInstance(inputStream, contentType))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unsupported RDF format");
  }

  @Test
  void mapToLdInstance_returnsEmptySet_whenExceptionOccursDuringParsing() {
    // given
    var inputStream = this.getClass().getResourceAsStream("/rdf/invalid.json");
    var contentType = "application/ld+json";

    // when
    assertThatThrownBy(() -> rdf4LdService.mapToLdInstance(inputStream, contentType))
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RDF parsing error");
  }

}
