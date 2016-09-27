package smartthings.ratpack.protobuf.parsers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import ratpack.handling.Context;
import ratpack.http.TypedData;
import ratpack.parse.NoOptParserSupport;
import ratpack.util.Types;
import smartthings.ratpack.protobuf.CacheConfig;
import smartthings.ratpack.protobuf.ProtobufModule;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static smartthings.ratpack.protobuf.ContentType.*;

/**
 * Smart Things parsing of protocol buffers.
 */
public class ProtobufParser extends NoOptParserSupport {

    private static final TypeToken<Message> PROTO3_TYPE_TOKEN = TypeToken.of(Message.class);

    private JsonFormat.Parser jsonParser;
    private CacheConfig cacheConfig;
    private Cache<Class<Message>, Method> newBuilderMethodCache;
    private Cache<Class<Message>, Method> parseFromMethodCache;

    @Inject
    public ProtobufParser(ProtobufModule.Config cacheConfig) {
        this.cacheConfig = cacheConfig.getCache();
        initializeCaches();
        jsonParser = JsonFormat.parser();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T parse(Context ctx, TypedData requestBody, TypeToken<T> type) throws Exception {
        if (!type.isSubtypeOf(PROTO3_TYPE_TOKEN)) {
            return null;
        }

        String contentType = getContentType(ctx.getRequest().getContentType().toString());

        if (PROTOBUF.getValue().equals(contentType)) {
            Method method = getParseMethod(type);
            return Types.cast(method.invoke(null, requestBody.getBytes()));
        } else if (JSON.getValue().equals(contentType)) {
            final Message.Builder builder = getMessageBuilder((Class<Message>) type.getRawType());
            jsonParser.merge(
                    requestBody.getText(),
                    builder
            );
            return Types.cast(builder.build());
        }

        return null;
    }

    private static String getContentType(String contentType) {
        if (Strings.isNullOrEmpty(contentType)) {
            return JSON.getValue();
        }
        return contentType;
    }

    private Message.Builder getMessageBuilder(Class<Message> type) throws Exception {
        Method method = getBuilderMethod(type);
        return (Message.Builder) method.invoke(type);
    }

    private Method getBuilderMethod(Class<Message> type) throws NoSuchMethodException {
        Method method = newBuilderMethodCache.getIfPresent(type);
        if (method == null) {
            method = type.getDeclaredMethod("newBuilder");
            newBuilderMethodCache.put(type, method);
        }
        return method;
    }

    @SuppressWarnings("unchecked")
    private <T> Method getParseMethod(TypeToken<T> type) throws Exception {
        Method method = parseFromMethodCache.getIfPresent(type.getRawType());
        if (method == null) {
            Class<Message> clazz = (Class<Message>) type.getRawType();
            method = clazz.getDeclaredMethod("parseFrom", byte[].class);
            parseFromMethodCache.put(clazz, method);
        }
        return method;
    }

    private void initializeCaches() {
        newBuilderMethodCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getMinutesTTL(), TimeUnit.MINUTES)
                .maximumSize(cacheConfig.getMaximumSize())
                .build();
        parseFromMethodCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getMinutesTTL(), TimeUnit.MINUTES)
                .maximumSize(cacheConfig.getMaximumSize())
                .build();
    }
}
