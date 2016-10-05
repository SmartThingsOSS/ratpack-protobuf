package smartthings.ratpack.protobuf.renderers

import com.google.protobuf.Message
import com.google.protobuf.StringValue
import com.google.protobuf.util.JsonFormat
import ratpack.handling.Context
import ratpack.http.Headers
import ratpack.http.Request
import ratpack.http.Response
import ratpack.test.handling.RequestFixture
import smartthings.ratpack.protobuf.WidgetProtos
import spock.lang.Specification
import spock.lang.Unroll

import static smartthings.ratpack.protobuf.ContentType.*

class ProtobufRendererSpec extends Specification {

    ProtobufRenderer renderer
    WidgetProtos.Widget widget
    RequestFixture requestFixture
    Context context
    Request request
    Headers headers
    Response response

    def setup() {
        renderer = new ProtobufRenderer();
        widget = WidgetProtos.Widget.newBuilder().setId("superid").setName("superwidget").build()
        requestFixture = RequestFixture.requestFixture()
        requestFixture.registry { r ->
            r.add(ProtobufRenderer, new ProtobufRenderer())
        }
        context = Mock()
        request = Mock()
        headers = Mock()
        response = Mock()
    }

    @Unroll
    def 'it should render json for contentType=#desc'() {
        given:
        String json = JsonFormat.printer().print(widget)
        Message message = StringValue.newBuilder().setValue(json).build()

        when:
        renderer.render(context, message)

        then:
        1 * context.getRequest() >> request
        1 * request.getHeaders() >> headers
        1 * headers.get("Accept") >> inputContentType
        1 * context.getResponse() >> response
        1 * response.send(*_) >> { CharSequence contentType, String body ->
            assert contentType == expectedContentType
            String simpleJson = body.replaceAll(/\\n\s?/, "").replaceAll("\\\\", "").replaceAll(/\s"/, '"')
            assert '''"{"id":"superid","name":"superwidget"}"''' == simpleJson
        }

        0 * _

        where:
        inputContentType    | expectedContentType | desc
        JSON.getValue()     | JSON.getValue()     | JSON.getValue()
        WILDCARD.getValue() | JSON.getValue()     | JSON.getValue()
        ""                  | JSON.getValue()     | "empty string"
        null                | JSON.getValue()     | "null"
    }

    def 'it should render protobuf'() {
        given:
        String expectedContentType = PROTOBUF.getValue()

        when:
        renderer.render(context, widget)

        then:
        1 * context.getRequest() >> request
        1 * request.getHeaders() >> headers
        1 * headers.get("Accept") >> expectedContentType
        1 * context.getResponse() >> response
        1 * response.send(*_) >> { CharSequence contentType, byte[] bytes ->
            assert contentType == expectedContentType
            assert bytes == widget.toByteArray()
        }

        0 * _
    }

    def 'it should throw 406 error otherwise'() {
        given:
        String expectedContentType = "application/marmalade"

        when:
        renderer.render(context, widget)

        then:
        1 * context.getRequest() >> request
        1 * request.getHeaders() >> headers
        1 * headers.get("Accept") >> expectedContentType
        2 * context.getResponse() >> response
        1 * response.status(406)
        1 * response.send('application/json', '{"type":"NotAcceptable","message":"Unsupported content type [application/marmalade]. Supported types are [application/json, application/x-protobuf]"}')

        0 * _
    }
}
