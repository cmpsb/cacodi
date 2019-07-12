package net.wukl.cacodi;

/**
 * The stock dependency resolver, supporting all features of the library.
 */
public interface DependencyResolver
        extends CachingDependencyResolver,
                FactoryInvokingDependencyResolver,
                InterfaceMappingDependencyResolver {
}
