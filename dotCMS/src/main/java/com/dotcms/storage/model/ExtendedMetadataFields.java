package com.dotcms.storage.model;

import com.dotcms.util.CollectionsUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates a collection with keys generated by Tika.
 * It is used to translate keys from Tika 2.0 to the old ones (list with possible values).
 */
public enum ExtendedMetadataFields {

    DC_CREATOR("dcCreator", CollectionsUtils.list("author, metaAuthor")),
    META_LAST_AUTHOR("metaLastAuthor", CollectionsUtils.list("lastAuthor")),
    DC_TITLE("dcTitle", CollectionsUtils.list("title")),
    DC_TERMS_CREATED("dctermsCreated", CollectionsUtils.list("date", "creationDate")),
    DC_TERMS_MODIFIED("dctermsModified", CollectionsUtils.list("lastModified", "modified")),

    META_SAVE_DATE("metaSaveDate", CollectionsUtils.list("lastSaveDate")),
    EXTENDED_PROPERTIES_APPLICATION("extendedPropertiesApplication", CollectionsUtils.list("applicationName")),
    META_CHARACTER_COUNT("metaCharacterCount", CollectionsUtils.list("characterCount")),
    EXTENDED_PROPERTIES_COMPANY("extendedPropertiesCompany", CollectionsUtils.list("company")),
    EXTENDED_PROPERTIES_TOTAL_TIME("extendedPropertiesTotalTime", CollectionsUtils.list("editTime")),
    META_KEYWORD("metaKeyword", CollectionsUtils.list("keywords", "dcSubject")),
    META_PAGE_COUNT("metaPageCount", CollectionsUtils.list("pageCount")),
    REVISION_NUMBER("cpRevision", CollectionsUtils.list("revisionNumber")),
    DC_SUBJECT("dcSubject", CollectionsUtils.list("subject", "cpSubject", "metaKeyword", "keywords")),
    EXTENDED_TEMPLATE("extendedPropertiesTemplate", CollectionsUtils.list("template")),
    WORD_COUNT("metaWordCount", CollectionsUtils.list("wordCount")),
    DC_IDENTIFIER("dcIdentifier", CollectionsUtils.list("identifier")),
    DC_PUBLISHER("dcPublisher", CollectionsUtils.list("publisher"));

    private final String key;
    private final List<String> possibleValues;

    ExtendedMetadataFields(String key, List<String> possibleValues) {
        this.key = key;
        this.possibleValues = possibleValues;
    }

    public String key() {
        return key;
    }

    public List<String> possibleValues() {
        return possibleValues;
    }

    public static Map<String, List<String>> keyMap() {
        return Stream.of(values()).collect(
                Collectors.toMap(ExtendedMetadataFields::key, ExtendedMetadataFields::possibleValues));
    }
}
