package net.wukl.cacodi;

/**
 * A dependency resolver that can call factories for classes that are requested.
 */
public interface FactoryInvokingDependencyResolver extends BasicDependencyResolver {
    /**
     * Registers a factory for a service.
     *
     * The factory must return a type implementing the interface, otherwise a compile-time error is
     * generated.
     *
     * @param iface   the interface the instance implements
     * @param factory the factory
     * @param <S>     the type of the interface
     */
    <S> void addFactory(Class<S> iface, Factory<S> factory);

    /**
     * Registers a factory for the service.
     *
     * The factory class is instantiated by the resolver before the factory is registered.
     *
     * @param iface the interface the service implements
     * @param factoryClass the factory class to instantiate and register
     * @param <S> the type of the service interface
     */
    default <S> void addFactory(
            final Class<S> iface, final Class<? extends Factory<S>> factoryClass
    ) {
        this.addFactory(iface, this.get(factoryClass));
    }

    /**
     * Registers a factory for a service, but only if there was no other factory in place.
     *
     * The factory must return a type implementing the interface, otherwise a compile-time error is
     * generated.
     *
     * @param iface   the interface the instance implements
     * @param factory the factory
     * @param <S>     the type of the interface
     *
     * @return the factory currently in the resolver, after updating
     */
    <S> Factory<S> addDefaultFactory(Class<S> iface, Factory<S> factory);

    /**
     * Registers a factory for the service, but only if there was no other factory in place.
     *
     * The factory class is instantiated by the resolver before the factory is registered and
     * only if no other factory was in place.
     *
     * @param iface the interface the service implements
     * @param factoryClass the factory class to instantiate and register
     * @param <S> the type of the service interface
     *
     * @return the factory currently in the resolver, after updating
     */
    <S> Factory<S> addDefaultFactory(Class<S> iface, Class<? extends Factory<S>> factoryClass);
}
