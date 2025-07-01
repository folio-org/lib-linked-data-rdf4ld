package org.folio.rdf4ld.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SYSTEM_DETAILS;
import static org.folio.rdf4ld.test.MonographUtil.getJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceUtilTest {

  @Test
  void getPrimaryMainTitle_shouldReturnEmptyStringWhenResourceIsNull() {
    // given
    var resource = (Resource) null;

    // when
    var result = ResourceUtil.getPrimaryMainTitle(resource);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getPrimaryMainTitle_shouldReturnEmptyStringWhenOutgoingEdgesAreNull() {
    // given
    var resource = new Resource();

    // when
    var result = ResourceUtil.getPrimaryMainTitle(resource);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getPrimaryMainTitle_shouldReturnTitleWhenValidOutgoingEdgesExist() {
    // given
    var target = new Resource();
    target.setTypes(Set.of(ResourceTypeDictionary.TITLE));
    target.setDoc(getJsonNode(Map.of(
      MAIN_TITLE.getValue(), List.of("Main Title"),
      SUBTITLE.getValue(), List.of("Subtitle"))
    ));
    var edge = new ResourceEdge(new Resource(), target, PredicateDictionary.TITLE);
    var resource = new Resource();
    resource.setOutgoingEdges(Set.of(edge));

    // when
    var result = ResourceUtil.getPrimaryMainTitle(resource);

    // then
    assertThat(result).isEqualTo("Main Title, Subtitle");
  }

  @Test
  void getPropertyString_shouldReturnEmptyStringWhenDocIsNull() {
    // given
    var doc = (JsonNode) null;

    // when
    var result = ResourceUtil.getPropertyString(doc, MISC_INFO);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getPropertyString_shouldReturnEmptyStringWhenPropertyDoesNotExist() {
    // given
    var doc = getJsonNode(Map.of());

    // when
    var result = ResourceUtil.getPropertyString(doc, MISC_INFO);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getPropertyString_shouldReturnPropertyValueWhenPropertyExists() {
    // given
    var doc = getJsonNode(Map.of(MISC_INFO.getValue(), List.of("Misc info")));

    // when
    var result = ResourceUtil.getPropertyString(doc, MISC_INFO);

    // then
    assertThat(result).isEqualTo("Misc info");
  }

  @Test
  void getPropertiesString_shouldReturnConcatenatedValuesForMultipleProperties() {
    // given
    var doc = getJsonNode(Map.of(
      MISC_INFO.getValue(), List.of("Misc info"),
      SYSTEM_DETAILS.getValue(), List.of("System details")
    ));

    // when
    var result = ResourceUtil.getPropertiesString(doc, MISC_INFO, SYSTEM_DETAILS);

    // then
    assertThat(result).isEqualTo("Misc info, System details");
  }

  @Test
  void copyWithoutPreferred_removesPreferredProperty_whenDocContainsPreferred() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.set("http://library.link/vocab/resourcePreferred", JsonNodeFactory.instance.arrayNode().add("true"));
    resource.setDoc(doc);

    // when
    var result = ResourceUtil.copyWithoutPreferred(resource);

    // then
    assertThat(result).isNotNull();
    assertThat(result.has("http://library.link/vocab/resourcePreferred")).isFalse();
  }

  @Test
  void copyWithoutPreferred_returnsNull_whenDocIsNull() {
    // given
    var resource = new Resource();
    resource.setDoc(null);

    // when
    var result = ResourceUtil.copyWithoutPreferred(resource);

    // then
    assertThat(result).isNull();
  }

  @Test
  void copyWithoutPreferred_returnsUnchangedCopy_whenDocDoesNotContainPreferred() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.put("otherProperty", "value");
    resource.setDoc(doc);

    // when
    var result = ResourceUtil.copyWithoutPreferred(resource);

    // then
    assertThat(result).isNotNull();
    assertThat(result.has("otherProperty")).isTrue();
    assertThat(result.get("otherProperty").asText()).isEqualTo("value");
  }

}
