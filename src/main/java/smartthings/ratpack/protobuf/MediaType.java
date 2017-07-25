package smartthings.ratpack.protobuf;

/**
 * Content types supported by smartthings protocol buffer library.
 */
public enum MediaType {
    PROTOBUF("application/x-protobuf");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
