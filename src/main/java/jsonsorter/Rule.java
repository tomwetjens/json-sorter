package jsonsorter;

import com.jayway.jsonpath.JsonPath;
import lombok.Value;

@Value
class Rule {

    private JsonPath path;
    private JsonPath sortBy;

    @Override
    public String toString() {
        return normalize(path) + " sorted by " + normalize(sortBy);
    }

    private static String normalize(JsonPath path) {
        return path.getPath()
                .replaceAll("\\['", ".")
                .replaceAll("'\\]", "")
                .replaceAll("\\[\\*\\]", "[*]");
    }
}
