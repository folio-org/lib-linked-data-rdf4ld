package org.folio.rdf4ld.mapper;

import static java.util.Optional.of;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.rdf4ld.test.TestUtil.emptyMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
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
  void mapRdfToLd_shouldReturnEmptySetWhenCoreRdf2LdInstanceMapperReturnsNoStatements() {
    // given
    var model = new ModelBuilder().build();
    var resourceMapping = new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef());
    doReturn(resourceMapping).when(mappingProfileReader).getBibframe20Profile();

    // when
    var result = topMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnSetWithResourcesMappedByAccordingMapper() {
    // given
    var model = mock(Model.class);
    doReturn(model).when(model).filter(any(), any(), any());
    var statement = mock(Statement.class);
    doReturn(Stream.of(statement)).when(model).stream();
    var resource = mock(org.eclipse.rdf4j.model.Resource.class);
    doReturn(resource).when(statement).getSubject();
    var resourceMapping = new ResourceMapping()
      .bfResourceDef(new BfResourceDef().addTypeSetItem("http://aaa.com/bfType"))
      .ldResourceDef(new LdResourceDef());
    doReturn(resourceMapping).when(mappingProfileReader).getBibframe20Profile();
    var mapper = mock(RdfMapperUnit.class);
    doReturn(mapper).when(rdfMapperUnitProvider).getMapper(any());
    var expectedResource = new Resource().setId(123L);
    doReturn(of(expectedResource)).when(mapper).mapToLd(any(), any(), any(), any());

    // when
    var result = topMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1).contains(expectedResource);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnModelEnrichedByAccordingMapper() {
    // given
    var mappingProfile = new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef());
    doReturn(mappingProfile).when(mappingProfileReader).getBibframe20Profile();
    doReturn(emptyMapper()).when(rdfMapperUnitProvider).getMapper(any());

    // when
    var result = topMapper.mapLdToBibframe2Rdf(null);

    // then
    assertThat(result).hasSize(1);
  }

}
