package org.folio.rdf4ld.util;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.ResourceInternalMapping;
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
  void getBibframe20Profile_shouldReturnMappingProfileWhenFilesAreValid() throws Exception {
    // given
    when(objectMapper.readValue(any(InputStream.class), eq(ResourceMapping.class)))
      .thenAnswer(inv -> getResourceMapping(randomUUID().toString()));

    // when
    var result = mappingProfileReader.getBibframe20Profile();

    // then
    assertThat(result).isNotNull();
  }

  @Test
  void getBibframe20Profile_shouldReturnNullWhenExceptionOccurs() throws Exception {
    // given
    when(objectMapper.readValue(any(InputStream.class), eq(ResourceMapping.class))).thenThrow(new IOException());

    // when
    var result = mappingProfileReader.getBibframe20Profile();

    // then
    assertThat(result).isNull();
  }

  private ResourceMapping getResourceMapping(String random) {
    return new ResourceMapping()
      .bfResourceDef(new BfResourceDef().predicate(random))
      .resourceMapping(new ResourceInternalMapping());
  }

}
