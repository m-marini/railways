/*
 *
 * Copyright (c) 2021 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.yaml.schema;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.getValue;
import static org.mmarini.Utils.iterable;

/**
 *
 */
public interface Validator extends Function<Locator, Consumer<JsonNode>> {
    Validator OBJECT = locator -> root -> {
        JsonNode node = locator.getNode(root);
        assertFor(node.isObject(), locator, "must be an object (%s)", node.getNodeType());
    };
    Validator STRING = locator -> root -> {
        JsonNode node = locator.getNode(root);
        assertFor(node.isTextual(), locator, "must be a string (%s)", node.getNodeType());
    };
    Validator ARRAY = locator -> root -> {
        JsonNode node = locator.getNode(root);
        assertFor(node.isArray(), locator, "must be an array (%s)", node.getNodeType());
    };
    Validator INT = locator -> root -> {
        JsonNode node = locator.getNode(root);
        assertFor(node.isInt(), locator, "must be an integer (%s)", node.getNodeType());
    };
    Validator NUMBER = locator -> root -> {
        JsonNode node = locator.getNode(root);
        assertFor(node.isNumber(), locator, "must be a number (%s)", node.getNodeType());
    };
    Validator BOOLEAN = locator -> root -> {
        JsonNode node = locator.getNode(root);
        assertFor(node.isBoolean(), locator, "must be boolean (%s)", node.getNodeType());
    };

    Validator POSITIVE_NUMBER = number(exclusiveMinimum(0d));
    Validator NEGATIVE_NUMBER = number(exclusiveMaximum(0d));
    Validator NON_POSITIVE_NUMBER = number(maximum(0d));
    Validator NON_NEGATIVE_NUMBER = number(minimum(0d));
    Validator POSITIVE_INT = integer(exclusiveMinimum(0));
    Validator NEGATIVE_INT = integer(exclusiveMaximum(0));
    Validator NON_POSITIVE_INT = integer(maximum(0));
    Validator NON_NEGATIVE_INT = integer(minimum(0));

    /**
     * Returns the validator that applies a validator to all the properties of an object node
     *
     * @param validator the validator
     */
    static Validator additionalProperties(Validator validator) {
        requireNonNull(validator);
        return locator -> root -> locator.properties(root)
                .map(validator)
                .forEach(c -> c.accept(root));
    }

    /**
     * Returns a validator that applies on all element of a list of validator
     *
     * @param validators the validators
     */
    static Validator allOf(Collection<Validator> validators) {
        requireNonNull(validators);
        return locator -> root -> {
            for (Validator validator : validators) {
                validator.apply(locator).accept(root);
            }
        };
    }

    /**
     * Returns a validator that applies on all element of a list of validator
     *
     * @param validators the validators
     */
    static Validator allOf(Validator... validators) {
        requireNonNull(validators);
        return locator -> root -> {
            for (Validator validator : validators) {
                validator.apply(locator).accept(root);
            }
        };
    }

    /**
     *
     */
    static Validator array() {
        return ARRAY;
    }

    static Validator array(Validator... validators) {
        requireNonNull(validators);
        List<Validator> vals = new ArrayList<>();
        vals.add(array());
        vals.addAll(Arrays.asList(validators));
        return allOf(vals);
    }

    /**
     * Returns the array type
     *
     * @param items the items type
     */
    static Validator arrayItems(Validator items) {
        requireNonNull(items);
        return array(items(items));
    }

    /**
     * Returns the array type
     *
     * @param prefixItems the prefix items types
     */
    static Validator arrayPrefixItems(List<Validator> prefixItems) {
        requireNonNull(prefixItems);
        return array(prefixItems(prefixItems));
    }

    /**
     * Returns the array type
     *
     * @param prefixItems the prefix items types
     * @param items       the items type
     */
    static Validator arrayPrefixItemsAndItems(List<Validator> prefixItems, Validator items) {
        requireNonNull(prefixItems);
        requireNonNull(items);
        return array(itemsAndPrefixItems(items, prefixItems));
    }

    /**
     * @param valid   true for assertion
     * @param pointer location in the document
     * @param pattern message pattern
     * @param args    message arguments
     */
    static void assertFor(boolean valid, JsonPointer pointer, String pattern, Object... args) {
        if (!valid) {
            throw new IllegalArgumentException(pointer + " " + format(pattern, args));
        }
    }

    /**
     * @param valid   true for assertion
     * @param locator location in the document
     * @param pattern message pattern
     * @param args    message arguments
     */
    static void assertFor(boolean valid, Locator locator, String pattern, Object... args) {
        assertFor(valid, locator.pointer, pattern, args);
    }

    static Validator booleanValue() {
        return BOOLEAN;
    }

    /**
     * @param validator the validator provider
     */
    static Validator deferred(BiFunction<JsonNode, Locator, Validator> validator) {
        return locator -> root -> validator.apply(root, locator).apply(locator).accept(root);
    }

    /**
     * @param maximum the exclusive maximum value
     */
    static Validator exclusiveMaximum(int maximum) {
        return locator -> root -> {
            int value = locator.getNode(root).asInt(0);
            assertFor(value < maximum, locator, "must be < %s (%s)", maximum, value);
        };
    }

    /**
     * @param maximum the exclusive maximum value
     */
    static Validator exclusiveMaximum(double maximum) {
        return locator -> root -> {
            double value = locator.getNode(root).asDouble(0);
            assertFor(value < maximum, locator, "must be < %s (%s)", maximum, value);
        };
    }

    /**
     * @param minimum the exclusive minimum value
     */
    static Validator exclusiveMinimum(double minimum) {
        return locator -> root -> {
            double value = locator.getNode(root).asDouble(0);
            assertFor(value > minimum, locator, "must be > %s (%s)", minimum, value);
        };
    }

    /**
     * @param minimum the exclusive minimum value
     */
    static Validator exclusiveMinimum(int minimum) {
        return locator -> root -> {
            int value = locator.getNode(root).asInt(0);
            assertFor(value > minimum, locator, "must be > %s (%s)", minimum, value);
        };
    }

    /**
     *
     */
    static Validator integer() {
        return INT;
    }

    static Validator integer(Validator... validators) {
        List<Validator> vals = new ArrayList<>();
        vals.add(integer());
        vals.addAll(Arrays.asList(validators));
        return allOf(vals);
    }

    /**
     * Returns the validator that applies a validator to all the elements of a node array
     *
     * @param items the validator
     */
    static Validator items(Validator items) {
        requireNonNull(items);
        return locator -> root -> locator.elements(root)
                .map(items)
                .forEach(c -> c.accept(root));
    }

    /**
     * @param items       the default items validator
     * @param prefixItems the prefix items validators
     */
    static Validator itemsAndPrefixItems(Validator items, Validator... prefixItems) {
        return itemsAndPrefixItems(items, Arrays.asList(prefixItems));
    }

    /**
     * @param items       the default items validator
     * @param prefixItems the prefix items validators
     */
    static Validator itemsAndPrefixItems(Validator items, List<Validator> prefixItems) {
        requireNonNull(items);
        requireNonNull(prefixItems);
        int noPrefix = prefixItems.size();
        return locator -> root -> {
            JsonNode node = locator.getNode(root);
            int noNodes = node.size();
            int noPrefixNodes = min(noNodes, noPrefix);

            for (int i = 0; i < noPrefixNodes; i++) {
                prefixItems.get(i)
                        .apply(locator.path(String.valueOf(i)))
                        .accept(root);
            }
            for (int i = noPrefixNodes; i < noNodes; i++) {
                items.apply(locator.path(String.valueOf(i)))
                        .accept(root);
            }
        };
    }

    /**
     * Returns the validator for string
     *
     * @param size the minimum size
     */
    static Validator maxItems(int size) {
        return locator -> root -> {
            int currentSize = locator.getNode(root).size();
            assertFor(currentSize <= size, locator, "must have at most %s items (%s)", size, currentSize);
        };
    }

    /**
     * @param length the maximum string length
     */
    static Validator maxLength(int length) {
        return locator -> root -> {
            int currentSize = locator.getNode(root).asText("").length();
            assertFor(currentSize <= length, locator, "must have length <= %s (%s)", length, currentSize);
        };
    }

    /**
     * @param size the maximum number of items
     */
    static Validator maxProperties(int size) {
        return locator -> root -> {
            int n = locator.getNode(root).size();
            assertFor(n <= size, locator, "must have at most %s properties (%s)", size, n);
        };
    }

    /**
     * @param maximum the maximum value
     */
    static Validator maximum(int maximum) {
        return locator -> root -> {
            int value = locator.getNode(root).asInt(0);
            assertFor(value <= maximum, locator, "must be <= %s (%s)", maximum, value);
        };
    }

    /**
     * @param maximum the maximum value
     */
    static Validator maximum(double maximum) {
        return locator -> root -> {
            double value = locator.getNode(root).asDouble(0);
            assertFor(value <= maximum, locator, "must be <= %s (%s)", maximum, value);
        };
    }

    /**
     * Returns the validator for string
     *
     * @param size the minimum size
     */
    static Validator minItems(int size) {
        return locator -> root -> {
            int currentSize = locator.getNode(root).size();
            assertFor(currentSize >= size, locator, "must have at least %s items (%s)", size, currentSize);
        };
    }

    /**
     * @param length the minimum string length
     */
    static Validator minLength(int length) {
        return locator -> root -> {
            int currentSize = locator.getNode(root).asText("").length();
            assertFor(currentSize >= length, locator, "must have length >= %s (%s)", length, currentSize);
        };
    }

    /**
     * @param size the minimum number of items
     */
    static Validator minProperties(int size) {
        return locator -> root -> {
            int n = locator.getNode(root).size();
            assertFor(n >= size, locator, "must have at least %s properties (%s)", size, n);
        };
    }

    /**
     * @param minimum the minimum value
     */
    static Validator minimum(int minimum) {
        return locator -> root -> {
            int value = locator.getNode(root).asInt(0);
            assertFor(value >= minimum, locator, "must be >= %s (%s)", minimum, value);
        };
    }

    /**
     * @param minimum the minimum value
     */
    static Validator minimum(double minimum) {
        return locator -> root -> {
            double value = locator.getNode(root).asDouble(0);
            assertFor(value >= minimum, locator, "must be >= %s (%s)", minimum, value);
        };
    }

    /**
     *
     */
    static Validator negativeInteger() {
        return NEGATIVE_INT;
    }

    /**
     *
     */
    static Validator negativeNumber() {
        return NEGATIVE_NUMBER;
    }

    /**
     *
     */
    static Validator nonNegativeInteger() {
        return NON_NEGATIVE_INT;
    }

    /**
     *
     */
    static Validator nonNegativeNumber() {
        return NON_NEGATIVE_NUMBER;
    }

    /**
     *
     */
    static Validator nonPositiveInteger() {
        return NON_POSITIVE_INT;
    }

    /**
     *
     */
    static Validator nonPositiveNumber() {
        return NON_POSITIVE_NUMBER;
    }

    /**
     *
     */
    static Validator number() {
        return NUMBER;
    }

    static Validator number(Validator... validators) {
        List<Validator> vals = new ArrayList<>();
        vals.add(number());
        vals.addAll(Arrays.asList(validators));
        return allOf(vals);
    }

    /**
     *
     */
    static Validator object() {
        return OBJECT;
    }

    /**
     * @param validators the validator list
     */
    static Validator object(Validator... validators) {
        requireNonNull(validators);
        List<Validator> vals = new ArrayList<>();
        vals.add(object());
        vals.addAll(Arrays.asList(validators));
        return allOf(vals);
    }

    /**
     * @param additionalProperties the additional properties validator
     */
    static Validator objectAdditionalProperties(Validator additionalProperties) {
        requireNonNull(additionalProperties);
        return object(additionalProperties(additionalProperties));
    }

    /**
     * Returns the object schema
     *
     * @param properties the properties schemas
     */
    static Validator objectProperties(Map<String, Validator> properties) {
        return objectPropertiesRequired(properties, List.of());
    }

    /**
     * Returns the object schema
     *
     * @param properties the properties schemas
     * @param required   the required properties
     */
    static Validator objectPropertiesRequired(Map<String, Validator> properties, List<String> required) {
        requireNonNull(properties);
        requireNonNull(required);
        return object(properties(properties, required));
    }

    /**
     * Returns the object schema
     *
     * @param properties           the properties schemas
     * @param required             the required properties
     * @param additionalProperties the additional properties schema
     */
    static Validator objectPropertiesRequiredAdditionalProperties(Map<String, Validator> properties, List<String> required, Validator additionalProperties) {
        requireNonNull(properties);
        requireNonNull(required);
        requireNonNull(additionalProperties);
        return object(properties(properties, required, additionalProperties));
    }

    /**
     * @param pattern the regex pattern
     */
    static Validator pattern(String pattern) {
        return locator -> root -> {
            String value = locator.getNode(root).asText("");
            assertFor(value.matches(pattern), locator, "must match pattern \"%s\" (%s)", pattern, value);
        };
    }

    /**
     *
     */
    static Validator positiveInteger() {
        return POSITIVE_INT;
    }

    /**
     *
     */
    static Validator positiveNumber() {
        return POSITIVE_NUMBER;
    }

    /**
     * @param prefixItems the prefix items
     */
    static Validator prefixItems(Validator... prefixItems) {
        return locator -> root -> {
            JsonNode node = locator.getNode(root);
            int n = min(node.size(), prefixItems.length);
            for (int i = 0; i < n; i++) {
                prefixItems[i]
                        .apply(locator.path(String.valueOf(i)))
                        .accept(root);
            }
        };
    }

    /**
     * @param prefixItems the prefix items
     */
    static Validator prefixItems(List<Validator> prefixItems) {
        return locator -> root -> {
            JsonNode node = locator.getNode(root);
            int n = min(node.size(), prefixItems.size());
            for (int i = 0; i < n; i++) {
                prefixItems.get(i)
                        .apply(locator.path(String.valueOf(i)))
                        .accept(root);
            }
        };
    }

    /**
     * @param properties           the properties schemas
     * @param required             the required properties
     * @param additionalProperties the additional properties schema
     */
    static Validator properties(Map<String, Validator> properties, Validator additionalProperties, String...
            required) {
        return properties(properties, Arrays.asList(required), additionalProperties);
    }

    /**
     * @param properties           the properties schemas
     * @param required             the required properties
     * @param additionalProperties the additional properties schema
     */

    static Validator properties(Map<String, Validator> properties, List<String> required, Validator
            additionalProperties) {
        requireNonNull(properties);
        requireNonNull(required);
        requireNonNull(additionalProperties);
        return locator -> root -> {
            JsonNode node = locator.getNode(root);
            // Validate required properties
            for (String name : required) {
                Locator child = locator.path(name);
                assertFor(node.has(name), child, "is missing");
                getValue(properties, name)
                        .orElse(additionalProperties)
                        .apply(child)
                        .accept(root);
            }
            // Validate optional properties
            for (String name : iterable(node.fieldNames())) {
                if (!required.contains(name)) {
                    getValue(properties, name)
                            .orElse(additionalProperties)
                            .apply(locator.path(name))
                            .accept(root);
                }
            }
        };
    }

    /**
     * @param properties the properties schemas
     * @param required   the required properties
     */
    static Validator properties(Map<String, Validator> properties, String... required) {
        requireNonNull(properties);
        requireNonNull(required);
        return properties(properties, Arrays.asList(required));
    }

    /**
     * @param properties the properties schemas
     * @param required   the required properties
     */
    static Validator properties(Map<String, Validator> properties, List<String> required) {
        requireNonNull(properties);
        requireNonNull(required);
        return locator -> root -> {
            JsonNode node = locator.getNode(root);
            // Validate required properties
            for (String name : required) {
                Locator child = locator.path(name);
                assertFor(node.has(name), child, "is missing");
                Validator schema = properties.get(name);
                if (schema != null) {
                    schema.apply(child).accept(root);
                }
            }
            // Validate optional properties
            for (String name : iterable(node.fieldNames())) {
                if (!required.contains(name)) {
                    Locator child = locator.path(name);
                    Validator schema = properties.get(name);
                    if (schema != null) {
                        schema.apply(child).accept(root);
                    }
                }
            }
        };
    }

    /**
     * Returns the validator of property
     *
     * @param name      the property name
     * @param validator the property validator
     */
    static Validator property(String name, Validator validator) {
        return locator -> validator.apply(locator.path(name));
    }

    /**
     * @param validators the validator list
     */
    static Validator string(Validator... validators) {
        List<Validator> vals = new ArrayList<>();
        vals.add(string());
        vals.addAll(Arrays.asList(validators));
        return allOf(vals);
    }

    /**
     *
     */
    static Validator string() {
        return STRING;
    }

    /**
     * Returns the validator for string
     *
     * @param values the accepted values
     */
    static Validator values(Collection<String> values) {
        return locator -> root -> {
            String value = locator.getNode(root).asText("");
            assertFor(values.contains(value), locator, "must match a value in %s (%s)", values, value);
        };
    }

    /**
     * Returns the validator for string
     *
     * @param values the accepted values
     */
    static Validator values(String... values) {
        return values(Arrays.asList(values));
    }

}
