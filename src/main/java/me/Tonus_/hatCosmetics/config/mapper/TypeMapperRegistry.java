package me.Tonus_.hatCosmetics.config.mapper;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TypeMapperRegistry implements ITypeMapperRegistry {
    private final Map<Class<?>, TypeMapper<?>> mappers = new ConcurrentHashMap<>();

    public TypeMapperRegistry() {
        register(new LocaleMapper());
        register(new MaterialMapper());
        register(new StorageFormatMapper());
    }

    @SuppressWarnings("unchecked")
    public <T> TypeMapper<T> getMapper(Class<T> desiredType) {
        return (TypeMapper<T>) mappers.get(desiredType);
    }

    private <T> void register(TypeMapper<T> mapper) {
        var targetType = findTargetType(mapper);
        mappers.put(targetType, mapper);
    }

    private Class<?> findTargetType(@NotNull TypeMapper<?> mapper) {
        var ifaces = mapper.getClass().getGenericInterfaces();
        for (var iface : ifaces) {
            if (iface instanceof ParameterizedType pt && pt.getRawType().equals(TypeMapper.class)) {
                return (Class<?>) pt.getActualTypeArguments()[0];
            }
        }

        throw new IllegalArgumentException("Cannot determine target type for mapper: " + mapper.getClass());
    }
}
