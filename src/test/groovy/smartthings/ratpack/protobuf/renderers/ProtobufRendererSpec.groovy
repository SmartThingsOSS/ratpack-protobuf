package smartthings.ratpack.protobuf.renderers

import io.netty.handler.codec.http.HttpHeaderNames
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.guice.Guice
import ratpack.http.HttpMethod
import ratpack.http.client.ReceivedResponse
import ratpack.test.embed.EmbeddedApp
import smartthings.ratpack.protobuf.ProtobufModule
import smartthings.ratpack.protobuf.WidgetProtos
import smartthings.ratpack.protobuf.parsers.ProtobufParser
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

class ProtobufRendererSpec extends Specification {

    @AutoCleanup
    EmbeddedApp app1 = GroovyEmbeddedApp.of({ spec ->
        registry(Guice.registry { bindings ->
            bindings.bind(ProtobufParser.class)
            bindings.bind(ProtobufRenderer.class)
        })
        .handlers {
            get('widget', { ctx ->
                ctx.render(
                    WidgetProtos.Widget.newBuilder().setId("superid").setName("superwidget").build()
                )
            })
        }
    })

    @AutoCleanup
    EmbeddedApp app2 = GroovyEmbeddedApp.of({ spec ->
        registry(Guice.registry { bindings ->
            ProtobufModule.Config config = new ProtobufModule.Config()
            config.with {
                defaultRenderer = ProtobufModule.DefaultRenderer.PROTOBUF
            }
            bindings.bindInstance(ProtobufModule.Config.class, config)
            bindings.bind(ProtobufParser.class)
            bindings.bind(ProtobufRenderer.class)
        })
        .handlers {
            get('widget', { ctx ->
                ctx.render(
                        WidgetProtos.Widget.newBuilder().setId("superid").setName("superwidget").build()
                )
            })
        }
    })

    @AutoCleanup
    EmbeddedApp app3 = GroovyEmbeddedApp.of({ spec ->
        registry(Guice.registry { bindings ->
            ProtobufModule.Config config = new ProtobufModule.Config()
            config.with {
                defaultRenderer = ProtobufModule.DefaultRenderer.ERROR_406
            }
            bindings.bindInstance(ProtobufModule.Config.class, config)
            bindings.bind(ProtobufParser.class)
            bindings.bind(ProtobufRenderer.class)
        })
                .handlers {
            get('widget', { ctx ->
                ctx.render(
                        WidgetProtos.Widget.newBuilder().setId("superid").setName("superwidget").build()
                )
            })
        }
    })

    void setup() {
    }

    @Unroll
    void 'it should render for mediaType=#inputMediaType'() {
        when:
        ReceivedResponse response = app1.httpClient.request('widget',{ spec ->
            spec.headers({ headers ->
                headers.set(HttpHeaderNames.CONTENT_TYPE, inputMediaType)
                headers.set(HttpHeaderNames.ACCEPT, inputMediaType)
            })
            spec.method(HttpMethod.GET)
        })

        then:
        assert response.getStatusCode() == 200
        assert response.headers.get('Content-Type') == expectedMediaType
        assert Integer.parseInt(response.headers.get('Content-Length')) > 0
        0 * _

        where:
        inputMediaType                     | expectedMediaType
        'application/json'                 | 'application/json'
        'application/vnd.company+json'     | 'application/json'
        'application/x-protobuf'           | 'application/x-protobuf'
        'application/vnd.company+protobuf' | 'application/x-protobuf'
        'garbage'                          | 'application/json'
    }

    @Unroll
    void 'it should allow changes default render to proto [mediaType=#inputMediaType]'() {
        when:
        ReceivedResponse response = app2.httpClient.request('widget',{ spec ->
            spec.headers({ headers ->
                headers.set(HttpHeaderNames.CONTENT_TYPE, inputMediaType)
                headers.set(HttpHeaderNames.ACCEPT, inputMediaType)
            })
            spec.method(HttpMethod.GET)
        })

        then:
        assert response.getStatusCode() == 200
        assert response.headers.get('Content-Type') == expectedMediaType
        assert Integer.parseInt(response.headers.get('Content-Length')) > 0
        0 * _

        where:
        inputMediaType                     | expectedMediaType
        'application/json'                 | 'application/json'
        'application/vnd.company+json'     | 'application/json'
        'application/x-protobuf'           | 'application/x-protobuf'
        'application/vnd.company+protobuf' | 'application/x-protobuf'
        'garbage'                          | 'application/x-protobuf'
    }

    void 'it should all default render to be 406 error'() {
        when:
        ReceivedResponse response = app3.httpClient.request('widget',{ spec ->
            spec.headers({ headers ->
                headers.set(HttpHeaderNames.CONTENT_TYPE, 'garbage')
                headers.set(HttpHeaderNames.ACCEPT, 'garbage')
            })
            spec.method(HttpMethod.GET)
        })

        then:
        assert response.getStatusCode() == 406
        0 * _
    }
}
