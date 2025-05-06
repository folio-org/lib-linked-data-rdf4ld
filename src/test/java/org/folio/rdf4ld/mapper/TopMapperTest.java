package org.folio.rdf4ld.mapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.rdf4ld.test.TestUtil.emptyMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.mapper.unit.MapperUnitProvider;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.DefaultMappingProfileReader;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class TopMapperTest {

  @InjectMocks
  private TopMapperImpl topMapper;
  @Mock
  private DefaultMappingProfileReader defaultMappingProfileReader;
  @Mock
  private CoreRdf2LdMapper coreRdf2LdMapper;
  @Mock
  private MapperUnitProvider mapperUnitProvider;

  @Test
  void mapToLd_shouldReturnEmptySetWhenCoreRdf2LdMapperReturnsNoStatements() {
    // given
    var model = new ModelBuilder().build();
    doReturn(new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef())
    ).when(defaultMappingProfileReader).getInstanceBibframe20Profile();
    doReturn(Stream.empty()).when(coreRdf2LdMapper).selectStatementsByType(any(), any());

    // when
    var result = topMapper.mapToLd(model);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void mapToLd_shouldReturnSetWithResourcesMappedByAccordingMapper() {
    // given
    var model = new ModelBuilder().build();
    doReturn(new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef())
    ).when(defaultMappingProfileReader).getInstanceBibframe20Profile();
    var statement = mock(Statement.class);
    doReturn(Stream.of(statement)).when(coreRdf2LdMapper).selectStatementsByType(any(), any());
    var mapper = mock(MapperUnit.class);
    doReturn(mapper).when(mapperUnitProvider).getMapper(any());
    var expectedResource = new Resource().setId(123L);
    doReturn(expectedResource).when(mapper).mapToLd(any(), any(), any(), any(), any());

    // when
    var result = topMapper.mapToLd(model);

    // then
    assertThat(result).hasSize(1).contains(expectedResource);
  }

  @Test
  void mapToBibframeRdf_shouldReturnModelEnrichedByAccordingMapper() {
    // given
    doReturn(new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef())
    ).when(defaultMappingProfileReader).getInstanceBibframe20Profile();
    doReturn(emptyMapper()).when(mapperUnitProvider).getMapper(any());

    // when
    var result = topMapper.mapToBibframeRdf(null);

    // then
    assertThat(result).hasSize(1);
  }

}
