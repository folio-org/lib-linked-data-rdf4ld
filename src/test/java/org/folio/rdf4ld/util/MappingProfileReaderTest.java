package org.folio.rdf4ld.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MappingProfileReaderTest {

  @InjectMocks
  private MappingProfileReader mappingProfileReader;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  void getInstanceBibframe20Profile_shouldReturnResourceMappingWhenFileIsValid() throws Exception {
    // given
    var expectedMapping = new ResourceMapping();
    when(objectMapper.readValue(any(InputStream.class), eq(ResourceMapping.class))).thenReturn(expectedMapping);

    // when
    var result = mappingProfileReader.getInstanceBibframe20Profile();

    // then
    assertThat(result).isEqualTo(expectedMapping);
  }

  @Test
  void getInstanceBibframe20Profile_shouldReturnNullWhenExceptionOccurs() throws Exception {
    // given
    when(objectMapper.readValue(any(InputStream.class), eq(ResourceMapping.class))).thenThrow(new IOException());

    // when
    var result = mappingProfileReader.getInstanceBibframe20Profile();

    // then
    assertThat(result).isNull();
  }

}
