package org.folio.rdf4ld.mapper.unit.monograph;

import static java.util.Optional.of;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.rdf4ld.test.MonographUtil.createPrimaryTitle;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Set;
import java.util.function.LongFunction;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
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
class WorkRdfMapperUnitTest {

  @InjectMocks
  private WorkRdfMapperUnit workRdfMapperUnit;
  @Mock
  private BaseRdfMapperUnit baseRdfMapperUnit;
  @Mock
  private FingerprintHashService hashService;
  @Mock
  private LongFunction<String> resourceUrlProvider;

  @Test
  void mapToLd_shouldSetLabelAndRecalculateId() {
    // given
    var model = mock(Model.class);
    var resource = mock(org.eclipse.rdf4j.model.Resource.class);
    var mapping = mock(ResourceMapping.class);
    var mappedResource = new Resource()
      .setId(123L);
    mappedResource.addOutgoingEdge(new ResourceEdge(mappedResource, createPrimaryTitle(""), TITLE));
    doReturn(of(mappedResource)).when(baseRdfMapperUnit).mapToLd(model, resource, mapping, null);
    long newId = 789L;
    doReturn(newId).when(hashService).hash(mappedResource);
    doReturn(model).when(model).filter(resource, RDF.TYPE, null);
    var statement = mock(Statement.class);
    var value = mock(Value.class);
    doReturn("http://id.loc.gov/ontologies/bibframe/Monograph").when(value).stringValue();
    doReturn(value).when(statement).getObject();
    doReturn(Set.of(statement).stream()).when(model).stream();

    // when
    var result = workRdfMapperUnit.mapToLd(model, resource, mapping, null);

    // then
    assertThat(result).isPresent()
      .hasValueSatisfying(w -> assertThat(w.getId()).isEqualTo(newId))
      .hasValueSatisfying(w -> assertThat(w.getLabel())
        .isEqualTo("Title mainTitle 1, Title mainTitle 2, Title subTitle 1, Title subTitle 2"));
  }
}
