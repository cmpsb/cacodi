package net.wukl.cacodi;

/**
 * The stock dependency resolver, supporting all features of the library.
 */
public interface DependencyResolver
        extends CachingDependencyResolver,
                FactoryInvokingDependencyResolver,
                InterfaceMappingDependencyResolver {
    /**
     * A global, non-replaceable resolver instance.
     */
    DependencyResolver GLOBAL_INSTANCE = buildInstance();

    /**
     * Builds an instance of the default resolver.
     *
     * @return the default resolver, distinct from the global instance.
     */
    static DependencyResolver buildInstance() {
        return new DefaultDependencyResolver();
    }
}
