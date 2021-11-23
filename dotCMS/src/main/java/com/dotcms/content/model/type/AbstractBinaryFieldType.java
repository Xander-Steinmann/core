package com.dotcms.content.model.type;

import com.dotcms.content.model.FieldValue;
import com.dotcms.content.model.FieldValueBuilder;
import com.dotcms.content.model.annotation.HydrateWith;
import com.dotcms.content.model.annotation.ValueType;
import com.dotcms.content.model.annotation.Hydration;
import com.dotcms.content.model.hydration.MetadataDelegate;
import com.dotcms.content.model.hydration.ResourceLinkDelegate;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.Map;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * Binary-Field json representation
 */
@ValueType
@Immutable
@JsonDeserialize(as = BinaryFieldType.class)
@JsonTypeName(value = AbstractBinaryFieldType.TYPENAME)
public interface AbstractBinaryFieldType extends FieldValue<String> {

    String TYPENAME = "Binary";

    /**
     * {@inheritDoc}
     */
    @Override
    default String type() {
        return TYPENAME;
    };

    @JsonProperty("link")
    @Parameter
    String link();

    @JsonProperty("metadata")
    @Parameter
    Map<String, Serializable> metadata();

    @Hydration(properties = {
        @HydrateWith(delegate = ResourceLinkDelegate.class, propertyName = "link"),
        @HydrateWith(delegate = MetadataDelegate.class, propertyName = "metadata")
    })
    abstract class Builder implements FieldValueBuilder {

    }

}
