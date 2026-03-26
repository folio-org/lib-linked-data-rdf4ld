package org.folio.rdf4ld.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.SYSTEM_DETAILS;
import static org.folio.rdf4ld.test.MonographUtil.getJsonNode;

import java.util.List;
import java.util.Map;
import org.folio.ld.dictionary.model.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

@UnitTest
class ResourceUtilTest {

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
  void copyExcluding() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.set("http://bibfra.me/vocab/library/miscInfo", JsonNodeFactory.instance.arrayNode().add("true"));
    resource.setDoc(doc);

    // when
    var result = ResourceUtil.copyExcluding(resource, MISC_INFO);

    // then
    assertThat(result).isNotNull();
    assertThat(result.has("http://bibfra.me/vocab/library/miscInfo")).isFalse();
  }

  @Test
  void copyExcluding_returnsNull_whenDocIsNull() {
    // given
    var resource = new Resource();
    resource.setDoc(null);

    // when
    var result = ResourceUtil.copyExcluding(resource);

    // then
    assertThat(result).isNull();
  }

  @Test
  void copyWithoutSpecificProperty_returnsUnchangedCopy() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.put("otherProperty", "value");
    resource.setDoc(doc);

    // when
    var result = ResourceUtil.copyExcluding(resource);

    // then
    assertThat(result).isNotNull();
    assertThat(result.has("otherProperty")).isTrue();
    assertThat(result.get("otherProperty").asString()).isEqualTo("value");
  }

  @Test
  void copyLongestLabelToName_shouldDoNothing_whenDocIsNull() {
    // given
    var resource = new Resource();
    resource.setDoc(null);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc()).isNull();
  }

  @Test
  void copyLongestLabelToName_shouldDoNothing_whenLabelPropertyDoesNotExist() {
    // given
    var resource = new Resource();
    var doc = getJsonNode(Map.of("otherProperty", List.of("value")));
    resource.setDoc(doc);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc().has(NAME.getValue())).isFalse();
  }

  @Test
  void copyLongestLabelToName_shouldDoNothing_whenLabelIsNotArray() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.put(LABEL.getValue(), "single value");
    resource.setDoc(doc);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc().has(NAME.getValue())).isFalse();
  }

  @Test
  void copyLongestLabelToName_shouldDoNothing_whenLabelArrayIsEmpty() {
    // given
    var resource = new Resource();
    var doc = getJsonNode(Map.of(LABEL.getValue(), List.of()));
    resource.setDoc(doc);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc().has(NAME.getValue())).isFalse();
  }

  @Test
  void copyLongestLabelToName_shouldCopyLabel_whenSingleLabelExists() {
    // given
    var resource = new Resource();
    var doc = getJsonNode(Map.of(LABEL.getValue(), List.of("Single Label")));
    resource.setDoc(doc);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc().has(NAME.getValue())).isTrue();
    assertThat(resource.getDoc().get(NAME.getValue()).get(0).asString()).isEqualTo("Single Label");
  }

  @Test
  void copyLongestLabelToName_shouldCopyLongestLabel_whenMultipleLabelsExist() {
    // given
    var resource = new Resource();
    var doc = getJsonNode(Map.of(LABEL.getValue(), List.of("Short", "Medium Label", "Longest Label Here")));
    resource.setDoc(doc);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc().has(NAME.getValue())).isTrue();
    assertThat(resource.getDoc().get(NAME.getValue()).get(0).asString()).isEqualTo("Longest Label Here");
  }

  @Test
  void copyLongestLabelToName_shouldCopyFirstLongest_whenMultipleLabelsHaveSameLength() {
    // given
    var resource = new Resource();
    var doc = getJsonNode(Map.of(LABEL.getValue(), List.of("First", "Other")));
    resource.setDoc(doc);

    // when
    ResourceUtil.copyLongestLabelToName(resource);

    // then
    assertThat(resource.getDoc().has(NAME.getValue())).isTrue();
    var nameValue = resource.getDoc().get(NAME.getValue()).get(0).asString();
    assertThat(nameValue).isIn("First", "Other")
      .hasSize(5);
  }

}
