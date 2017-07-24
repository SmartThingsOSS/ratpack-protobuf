package smartthings.ratpack.protobuf.utils;

import smartthings.ratpack.protobuf.MediaType;

/**
 * Simple utility for working with Protocol buffers.
 */
public class ProtoUtils {

    public static boolean isProtobuf(ratpack.http.MediaType mediaType) {
        if (mediaType == null || mediaType.getType() == null) {
            return false;
        }
        String type = mediaType.getType();
        return (type.equals(MediaType.PROTOBUF.getValue()) || type.endsWith("+protobuf"));
    }
}
