package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.rdf4ld.test.TestUtil.emptyMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.MappingProfileReader;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class Rdf4LdMapperTest {

  @InjectMocks
  private Rdf4LdMapperImpl topMapper;
  @Mock
  private MappingProfileReader mappingProfileReader;
  @Mock
  private CoreRdf2LdMapper coreRdf2LdMapper;
  @Mock
  private RdfMapperUnitProvider rdfMapperUnitProvider;

  @Test
  void mapToLd_shouldReturnEmptySetWhenCoreRdf2LdInstanceMapperReturnsNoStatements() {
    // given
    var model = new ModelBuilder().build();
    doReturn(new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef())
    ).when(mappingProfileReader).getInstanceBibframe20Profile();

    // when
    var result = topMapper.mapToLdInstance(model);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void mapToLdInstance_shouldReturnSetWithResourcesMappedByAccordingMapper() {
    // given
    var model = new ModelBuilder().build();
    doReturn(new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef())
    ).when(mappingProfileReader).getInstanceBibframe20Profile();
    var resource = mock(org.eclipse.rdf4j.model.Resource.class);
    doReturn(Stream.of(resource)).when(coreRdf2LdMapper).selectSubjectsByType(any(), any());
    var mapper = mock(RdfMapperUnit.class);
    doReturn(mapper).when(rdfMapperUnitProvider).getMapper(any());
    var expectedResource = new Resource().setId(123L);
    doReturn(expectedResource).when(mapper).mapToLd(any(), any(), any(), any(), any());

    // when
    var result = topMapper.mapToLdInstance(model);

    // then
    assertThat(result).hasSize(1).contains(expectedResource);
  }

  @Test
  void mapToBibframeRdfInstance_shouldReturnModelEnrichedByAccordingMapper() {
    // given
    doReturn(new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef())
    ).when(mappingProfileReader).getInstanceBibframe20Profile();
    doReturn(emptyMapper()).when(rdfMapperUnitProvider).getMapper(any());

    // when
    var result = topMapper.mapToBibframeRdfInstance(null);

    // then
    assertThat(result).hasSize(1);
  }

}
