package org.folio.rdf4ld.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MappingProfileReaderTest {

  @InjectMocks
  private MappingProfileReader mappingProfileReader;

  @Test
  void getBibframe20Profile_shouldReturnMappingProfileWhenFilesAreValid() throws Exception {
    // when
    var result = mappingProfileReader.getBibframe20Profile();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTopResourceMappings()).isNotNull();
    assertThat(result.getTopResourceMappings()).hasSize(2); // Instance and Hub
  }

}
