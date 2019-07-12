package net.wukl.cacodi;

import java.util.function.Supplier;

/**
 * The general interface of a dependency resolver.
 *
 * All resolver variants implement this set of methods and maybe more, depending on their
 * capabilities.
 */
public interface IDependencyResolver {
    /**
     * Looks up a dependency by its type, possibly constructing a new instance of that type.
     *
     * Be careful when requesting abstract or concrete classes (i.e., not interfaces), because if
     * an implementing type is registered with {@link #implement}, then the instance is stored
     * <em>only</em> under the requested type, not the implementing type.
     *
     * @param type the dependency type
     * @param <T>  the dependency type
     * @return an instance of the dependency
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    <T> T get(Class<T> type);

    /**
     * Statically adds an instance to the resolver and set the type under which is should be found.
     *
     * The alias type must the instance type's superclass.
     *
     * @param iface the interface the instance implements
     * @param impl  the actual implementation
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    <S, T extends S> void add(Class<S> iface, T impl);

    /**
     * Statically adds an instance to the resolver and sets the type under which is should be found,
     * but only if there was no other implementor in place.
     *
     * The implementor must implement the interface, otherwise a compile-time error is generated.
     *
     * @param iface the interface the instance implements
     * @param impl  the actual implementation
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     *
     * @return the implementation currently in the resolver, after updating the instance
     */
    <S, T extends S> S addDefault(Class<S> iface, T impl);

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
        public T build(final DependencyResolver resolver) {
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
