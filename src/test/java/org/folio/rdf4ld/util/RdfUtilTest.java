package org.folio.rdf4ld.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rdf4ld.test.RdfTestUtil.createBlankNode;
import static org.folio.rdf4ld.test.RdfTestUtil.createIri;
import static org.folio.rdf4ld.test.RdfTestUtil.createMixedRdfList;
import static org.folio.rdf4ld.test.RdfTestUtil.createRdfList;
import static org.folio.rdf4ld.test.RdfTestUtil.createSingleElementList;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class RdfUtilTest {

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("emptyListScenarios")
  void extractRdfList_shouldReturnEmptyList(String scenario, Resource listHead) {
    // given
    var model = new LinkedHashModel();

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).isEmpty();
  }

  private static Stream<Arguments> emptyListScenarios() {
    return Stream.of(
      Arguments.of("when listHead is NIL", RDF.NIL),
      Arguments.of("when listHead has no FIRST element", Values.bnode()),
      Arguments.of("when listHead is null", null)
    );
  }

  @Test
  void extractRdfList_shouldReturnSingleElementList() {
    // given
    var model = new ModelBuilder().build();
    var element = createIri("http://example.org/element1");
    var listHead = createSingleElementList(model, element);

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(element);
  }

  @Test
  void extractRdfList_shouldReturnMultipleElementList() {
    // given
    var model = new ModelBuilder().build();
    var element1 = createIri("http://example.org/element1");
    var element2 = createIri("http://example.org/element2");
    var element3 = createIri("http://example.org/element3");
    var listHead = createRdfList(model, List.of(element1, element2, element3));

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.getFirst()).isEqualTo(element1);
    assertThat(result.get(1)).isEqualTo(element2);
    assertThat(result.get(2)).isEqualTo(element3);
  }

  @Test
  void extractRdfList_shouldHandleBlankNodeElements() {
    // given
    var model = new ModelBuilder().build();
    var blankNodeElement = createBlankNode("element");
    var listHead = createSingleElementList(model, blankNodeElement);

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(blankNodeElement);
  }

  @Test
  void extractRdfList_shouldStopWhenNoRestElement() {
    // given
    var model = new ModelBuilder().build();
    var listHead = Values.bnode("list");
    var element = Values.iri("http://example.org/element1");

    var modelBuilder = new ModelBuilder(model);
    modelBuilder.subject(listHead)
      .add(RDF.FIRST, element);
    // No RDF.REST added

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(element);
  }

  @Test
  void extractRdfList_shouldSkipNonResourceElements() {
    // given
    var model = new ModelBuilder().build();
    var listHead = Values.bnode("list1");
    var node2 = Values.bnode("list2");

    var element1 = Values.iri("http://example.org/element1");
    var literalElement = Values.literal("not a resource");

    var modelBuilder = new ModelBuilder(model);
    modelBuilder.subject(listHead)
      .add(RDF.FIRST, literalElement)  // This should be skipped
      .add(RDF.REST, node2);

    modelBuilder.subject(node2)
      .add(RDF.FIRST, element1)
      .add(RDF.REST, RDF.NIL);

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(element1);
  }

  @Test
  void extractRdfList_shouldHandleMixedIriAndBlankNodes() {
    // given
    var model = new ModelBuilder().build();
    var iriElement = createIri("http://example.org/element1");
    var blankElement = createBlankNode("blankElement");
    var iriElement2 = createIri("http://example.org/element3");
    var listHead = createMixedRdfList(model, iriElement, blankElement, iriElement2);

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.getFirst()).isEqualTo(iriElement);
    assertThat(result.get(1)).isEqualTo(blankElement);
    assertThat(result.get(2)).isEqualTo(iriElement2);
  }

  @ParameterizedTest(name = "should handle list with {0} elements")
  @ValueSource(ints = {1, 3, 5, 10, 20})
  void extractRdfList_shouldHandleListsOfVariousLengths(int listSize) {
    // given
    var model = new ModelBuilder().build();
    var elements = IntStream.range(0, listSize)
      .mapToObj(i -> createIri("http://example.org/element" + i))
      .toList();

    var listHead = createRdfList(model, elements);

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(listSize);
    IntStream.range(0, listSize).forEach(i -> assertThat(result.get(i)).isEqualTo(elements.get(i)));
  }

  @Test
  void extractRdfList_shouldHandleRealWorldComplexSubjectComponentList() {
    // given - simulating the ComplexSubject componentList structure
    var model = new ModelBuilder().build();
    var topicIri = createIri("http://id.loc.gov/authorities/subjects/sh85070981");
    var personIri = createIri("http://id.loc.gov/rwo/agents/n79026681");
    var familyBlank = createBlankNode("familyAgent");
    var listHead = createMixedRdfList(model, topicIri, personIri, familyBlank);

    // when
    var result = RdfUtil.extractRdfList(model, listHead);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.getFirst()).isEqualTo(topicIri);
    assertThat(result.get(1)).isEqualTo(personIri);
    assertThat(result.get(2)).isEqualTo(familyBlank);
  }

  @Test
  void selectSubjectsByTypes_shouldReturnEmptyStreamWhenNoTypesSpecified() {
    // given
    var model = new ModelBuilder().build();
    var emptyTypeSet = java.util.Set.<String>of();

    // when
    var result = RdfUtil.selectSubjectsByTypes(model, emptyTypeSet);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void selectSubjectsByTypes_shouldReturnSubjectsWithAllSpecifiedTypes() {
    // given
    var model = new ModelBuilder().build();
    var type1 = "http://example.org/Type1";
    var type2 = "http://example.org/Type2";
    var type3 = "http://example.org/Type3";

    var subject1 = createIri("http://example.org/subject1");
    var subject2 = createIri("http://example.org/subject2");
    var subject3 = createIri("http://example.org/subject3");

    // subject1 has type1 and type2
    new ModelBuilder(model).subject(subject1)
      .add(RDF.TYPE, Values.iri(type1))
      .add(RDF.TYPE, Values.iri(type2));

    // subject2 has only type1
    new ModelBuilder(model).subject(subject2)
      .add(RDF.TYPE, Values.iri(type1));

    // subject3 has all three types
    new ModelBuilder(model).subject(subject3)
      .add(RDF.TYPE, Values.iri(type1))
      .add(RDF.TYPE, Values.iri(type2))
      .add(RDF.TYPE, Values.iri(type3));

    // when - searching for subjects with both type1 AND type2
    var result = RdfUtil.selectSubjectsByTypes(model, java.util.Set.of(type1, type2));

    // then - should return only subject1 and subject3 (both have type1 and type2)
    var resultList = result.toList();
    assertThat(resultList)
      .hasSize(2)
      .containsExactlyInAnyOrder(subject1, subject3);
  }

  @Test
  void selectSubjectsByTypes_shouldReturnAllSubjectsWhenAllHaveSingleSpecifiedType() {
    // given
    var model = new ModelBuilder().build();
    var type1 = "http://example.org/Type1";

    var subject1 = createIri("http://example.org/subject1");
    var subject2 = createIri("http://example.org/subject2");

    // Both subjects have type1
    new ModelBuilder(model).subject(subject1)
      .add(RDF.TYPE, Values.iri(type1));

    new ModelBuilder(model).subject(subject2)
      .add(RDF.TYPE, Values.iri(type1));

    // when - searching for subjects with type1
    var result = RdfUtil.selectSubjectsByTypes(model, java.util.Set.of(type1));

    // then - should return both subjects
    var resultList = result.toList();
    assertThat(resultList)
      .hasSize(2)
      .containsExactlyInAnyOrder(subject1, subject2);
  }
}

