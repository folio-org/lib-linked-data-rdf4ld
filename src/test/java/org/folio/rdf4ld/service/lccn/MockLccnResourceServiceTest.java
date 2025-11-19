package org.folio.rdf4ld.service.lccn;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.rdf4ld.test.TestUtil.mockLccnResource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MockLccnResourceServiceTest {

  @InjectMocks
  private MockLccnResourceServiceImpl lccnMockResourceService;
  @Mock
  private FingerprintHashService fingerprintHashService;
  @Mock
  private RdfMapperUnitProvider rdfMapperUnitProvider;

  @Test
  void mockLccnResource_shouldReturnMockResource() {
    // given
    var lccn = UUID.randomUUID().toString();
    var expectedLabel = "LCCN_RESOURCE_MOCK_" + lccn;

    // when
    var result = lccnMockResourceService.mockLccnResource(lccn);

    // then
    assertThat(result.getId()).isEqualTo(expectedLabel.hashCode());
    assertThat(result.getLabel()).isEqualTo(expectedLabel);
  }

  @ParameterizedTest
  @CsvSource({
    "LCCN_RESOURCE_MOCK_123, true",
    "123, false"
  })
  void isMockLccnResource_shouldReturnCorrectResult(String label, boolean expected) {
    // given
    var resource = new Resource().setLabel(label);

    // when
    var result = lccnMockResourceService.isMockLccnResource(resource);

    // then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void gatherLccns_shouldReturnAllLccnValues() {
    // given
    var lccn1 = UUID.randomUUID().toString();
    var lccn2 = UUID.randomUUID().toString();
    var lccn3 = UUID.randomUUID().toString();
    var resources = Set.of(
      mockLccnResource(lccn1),
      mockLccnResource(lccn2),
      mockLccnResource(lccn3)
    );

    // when
    var result = lccnMockResourceService.gatherLccns(resources);

    // then
    assertThat(result).containsExactlyInAnyOrder(lccn1, lccn2, lccn3);
  }

  @Test
  void unMockLccnResources_shouldReplaceAllMockResourcesWithRealOnes() {
    // given
    var lccn1 = UUID.randomUUID().toString();
    var lccn2 = UUID.randomUUID().toString();

    var mockResource1 = mockLccnResource(lccn1);
    var mockResource2 = mockLccnResource(lccn2);

    var realResource1 = new Resource().setId(1L).setLabel("Real LCCN 1");
    var realResource2 = new Resource().setId(2L).setLabel("Real LCCN 2");

    var parentResource = new Resource()
      .setId(100L)
      .setLabel("Parent Resource");

    var edge1 = new ResourceEdge(parentResource, mockResource1, PredicateDictionary.MAP);
    var edge2 = new ResourceEdge(parentResource, mockResource2, PredicateDictionary.MAP);

    parentResource.setOutgoingEdges(Set.of(edge1, edge2));
    var resources = Set.of(parentResource);

    Function<String, Resource> lccnProvider = lccn -> {
      if (lccn.equals(lccn1)) {
        return realResource1;
      }
      if (lccn.equals(lccn2)) {
        return realResource2;
      }
      return null;
    };

    var mapperUnit = mock(RdfMapperUnit.class);
    when(mapperUnit.enrichUnMockedResource(realResource1)).thenReturn(realResource1);
    when(mapperUnit.enrichUnMockedResource(realResource2)).thenReturn(realResource2);
    when(rdfMapperUnitProvider.getMapper(any(), any())).thenReturn(mapperUnit);
    when(fingerprintHashService.hash(any())).thenReturn(200L);

    // when
    var result = lccnMockResourceService.unMockLccnResources(resources, lccnProvider);

    // then
    assertThat(result).hasSize(1);
    assertThat(parentResource.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .containsExactlyInAnyOrder(realResource1, realResource2);
    verify(fingerprintHashService).hash(parentResource);
  }

  @Test
  void unMockLccnResources_shouldReturnResourcesUnchangedWhenNoMockLccns() {
    // given
    var regularResource = new Resource()
      .setId(1L)
      .setLabel("Regular Resource")
      .setOutgoingEdges(Set.of());

    var resources = Set.of(regularResource);
    Function<String, Resource> lccnProvider = lccn -> null;

    // when
    var result = lccnMockResourceService.unMockLccnResources(resources, lccnProvider);

    // then
    assertThat(result).containsExactly(regularResource);
    verify(fingerprintHashService, never()).hash(any());
  }

  @Test
  void unMockLccnResources_shouldHandleNestedMockResources() {
    // given
    var lccn = UUID.randomUUID().toString();
    var mockResource = mockLccnResource(lccn);
    var realResource = new Resource().setId(1L).setLabel("Real LCCN");

    var childResource = new Resource().setId(10L).setLabel("Child");
    var parentResource = new Resource().setId(100L).setLabel("Parent");

    var edgeToMock = new ResourceEdge(childResource, mockResource, PredicateDictionary.MAP);
    childResource.setOutgoingEdges(Set.of(edgeToMock));

    var edgeToChild = new ResourceEdge(parentResource, childResource, PredicateDictionary.MAP);
    parentResource.setOutgoingEdges(Set.of(edgeToChild));

    var resources = Set.of(parentResource);
    Function<String, Resource> lccnProvider = l -> l.equals(lccn) ? realResource : null;

    var mapperUnit = mock(RdfMapperUnit.class);
    when(mapperUnit.enrichUnMockedResource(realResource)).thenReturn(realResource);
    when(rdfMapperUnitProvider.getMapper(any(), any())).thenReturn(mapperUnit);
    when(fingerprintHashService.hash(childResource)).thenReturn(childResource.getId() + 50);
    when(fingerprintHashService.hash(parentResource)).thenReturn(parentResource.getId() + 50);

    // when
    var result = lccnMockResourceService.unMockLccnResources(resources, lccnProvider);

    // then
    assertThat(result).containsExactly(parentResource);
    assertThat(childResource.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .containsExactly(realResource);
    verify(fingerprintHashService).hash(childResource);
    verify(fingerprintHashService).hash(parentResource);
  }

}
