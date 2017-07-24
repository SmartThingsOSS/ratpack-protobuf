package smartthings.ratpack.protobuf.renderers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.apache.http.HttpStatus;
import ratpack.handling.Context;
import ratpack.http.internal.DefaultMediaType;
import ratpack.render.RendererSupport;
import smartthings.ratpack.protobuf.MediaType;
import smartthings.ratpack.protobuf.ProtobufModule;
import smartthings.ratpack.protobuf.utils.ProtoUtils;

/**
 * Smart Things rendering of protocol buffers.
 */
@Singleton
public class ProtobufRenderer extends RendererSupport<Message> {

    private static final String JSON = ratpack.http.MediaType.APPLICATION_JSON;
    private static final String PROTOBUF = MediaType.PROTOBUF.getValue();

    private final JsonFormat.Printer printer;
    private final ProtobufModule.Config config;

    @Inject
    public ProtobufRenderer(ProtobufModule.Config config) {
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
        this.config = config;
    }

    @Override
    public void render(Context ctx, Message message) throws Exception {
        String accept = ctx.getRequest().getHeaders().get("Accept");
        ratpack.http.MediaType mediaType = DefaultMediaType.get(accept);
        if (mediaType.isJson()) {
            json(ctx, message);
        } else if (ProtoUtils.isProtobuf(mediaType)) {
            protobuf(ctx, message);
        } else {
            noMatch(ctx, message, accept);
        }

    }

    private void json(Context ctx, Message message) throws Exception {
        ctx.getResponse().send(ratpack.http.MediaType.APPLICATION_JSON, printer.print(message));
    }

    private void protobuf(Context ctx, Message message) throws Exception {
        ctx.getResponse().send(MediaType.PROTOBUF.getValue(), message.toByteArray());
    }

    private void render406(Context ctx, String accept) throws Exception {
        String msg = String.format(
            "Unsupported content type [%s]. Supported types are [%s, %s]", accept, JSON, PROTOBUF
        );
        String body = String.format(
            "{'type':'NotAcceptable','message':'%s'}", msg
        ).replaceAll("'", "\"");

        ctx.getResponse().status(HttpStatus.SC_NOT_ACCEPTABLE);
        ctx.getResponse().send(JSON, body);
    }

    private void noMatch(Context ctx, Message message, String accept) throws Exception {
        switch (config.getDefaultRenderer()) {
            case JSON:
                json(ctx, message);
                break;
            case PROTOBUF:
                protobuf(ctx, message);
                break;
            case ERROR_406:
                render406(ctx, accept);
                break;
            default:
                json(ctx, message);
        }
    }

}
