package smartthings.ratpack.protobuf;

/**
 * Content types supported by smartthings protocol buffer library.
 */
public enum ContentType {

    JSON("application/json"),
    PROTOBUF("application/x-protobuf"),
    WILDCARD("*/*");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
