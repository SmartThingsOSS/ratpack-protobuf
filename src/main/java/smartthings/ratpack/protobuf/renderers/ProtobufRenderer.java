package smartthings.ratpack.protobuf.renderers;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import ratpack.handling.Context;
import ratpack.render.RendererSupport;
import org.apache.http.HttpStatus;

import static smartthings.ratpack.protobuf.ContentType.*;

/**
 * Smart Things rendering of protocol buffers.
 */
public class ProtobufRenderer extends RendererSupport<Message> {

    private JsonFormat.Printer printer;

    @Inject
    public ProtobufRenderer() {
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @Override
    public void render(Context ctx, Message message) throws Exception {
        final String contentType = ctx.getRequest().getHeaders().get("Accept");
        if (Strings.isNullOrEmpty(contentType) ||
                JSON.getValue().equals(contentType) ||
                WILDCARD.getValue().equals(contentType)) {
            ctx.getResponse().send(JSON.getValue(), printer.print(message));
        } else if (PROTOBUF.getValue().equals(contentType)) {
            ctx.getResponse().send(contentType, message.toByteArray());
        } else {
            render406(ctx, contentType);
        }
    }

    void render406(Context ctx, String accepts) throws Exception {
        String msg =
                "Unsupported content type [" + accepts + "]. Supported types are [" + JSON.getValue() + ", " +
                        PROTOBUF.getValue() + "]";
        ctx.getResponse().status(HttpStatus.SC_NOT_ACCEPTABLE);
        ctx.getResponse().send(
                JSON.getValue(),
                String.format("{'type':'NotAcceptable','message':'%s'}", msg).replaceAll("'", "\"")
        );
    }

}
