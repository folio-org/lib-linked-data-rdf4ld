package org.folio.rdf4ld.mapper;

import static java.util.Optional.of;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.rdf4ld.test.TestUtil.emptyMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.MappingProfile;
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
    var mappingProfile = new MappingProfile()
      .addTopResourceMappingsItem(resourceMapping);
    doReturn(mappingProfile).when(mappingProfileReader).getBibframe20Profile();

    // when
    var result = topMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnSetWithResourcesMappedByAccordingMapper() {
    // given
    var model = new ModelBuilder().build();
    var bfType = "http://aaa.com/bfType";
    var resource = Values.iri("http://example.org/resource1");

    // Add subject with type to model
    new ModelBuilder(model).subject(resource)
      .add(RDF.TYPE, Values.iri(bfType));

    var resourceMapping = new ResourceMapping()
      .bfResourceDef(new BfResourceDef().addTypeSetItem(bfType))
      .ldResourceDef(new LdResourceDef());
    var mappingProfile = new MappingProfile()
      .addTopResourceMappingsItem(resourceMapping);
    doReturn(mappingProfile).when(mappingProfileReader).getBibframe20Profile();

    var mapper = mock(RdfMapperUnit.class);
    doReturn(mapper).when(rdfMapperUnitProvider).getMapper(any(), any());
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
    var resource = new Resource()
      .setId(100L)
      .setTypes(java.util.Set.of());

    var resourceMapping = new ResourceMapping()
      .bfResourceDef(new BfResourceDef())
      .ldResourceDef(new LdResourceDef());
    var mappingProfile = new MappingProfile()
      .addTopResourceMappingsItem(resourceMapping);
    doReturn(mappingProfile).when(mappingProfileReader).getBibframe20Profile();
    doReturn(emptyMapper()).when(rdfMapperUnitProvider).getMapper(any(), any());

    // when
    var result = topMapper.mapLdToBibframe2Rdf(resource);

    // then
    assertThat(result).isNotNull();
  }

  @Test
  void mapRdfToLd_withMappingProfile_shouldMapAllTopResourceMappings() {
    // given
    var model = new ModelBuilder().build();

    var type1 = "http://aaa.com/type1";
    var type2 = "http://aaa.com/type2";

    var resource1 = Values.iri("http://example.org/resource1");
    var resource2 = Values.iri("http://example.org/resource2");

    // Add subjects with types to model
    new ModelBuilder(model).subject(resource1)
      .add(RDF.TYPE, Values.iri(type1));
    new ModelBuilder(model).subject(resource2)
      .add(RDF.TYPE, Values.iri(type2));

    var resourceMapping1 = new ResourceMapping()
      .bfResourceDef(new BfResourceDef().addTypeSetItem(type1))
      .ldResourceDef(new LdResourceDef());
    var resourceMapping2 = new ResourceMapping()
      .bfResourceDef(new BfResourceDef().addTypeSetItem(type2))
      .ldResourceDef(new LdResourceDef());

    var mappingProfile = new MappingProfile()
      .addTopResourceMappingsItem(resourceMapping1)
      .addTopResourceMappingsItem(resourceMapping2);

    var mapper = mock(RdfMapperUnit.class);
    doReturn(mapper).when(rdfMapperUnitProvider).getMapper(any(), any());
    var expectedResource1 = new Resource().setId(123L);
    var expectedResource2 = new Resource().setId(456L);
    doReturn(of(expectedResource1)).doReturn(of(expectedResource2)).when(mapper).mapToLd(any(), any(), any(), any());

    // when
    var result = topMapper.mapRdfToLd(model, mappingProfile);

    // then
    assertThat(result).hasSize(2).contains(expectedResource1, expectedResource2);
  }

  @Test
  void mapRdfToLd_shouldHandleEmptyTopResourceMappings() {
    // given
    var model = new ModelBuilder().build();
    var mappingProfile = new MappingProfile();

    // when
    var result = topMapper.mapRdfToLd(model, mappingProfile);

    // then
    assertThat(result).isEmpty();
  }

}
