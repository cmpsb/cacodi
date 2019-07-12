package net.wukl.cacodi;

import java.util.function.Function;

/**
 * The general interface of a dependency resolver.
 *
 * All resolver variants implement this set of methods and maybe more, depending on their
 * capabilities.
 */
public interface BasicDependencyResolver extends Function<Class<?>, Object> {
    /**
     * Looks up a dependency by its type, possibly constructing a new instance of that type.
     *
     * Be careful when requesting abstract or concrete classes (i.e., not interfaces), because if
     * an implementing type is registered with {@link InterfaceMappingDependencyResolver#implement},
     * then the instance is stored <em>only</em> under the requested type,
     * not the implementing type.
     *
     * @param type the dependency type
     * @param <T> the dependency type
     * @return an instance of the dependency
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    <T> T get(Class<T> type);

    @Override
    default Object apply(final Class<?> type) {
        return this.get(type);
    }
}
