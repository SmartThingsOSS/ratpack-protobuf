package smartthings.ratpack.protobuf;

import ratpack.guice.ConfigurableModule;

/**
 * Guice bindings for Smart Things Protobuf Parsing/Rendering.
 */
public class ProtobufModule extends ConfigurableModule<ProtobufModule.Config>{

    @Override
    protected void configure() {
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
