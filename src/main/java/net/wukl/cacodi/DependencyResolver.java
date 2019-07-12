package net.wukl.cacodi;

/**
 * The general interface of a dependency resolver.
 *
 * All resolver variants implement this set of methods and maybe more, depending on their
 * capabilities.
 */
public interface DependencyResolver {
    /**
     * A global, non-replaceable resolver instance.
     */
    DefaultDependencyResolver GLOBAL_INSTANCE = buildInstance();

    /**
     * Builds an instance of the default resolver.
     *
     * @return the default resolver, distinct from the global instance.
     */
    static DefaultDependencyResolver buildInstance() {
        return new DefaultDependencyResolver();
    }

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
}
