package jsonsorter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;

public class RuleSuggester {

    private static final List<String> POSSIBLE_SORT_PROPERTIES = Arrays.asList("code", "type", "name", "id");

    public Set<Rule> suggestRules(JsonNode jsonNode) {
        System.out.println("Possible sort properties: " + POSSIBLE_SORT_PROPERTIES);

        return suggestRules(jsonNode, "$");
    }

    private Set<Rule> suggestRules(JsonNode jsonNode, String path) {
        // Use comparator to ensure only unique paths are added
        Set<Rule> rules = new TreeSet<>(Comparator.comparing(r -> r.getPath().getPath()));

        if (jsonNode.isArray()) {
            suggestRule((ArrayNode) jsonNode, path).ifPresent(rules::add);

            // Recurse into elements
            jsonNode.elements().forEachRemaining(element ->
                    rules.addAll(suggestRules(element, path + "[*]")));
        } else if (jsonNode.isObject()) {
            // Recurse into object
            jsonNode.fields().forEachRemaining(field ->
                    rules.addAll(suggestRules(field.getValue(), path + "." + field.getKey())));
        }

        return rules;
    }

    private Optional<Rule> suggestRule(ArrayNode jsonArray, String path) {
        Optional<String> property = suggestSortProperty(jsonArray);
        return property.map(propertyName -> new Rule(JsonPath.compile(path), JsonPath.compile("$." + propertyName)));
    }

    private Optional<String> suggestSortProperty(ArrayNode jsonArray) {
        // Determine the properties that are present in all elements of the array,
        // which could be used for sorting
        Set<String> commonPropertyNames = toStream(jsonArray.elements())
                .map(element -> toStream(element.fieldNames()).collect(Collectors.toSet()))
                .reduce(RuleSuggester::intersection)
                .orElse(Collections.emptySet());

        // Take the one with highest precedence
        return POSSIBLE_SORT_PROPERTIES.stream()
                .filter(commonPropertyNames::contains)
                .findFirst();
    }

    private static <T> Stream<T> toStream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    private static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        return a.stream()
                .filter(b::contains)
                .collect(Collectors.toSet());
    }
}
