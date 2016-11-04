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

        public CacheConfig getCache() {
            return cache;
        }

        public void setCache(CacheConfig cacheConfig) {
            this.cache = cacheConfig;
        }
    }
}
