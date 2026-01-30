package org.folio.rdf4ld.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.test.MonographUtil.createGenreForm;
import static org.folio.rdf4ld.test.MonographUtil.createInstance;
import static org.folio.rdf4ld.test.MonographUtil.createWork;
import static org.folio.rdf4ld.test.TestUtil.mockLccnResource;
import static org.folio.rdf4ld.test.TestUtil.toJsonLdString;
import static org.folio.rdf4ld.test.TestUtil.validateOutgoingEdge;
import static org.folio.rdf4ld.test.TestUtil.validateResourceWithGivenEdges;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.Rdf4LdMapper;
import org.folio.rdf4ld.test.SpringTestConfig;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class WorkGenreMappingIT {

  private static final Map<PropertyDictionary, List<String>> EXPECTED_WORK_PROPERTIES = Map.of(
    LINK, List.of("http://test-tobe-changed.folio.com/resources/WORK_ID")
  );
  private static final String GENRE_FORM_LABEL = "Fiction";
  private static final String GENRE_FORM_LCCN = "gf2014026339";
  @Autowired
  private Rdf4LdMapper rdf4LdMapper;

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithGenreMock() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_genre_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "", work ->
      validateResourceWithGivenEdges(work, new ResourceEdge(work, mockLccnResource("gf2014026339"), GENRE))
    );
  }

  @Test
  void mapBibframe2RdfToLd_shouldReturnMappedInstanceWithWorkWithGenre() throws IOException {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/work_genre_no_lccn.json");
    var model = Rio.parse(input, "", RDFFormat.JSONLD);

    // when
    var result = rdf4LdMapper.mapBibframe2RdfToLd(model);

    // then
    assertThat(result).hasSize(1);
    var instance = result.iterator().next();
    assertThat(instance.getId()).isNotNull();
    assertThat(instance.getIncomingEdges()).isEmpty();
    assertThat(instance.getOutgoingEdges()).hasSize(1);
    var expectedFormProperties = Map.of(
      LABEL, List.of(GENRE_FORM_LABEL),
      NAME, List.of(GENRE_FORM_LABEL)
    );
    validateOutgoingEdge(instance, INSTANTIATES, Set.of(WORK, BOOKS), EXPECTED_WORK_PROPERTIES, "", work ->
      validateOutgoingEdge(work, GENRE, Set.of(FORM), expectedFormProperties, GENRE_FORM_LABEL, null)
    );
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithGenreWithNoLccn() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var genreForm = createGenreForm(GENRE_FORM_LCCN, false, GENRE_FORM_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, genreForm, GENRE));
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_genre_no_lccn.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString())
      .replaceAll("GENRE_FORM_ID", "_" + genreForm.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

  @Test
  void mapLdToBibframe2Rdf_shouldReturnMappedRdfInstanceWithWorkWithGenreWithLccn() throws IOException {
    // given
    var work = createWork(Map.of(), BOOKS);
    var genreForm = createGenreForm(GENRE_FORM_LCCN, true, GENRE_FORM_LABEL);
    work.addOutgoingEdge(new ResourceEdge(work, genreForm, GENRE));
    var instance = createInstance(null);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));
    var expected = new String(this.getClass().getResourceAsStream("/rdf/work_genre_lccn.json").readAllBytes())
      .replaceAll("INSTANCE_ID", instance.getId().toString())
      .replaceAll("WORK_ID", work.getId().toString());

    // when
    var model = rdf4LdMapper.mapLdToBibframe2Rdf(instance);

    // then
    var jsonLdString = toJsonLdString(model);
    assertThat(jsonLdString).isEqualTo(expected);
  }

}
