package org.folio.rdf4ld.mapper.unit.monograph;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.rdf4ld.test.MonographUtil.createPrimaryTitle;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InstanceRdfMapperUnitTest {

  @InjectMocks
  private InstanceRdfMapperUnit instanceRdfMapperUnit;
  @Mock
  private BaseRdfMapperUnit baseRdfMapperUnit;
  @Mock
  private FingerprintHashService hashService;

  @Test
  void mapToLd_shouldSetLabelAndRecalculateId() {
    // given
    var model = mock(Model.class);
    var resource = mock(org.eclipse.rdf4j.model.Resource.class);
    var resourceMapping = mock(ResourceMapping.class);
    var mappedResource = new Resource()
      .setId(123L);
    mappedResource.addOutgoingEdge(new ResourceEdge(mappedResource, createPrimaryTitle(), TITLE));
    doReturn(mappedResource).when(baseRdfMapperUnit).mapToLd(model, resource, resourceMapping, null);
    long newId = 789L;
    doReturn(newId).when(hashService).hash(mappedResource);

    // when
    var result = instanceRdfMapperUnit.mapToLd(model, resource, resourceMapping, null);

    // then
    assertThat(result.getId()).isEqualTo(newId);
    assertThat(result.getLabel())
      .isEqualTo("Title mainTitle 1, Title mainTitle 2, Title subTitle 1, Title subTitle 2");
  }
}
