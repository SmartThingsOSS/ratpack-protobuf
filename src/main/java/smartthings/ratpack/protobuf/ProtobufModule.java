package smartthings.ratpack.protobuf;

import ratpack.guice.ConfigurableModule;
import smartthings.ratpack.protobuf.parsers.ProtobufParser;
import smartthings.ratpack.protobuf.renderers.ProtobufRenderer;

/**
 * Guice bindings for SmartThings Protobuf Parsing/Rendering.
 */
public class ProtobufModule extends ConfigurableModule<ProtobufModule.Config>{

    @Override
    protected void configure() {
        bind(ProtobufParser.class);
        bind(ProtobufRenderer.class);
    }

    /**
     * Configuration POJO for ProtobufModule.
     */
    public static class Config {
        private CacheConfig cache;
        private DefaultRenderer defaultRenderer = DefaultRenderer.JSON;

        public CacheConfig getCache() {
            return cache;
        }

        public void setCache(CacheConfig cacheConfig) {
            this.cache = cacheConfig;
        }

        public DefaultRenderer getDefaultRenderer() {
            return defaultRenderer;
        }

        public void setDefaultRenderer(DefaultRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        public void setDefaultRenderer(String defaultRenderer) {
            try {
                this.defaultRenderer = DefaultRenderer.valueOf(defaultRenderer);
            } catch (IllegalArgumentException e) {
                this.defaultRenderer = DefaultRenderer.JSON;
            }
        }
    }

    /**
     * Supported renderer options for when no match is found based on Accept header.
     */
    public enum DefaultRenderer {
        JSON,
        PROTOBUF,
        ERROR_406
    }
}
