package me.Tonus_.hatCosmetics.config.mapper;


public interface ITypeMapperRegistry {
    <T> TypeMapper<T> getMapper(Class<T> desiredType);
}
