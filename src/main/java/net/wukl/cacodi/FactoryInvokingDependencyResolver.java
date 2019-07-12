package net.wukl.cacodi;

import java.util.function.Supplier;

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
     * Registers a factory for a service.
     *
     * The supplier is wrapped in a {@link CapturedSupplier} that delegates to the supplier.
     *
     * @param iface the interface the service implements
     * @param factory the factory
     * @param <S> the type of the interface
     */
    default <S> void addFactory(final Class<S> iface, final Supplier<S> factory) {
        this.addFactory(iface, new CapturedSupplier<>(factory));
    }

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
     * Registers a factory for a service, but only if there was no other factory in place.
     *
     * The supplier is wrapped in a {@link CapturedSupplier} that delegates to the supplier.
     *
     * @param iface the interface the service implements
     * @param factory the factory
     * @param <S> the type of the interface
     *
     * @return the factory currently in the resolver, after updating
     */
    default <S> Factory<S> addDefaultFactory(final Class<S> iface, final Supplier<S> factory) {
        return this.addDefaultFactory(iface, new CapturedSupplier<>(factory));
    }

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


    /**
     * An adaptor that converts {@link Supplier}s into {@link Factory} instances.
     *
     * @param <T> the type of service the supplier generates
     */
    final class CapturedSupplier<T> implements Factory<T> {
        /**
         * The captured supplier.
         */
        private final Supplier<T> supplier;

        /**
         * Creates a new captured supplier.
         *
         * @param supplier the captured supplier
         */
        @Manual
        public CapturedSupplier(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        /**
         * Builds the service.
         *
         * @param resolver the dependency resolver invoking the factory
         * @return an instance of the service
         * @throws UnresolvableDependencyException if the dependency cannot be resolved
         */
        @Override
        public T build(final BasicDependencyResolver resolver) {
            return this.supplier.get();
        }

        /**
         * Returns the captured supplier instance.
         *
         * @return the supplier
         */
        public Supplier<T> getSupplier() {
            return this.supplier;
        }

        @Override
        public String toString() {
            return "Factory capture of supplier " + this.supplier;
        }
    }
}
