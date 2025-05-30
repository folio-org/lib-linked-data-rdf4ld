package org.folio.rdf4ld.mapper.unit.monograph;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.rdf4ld.test.MonographUtil.createPrimaryTitle;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkRdfMapperUnitTest {

  @InjectMocks
  private WorkRdfMapperUnit workRdfMapperUnit;
  @Mock
  private BaseRdfMapperUnit baseRdfMapperUnit;
  @Mock
  private FingerprintHashService hashService;

  @Test
  void mapToLd_shouldSetLabelAndRecalculateId() {
    // given
    var model = mock(Model.class);
    var resource = mock(org.eclipse.rdf4j.model.Resource.class);
    var resourceMapping = mock(ResourceInternalMapping.class);
    var ldTypes = Set.of(ResourceTypeDictionary.WORK);
    var localOnly = true;
    var mappedResource = new Resource()
      .setId(123L);
    mappedResource.addOutgoingEdge(new ResourceEdge(mappedResource, createPrimaryTitle(456L), TITLE));
    doReturn(mappedResource).when(baseRdfMapperUnit).mapToLd(model, resource, resourceMapping, ldTypes, localOnly);
    long newId = 789L;
    doReturn(newId).when(hashService).hash(mappedResource);

    // when
    var result = workRdfMapperUnit.mapToLd(model, resource, resourceMapping, ldTypes, localOnly);

    // then
    assertThat(result.getId()).isEqualTo(newId);
    assertThat(result.getLabel()).isEqualTo("Primary: mainTitle456, Primary: subTitle");
  }
}
