package net.wukl.cacodi;

/**
 * A dependency resolver that caches the instances it builds.
 */
public interface CachingDependencyResolver extends DependencyResolver {
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
}
