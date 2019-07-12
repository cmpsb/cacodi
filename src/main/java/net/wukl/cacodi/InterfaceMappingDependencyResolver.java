package net.wukl.cacodi;

/**
 * A dependency resolver that is interface-aware and can map them to registered, implementing
 * types.
 */
public interface InterfaceMappingDependencyResolver extends DependencyResolver {
    /**
     * Registers an interface implementation.
     *
     * The implementing type must be an instantiable type, i.e. a concrete class (not abstract!)
     * Furthermore, the implementing type must implement the interface, otherwise a compile-time
     * error is generated.
     *
     * @param iface the interface of the implementor
     * @param impl  the implementor class
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     *
     * @throws UninstantiableTypeException if the implementor type is not instantiable
     */
    <S, T extends S> void implement(Class<S> iface, Class<T> impl);

    /**
     * Registers an interface implementation, but only if there was no implementor in place.
     *
     * The implementing type must be an instantiable type, i.e. a concrete class (not abstract!)
     * Furthermore, the implementing type must implement the interface, otherwise a compile-time
     * error is generated.
     *
     * @param iface the interface of the implementor
     * @param impl  the implementor class
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     *
     * @return the implementor currently in the resolver, after updating the interface
     *
     * @throws UninstantiableTypeException if the implementor type is not instantiable
     */
    <S, T extends S> Class<?> implementDefault(Class<S> iface, Class<T> impl);
}
