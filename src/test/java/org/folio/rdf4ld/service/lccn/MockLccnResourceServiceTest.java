package org.folio.rdf4ld.service.lccn;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.AGENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.rdf4ld.test.MonographUtil.createAgent;
import static org.folio.rdf4ld.test.MonographUtil.getJsonNode;
import static org.folio.rdf4ld.test.TestUtil.mockLccnResource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnitProvider;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  void mockLccnResource_shouldReturnNewMockResource_ifNoMappedOne() {
    // given
    var lccn = UUID.randomUUID().toString();

    // when
    var result = lccnMockResourceService.mockLccnResource(null, lccn);

    // then
    assertThat(result.getId()).isEqualTo(lccn.hashCode());
    assertThat(result.getLabel()).isEqualTo(lccn);
  }

  @Test
  void mockLccnResource_shouldReturnSameResourceMocked_ifMappedOneProvided() {
    // given
    var lccn = UUID.randomUUID().toString();
    var mapped = createAgent(UUID.randomUUID().toString(), ID_LCNAF, true, List.of(AGENT, PERSON), "agentLabel");

    // when
    var result = lccnMockResourceService.mockLccnResource(mapped, lccn);

    // then
    assertThat(result.getId()).isEqualTo(lccn.hashCode());
    assertThat(result.getLabel()).isEqualTo(lccn);
    assertThat(result).isEqualTo(mapped);
    assertThat(result.getDoc()).isNotEmpty();
    assertThat(result.getTypes()).isNotEmpty();
    assertThat(result.getOutgoingEdges()).isNotEmpty();
  }

  @Test
  void gatherLccns_shouldReturnAllLccnValuesRecursively() {
    // given
    var lccn1 = UUID.randomUUID().toString();
    var lccn2 = UUID.randomUUID().toString();
    var lccn4 = UUID.randomUUID().toString();
    var mockResource1 = mockLccnResource(lccn1);
    var mockResource2 = mockLccnResource(lccn2);
    var mockResource4 = mockLccnResource(lccn4);
    var regularResource = new Resource()
      .setId(100L)
      .setLabel("Regular Resource");
    mockResource1.setOutgoingEdges(Set.of(new ResourceEdge(mockResource1, mockResource2, MAP)));
    var lccn3 = UUID.randomUUID().toString();
    var mockResource3 = mockLccnResource(lccn3);
    mockResource2.setOutgoingEdges(Set.of(new ResourceEdge(mockResource2, mockResource3, MAP)));
    regularResource.setOutgoingEdges(Set.of(new ResourceEdge(regularResource, mockResource4, MAP)));
    var lccn5 = UUID.randomUUID().toString();
    var mockResource5 = mockLccnResource(lccn5);
    mockResource4.setOutgoingEdges(Set.of(new ResourceEdge(mockResource4, mockResource5, MAP)));

    var resources = Set.of(mockResource1, regularResource);

    // when
    var result = lccnMockResourceService.gatherLccns(resources);

    // then
    assertThat(result).containsExactlyInAnyOrder(lccn1, lccn2, lccn3, lccn4, lccn5);
  }

  @Test
  void unMockLccnEdges_shouldReplaceAllMockEdgesWithRealOnes() {
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

    var edge1 = new ResourceEdge(parentResource, mockResource1, MAP);
    var edge2 = new ResourceEdge(parentResource, mockResource2, MAP);

    parentResource.setOutgoingEdges(Set.of(edge1, edge2));

    Function<String, Optional<Resource>> lccnProvider = lccn -> {
      if (lccn.equals(lccn1)) {
        return of(realResource1);
      }
      if (lccn.equals(lccn2)) {
        return of(realResource2);
      }
      return empty();
    };

    var mapperUnit = mock(RdfMapperUnit.class);
    when(mapperUnit.enrichUnMockedResource(realResource1)).thenReturn(realResource1);
    when(mapperUnit.enrichUnMockedResource(realResource2)).thenReturn(realResource2);
    when(rdfMapperUnitProvider.getMapper(any(), any())).thenReturn(mapperUnit);
    when(fingerprintHashService.hash(any())).thenReturn(200L);

    // when
    var result = lccnMockResourceService.unMockLccnEdges(parentResource, lccnProvider);

    // then
    assertThat(result).isEqualTo(parentResource);
    assertThat(result.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .containsExactlyInAnyOrder(realResource1, realResource2);
    verify(fingerprintHashService).hash(parentResource);
  }

  @Test
  void unMockLccnEdges_shouldKeepEdgesUnchangedWhenNoMockLccns() {
    // given
    var regularResource = new Resource()
      .setId(1L)
      .setLabel("Regular Resource")
      .setOutgoingEdges(Set.of());

    Function<String, Optional<Resource>> lccnProvider = lccn -> empty();

    // when
    var result = lccnMockResourceService.unMockLccnEdges(regularResource, lccnProvider);

    // then
    assertThat(result).isEqualTo(regularResource);
    verify(fingerprintHashService, never()).hash(any());
  }

  @Test
  void unMockLccnEdges_shouldHandleMockEdges() {
    // given
    var lccn = UUID.randomUUID().toString();
    var mockResource = mockLccnResource(lccn);
    var realResource = new Resource().setId(1L).setLabel("Real LCCN");

    var childResource = new Resource().setId(10L).setLabel("Child");
    var parentResource = new Resource().setId(100L).setLabel("Parent");

    var edgeToMock = new ResourceEdge(childResource, mockResource, MAP);
    childResource.setOutgoingEdges(Set.of(edgeToMock));

    var edgeToChild = new ResourceEdge(parentResource, childResource, MAP);
    parentResource.setOutgoingEdges(Set.of(edgeToChild));

    Function<String, Optional<Resource>> lccnProvider = l -> l.equals(lccn) ? of(realResource) : empty();

    var mapperUnit = mock(RdfMapperUnit.class);
    when(mapperUnit.enrichUnMockedResource(realResource)).thenReturn(realResource);
    when(rdfMapperUnitProvider.getMapper(any(), any())).thenReturn(mapperUnit);
    when(fingerprintHashService.hash(any())).thenAnswer(invocation -> {
      Resource r = invocation.getArgument(0);
      return r.getId() + 50;
    });

    // when
    var result = lccnMockResourceService.unMockLccnEdges(parentResource, lccnProvider);

    // then
    assertThat(result).isEqualTo(parentResource);
    assertThat(childResource.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .containsExactly(realResource);
    verify(fingerprintHashService).hash(childResource);
  }

  @Test
  void unMockLccnEdges_shouldUnMockResourceNotProvidedByLccnButWithLocalData() {
    // given
    var lccn = UUID.randomUUID().toString();
    var realLabel = "real label";
    var mockResourceDoc = getJsonNode(Map.of(
      LABEL.getValue(), List.of(realLabel),
      NAME.getValue(), List.of(realLabel))
    );
    var mockResource = mockLccnResource(lccn)
      .addType(AGENT)
      .setDoc(mockResourceDoc);
    var childResource = new Resource().setId(10L).setLabel("Child");
    mockResource.setOutgoingEdges(Set.of(new ResourceEdge(mockResource, childResource, MAP)));
    var parentResource = new Resource().setId(100L).setLabel("Parent");
    parentResource.setOutgoingEdges(Set.of(new ResourceEdge(parentResource, mockResource, MAP)));

    Function<String, Optional<Resource>> lccnProvider = l -> empty();

    var mapperUnit = mock(RdfMapperUnit.class);
    when(mapperUnit.enrichUnMockedResource(mockResource)).thenReturn(mockResource);
    when(rdfMapperUnitProvider.getMapper(any(), any())).thenReturn(mapperUnit);
    when(fingerprintHashService.hash(mockResource)).thenReturn(mockResource.getId() + 50);

    // when
    var result = lccnMockResourceService.unMockLccnEdges(parentResource, lccnProvider);

    // then
    assertThat(result).isEqualTo(parentResource);
    assertThat(parentResource.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .containsExactly(mockResource);
    assertThat(mockResource.getLabel()).isEqualTo(realLabel);
    assertThat(mockResource.getTypes()).isEqualTo(Set.of(AGENT));
    assertThat(mockResource.getDoc()).isEqualTo(mockResourceDoc);
    assertThat(mockResource.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .containsExactly(childResource);
    verify(fingerprintHashService).hash(mockResource);
    verify(fingerprintHashService, never()).hash(childResource);
  }

}
