package jsonsorter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonSorter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    private static final Configuration JSON_PATH = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider(OBJECT_MAPPER))
            .mappingProvider(new JacksonMappingProvider(OBJECT_MAPPER))
            .build();

    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
        JsonNode input = OBJECT_MAPPER.readTree(inputStream);

        Set<Rule> rules = new RuleSuggester().suggestRules(input);

        System.out.println("Rules:");
        rules.forEach(System.out::println);

        rules.forEach(rule -> processRule(input, rule));

        // Convert to generic Map so properties will be sorted when serializing
        Object obj = OBJECT_MAPPER.treeToValue(input, Object.class);
        OBJECT_MAPPER.writeValue(outputStream, obj);
    }

    private static void processRule(JsonNode json, Rule rule) {
        ArrayNode jsonArrays = rule.getPath().read(json, JSON_PATH.addOptions(Option.ALWAYS_RETURN_LIST));

        for (JsonNode jsonArray : jsonArrays) {
            sortArray(rule, (ArrayNode) jsonArray);
        }
    }

    private static void sortArray(Rule rule, ArrayNode jsonArray) {
        List<JsonNode> elements = new ArrayList<>(jsonArray.size());
        jsonArray.elements().forEachRemaining(elements::add);

        elements.sort(Comparator.comparing(element -> rule.getSortBy().read(element, JSON_PATH).toString()));

        jsonArray.removeAll();
        jsonArray.addAll(elements);
    }
}
