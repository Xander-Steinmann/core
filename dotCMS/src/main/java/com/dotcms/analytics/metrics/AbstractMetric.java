package com.dotcms.analytics.metrics;

import com.dotcms.experiments.business.result.Event;
import com.dotcms.util.DotPreconditions;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.liferay.util.StringPool;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

/**
 * A Metric represents something that wants to be measured in the system.
 * <p>
 * It comprises a name, a {@link MetricType} and a List of {@link Condition}s.
 * <p>
 * The {@link Condition}s are filters for the Metric.
 */

@Value.Style(typeImmutable="*", typeAbstract="Abstract*")
@Value.Immutable
@JsonSerialize(as = Metric.class)
@JsonDeserialize(as = Metric.class)
public interface AbstractMetric extends Serializable {
    String name();

    MetricType type();

    List<Condition> conditions();


    /**
     * Return true if all the {@link Condition} are valid for a specific {@link Event}
     *
     * @param event
     * @return
     */
    default boolean validateConditions(final Event event) {

        boolean isValid = true;

        for (final Condition condition : conditions()) {
            final Parameter parameter = type().getParameter(condition.parameter()).orElseThrow();

            if (parameter.validate()) {
                isValid = isValid && condition.isValid(parameter, event);

                if (!isValid) {
                    break;
                }
            }
        }

        return isValid;
    }
}
