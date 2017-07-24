package smartthings.ratpack.protobuf

import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.http.client.RequestSpec
import smartthings.ratpack.protobuf.WidgetProtos.Widget
import spock.lang.Specification

class ProtobufModuleSpec extends Specification {
    def "Renders and parsers are registered properly"() {
        given:
        String id = UUID.randomUUID().toString()
        Widget widget = Widget.newBuilder()
                .setId(id)
                .setName("ST Protobuf")
                .build()

        expect:
        GroovyEmbeddedApp.ratpack {
            bindings {
                moduleConfig(ProtobufModule, new ProtobufModule.Config(cache: new CacheConfig()))
            }
            handlers {
                post {
                    parse(Widget).then {
                        render it
                    }
                }
            }
        }.test {
            requestSpec { RequestSpec requestSpec ->
                requestSpec.headers.add("Accept", MediaType.PROTOBUF.value)
                requestSpec.body.type(MediaType.PROTOBUF.value)
                requestSpec.body.bytes(widget.toByteArray())
            }
            def response = post()

            assert response.status.code == 200
            Widget result = Widget.parseFrom(response.body.getBytes())
            assert result.name == "ST Protobuf"
            assert result.id == id
        }
    }
}
