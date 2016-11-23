package smartthings.ratpack.protobuf.parsers

import com.github.benmanes.caffeine.cache.Cache
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.util.JsonFormat
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.parse.NoSuchParserException
import ratpack.test.handling.HandlingResult
import smartthings.ratpack.protobuf.CacheConfig
import smartthings.ratpack.protobuf.ContentType
import smartthings.ratpack.protobuf.ProtobufModule
import spock.lang.Specification
import ratpack.test.handling.RequestFixture
import smartthings.ratpack.protobuf.WidgetProtos.Widget

import java.lang.reflect.Method

class ProtobufParserSpec extends Specification {

    DataService service
    Widget widget
    RequestFixture requestFixture
    Cache caffeine
    Method someMethod

    def setup() {
        service = Mock()
        widget = Widget.newBuilder().setId("superid").setName("superwidget").build()
        caffeine = Mock()
        someMethod = String.getMethod("toString")
        setupRequestFixture()
    }

    def 'it should parse json and use cached methods if possible'() {
        given:
        String json = JsonFormat.printer().print(widget)

        when: "first request is issued"
        requestFixture.body(json, ContentType.JSON.getValue())
                .handle(new DataHandler())

        then: "cache miss, put method in cache"
        1 * service.process({ Widget result ->
            result.id == widget.id
            result.name == widget.name
        } as Widget)
        1 * caffeine.getIfPresent(_) >> null
        1 * caffeine.put(_, _)
        0 * _

        when: "a second request is issued"
        requestFixture.body(widget.toByteArray(), ContentType.JSON.getValue())
                .handle(new DataHandler())

        then: "get cached method"
        1 * caffeine.getIfPresent(_) >> someMethod
        0 * caffeine.put(_,_)
        0 * _
    }

    def 'it should parse empty content type as json'() {
        given:
        String json = JsonFormat.printer().print(widget)

        when:
        requestFixture.body(json, "").handle(new DataHandler())

        then:
        1 * service.process({ Widget result ->
            result.id == widget.id
            result.name == widget.name
        } as Widget)
        1 * caffeine.getIfPresent(_) >> null
        1 * caffeine.put(_, _)
        0 * _
    }

    def 'it should parse json content type with charset as json'() {
        given:
        String json = JsonFormat.printer().print(widget)

        when:
        requestFixture.body(json, 'application/json;charset=utf-8').handle(new DataHandler())

        then:
        1 * service.process({ Widget result ->
            result.id == widget.id
            result.name == widget.name
        } as Widget)
        1 * caffeine.getIfPresent(_) >> null
        1 * caffeine.put(_, _)
        0 * _
    }

    def 'it should parse protocol buffers and use cached methods if possible'() {
        when: "first request is issued"
        requestFixture.body(widget.toByteArray(), ContentType.PROTOBUF.getValue())
                .handle(new DataHandler())

        then: "cache miss, put method in cache"
        1 * service.process({ Widget result ->
            result.id == widget.id
            result.name == widget.name
        } as Widget)
        1 * caffeine.getIfPresent(_) >> null
        1 * caffeine.put(_, _)
        0 * _

        when: "a second request is issued"
        requestFixture.body(widget.toByteArray(), ContentType.PROTOBUF.getValue())
                .handle(new DataHandler())

        then: "get cached method"
        1 * caffeine.getIfPresent(_) >> someMethod
        0 * caffeine.put(_,_)
        0 * _
    }

    void 'it should throw an exception for unrecognized field in json'() {
        given:
        String json = '''{"somefield":"someval"}'''

        when:
        HandlingResult result = requestFixture.body(json, ContentType.JSON.getValue())
                .handle(new DataHandler())

        then:
        assert result
        assert result.exception(Exception).class == InvalidProtocolBufferException
        assert result.exception(Exception).message == 'Cannot find field: somefield in message smartthings.Widget'
    }

    void 'it should throw an exception for bad json'() {
        given:
        String json = 'badjson'

        when:
        HandlingResult result = requestFixture.body(json, ContentType.JSON.getValue())
                .handle(new DataHandler())

        then:
        assert result
        assert result.exception(Exception).class == InvalidProtocolBufferException
        assert result.exception(Exception).message == 'Expect message object but got: "badjson"'
    }

    def 'it should let ratpack handle unparsable type'() {
        when:
        String contentType = 'application/marmalade'
        HandlingResult result = requestFixture.body(widget.toByteArray(), contentType)
                .handle(new DataHandler())
        then:
        assert result.exception(Exception).class == NoSuchParserException
    }

    class DataHandler implements Handler {
        void handle(Context ctx) throws Exception {
            ctx.parse(Widget)
                    .map({ exe ->
                ctx.get(DataService).process(exe)
            })
                    .onError({ error ->
                ctx.error(error)
            })
                    .then({
                ctx.render('ok')
            })
        }
    }

    class DataService {
        void process(Widget widget) {

        }

        void process(String str) {

        }
    }

    private void setupRequestFixture() {
        requestFixture = RequestFixture.requestFixture()
        requestFixture.registry { r ->
            ProtobufParser parser = new ProtobufParser(new ProtobufModule.Config(
                    cache: new CacheConfig(minutesTTL: 60, maximumSize: 1000))
            )
            parser.newBuilderMethodCache = caffeine
            parser.parseFromMethodCache = caffeine
            r.add(ProtobufParser, parser)
            r.add(DataService, service)
        }
    }
}
