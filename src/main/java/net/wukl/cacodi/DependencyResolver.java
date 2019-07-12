package net.wukl.cacodi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dependency resolver.
 *
 * @author Luc Everse
 */
public class DependencyResolver implements IDependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencyResolver.class);
    /**
     * The current list of instances.
     */
    private final Map<Class, Object> instances;
    /**
     * A mapping between interfaces and their implementing classes.
     */
    private final Map<Class, Class> implementations;

    /**
     * The factories to consult.
     */
    private final Map<Class, Factory<?>> factories;

    /**
     * Creates a new dependency resolver.
     * This will also list the dependency resolver in itself.
     */
    public DependencyResolver() {
        this.instances = new HashMap<>();
        this.implementations = new HashMap<>();
        this.factories = new HashMap<>();

        this.preloadInstances();
    }

    /**
     * Creates a new dependency resolver as a clone of another.
     *
     * @param other the source resolver
     */
    public DependencyResolver(final DependencyResolver other) {
        this.instances = new HashMap<>(other.instances);
        this.implementations = new HashMap<>(other.implementations);
        this.factories = new HashMap<>(other.factories);

        this.preloadInstances();
    }

    /**
     * Pre-loads the resolver with useful dependencies.
     */
    private void preloadInstances() {
        this.instances.put(DependencyResolver.class, this);
        this.instances.put(ClassLoader.class, this.getClass().getClassLoader());
    }

    /**
     * Try to instantiate an object of a given type.
     *
     * @param type      the class to instantiate
     * @param <T>  the class to instantiate
     *
     * @return an instance of the given type
     *
     * @throws UnresolvableDependencyException if the type couldn't be instantiated
     */
    @SuppressWarnings("unchecked")
    private <T> T instantiate(final Class<T> type) {
        logger.debug("Instantiating a {}.", type.getCanonicalName());

        // Use the factory if it's available.
        final var factory = (Factory<T>) this.factories.get(type);
        if (factory != null) {
            try {
                final var obj = factory.build(this);

                this.populateFields(obj, type);

                logger.debug("Successfully instantiated a {}", type.getCanonicalName());
                return obj;
            } catch (final IllegalAccessException ex) {
                logger.debug(
                        "Can't instantiate the {} using its factory: field is not accessible",
                        type.getCanonicalName(), ex
                );
            }
        }

        final List<Constructor<?>> ctors = new ArrayList<>();
        try {
            ctors.add(type.getDeclaredConstructor());
        } catch (final NoSuchMethodException ex) {
            // Skip
        }
        ctors.addAll(Arrays.asList(type.getConstructors()));

        for (final Constructor<?> ctor : ctors) {
            if (ctor.isAnnotationPresent(Manual.class)) {
                continue;
            }

            try {
                final T obj = this.construct(ctor, type);

                this.populateFields(obj, type);

                logger.debug("Successfully instantiated a {}.", type.getCanonicalName());
                return obj;
            } catch (final InstantiationException ex) {
                logger.debug("Can't instantiate the {}: not an instantiable class",
                             type.getCanonicalName());
            } catch (final IllegalAccessException ex) {
                logger.debug("Can't instantiate the {}: class, constructor or field inaccessible",
                             type.getCanonicalName());
            } catch (final InvocationTargetException ex) {
                logger.debug("Can't instantiate the {}: exception in constructor: ",
                             type.getCanonicalName(), ex);
            } catch (final UnresolvableDependencyException ex) {
                // Recursive construction failed. Already logged, try the next
            }
        }

        logger.debug("Can't instantiate the {}: all constructors exhausted",
                     type.getCanonicalName());
        throw new UnresolvableDependencyException("All constructors exhausted");
    }

    /**
     * Try to construct the dependency using the given constructor.
     *
     * @param ctor      the constructor to try
     * @param type      the dependency's type
     * @param <T>       the dependency's type
     *
     * @return an instance of the dependency
     *
     * @throws IllegalAccessException    if the constructor is inaccessible, i.e. private
     * @throws InvocationTargetException if the constructor raised an exception
     * @throws InstantiationException    if the dependency is not an instantiable class
     */
    private <T> T construct(final Constructor<?> ctor, final Class<T> type)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Parameter[] parameters = ctor.getParameters();
        final int numParameters = parameters.length;
        final Object[] values = new Object[numParameters];

        for (int i = 0; i < numParameters; ++i) {
            values[i] = this.get(parameters[i].getType());
        }

        return type.cast(ctor.newInstance(values));
    }

    /**
     * Inject any dependencies in an object's annotated fields.
     *
     * @param obj       the object instance to modify
     * @param type      the type of the object instance
     * @param <T>       the type of the object instance
     *
     * @throws IllegalAccessException if at least one field cannot be modified
     */
    private <T> void populateFields(final T obj, final Class<T> type)
            throws IllegalAccessException {
        final List<Field> fields = this.collectInjectableFields(type);

        for (final Field field : fields) {
            final Class<?> fieldType = field.getType();
            final Object instance = this.get(fieldType);

            final boolean isAccessible = field.canAccess(obj);
            try {
                if (!isAccessible) {
                    field.setAccessible(true);
                }

                field.set(obj, instance);
            } catch (final SecurityException ex) {
                logger.warn("Unable to make {} accessible. Injection may fail.",
                        field.toString());
            } finally {
                // Restore the inaccessibility when we're done, even after an exception.
                if (!isAccessible) {
                    field.setAccessible(false);
                }
            }
        }
    }

    /**
     * Recursively collects all fields that are eligible for dependency injection in a class.
     *
     * @param type the type to collect the fields for
     * @param <T>  the type to collect the fields for
     *
     * @return a list of all injectable fields
     */
    private <T> List<Field> collectInjectableFields(final Class<T> type) {
        final List<Field> fields = new ArrayList<>();

        for (final Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                fields.add(field);
            }
        }

        final Class<? super T> superClass = type.getSuperclass();
        if (superClass != null) {
            fields.addAll(this.collectInjectableFields(superClass));
        }

        return fields;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> type) {
        // If there's a cached version ready, return that.
        final Object cachedInstance = this.instances.get(type);
        if (cachedInstance != null) {
            return type.cast(cachedInstance);
        }

        final Class<? extends T> implType;
        if (this.factories.containsKey(type)) {
            // Use the factory if one is registered.
            implType = type;
        } else {
            // Otherwise look for the implementor class.
            implType = this.implementations.getOrDefault(type, type);
        }

        final T instance = this.instantiate(implType);
        this.instances.put(type, instance);
        return instance;
    }

    /** {@inheritDoc} */
    public <S, T extends S> void add(final Class<S> iface, final T impl) {
        this.implementations.put(iface, impl.getClass());
        this.instances.put(iface, impl);
    }

    /** {@inheritDoc} */
    public <S> void addFactory(final Class<S> iface, final Factory<S> factory) {
        this.factories.put(iface, factory);
    }

    /** {@inheritDoc} */
    public <S, T extends S> S addDefault(final Class<S> iface, final T impl) {
        if (!this.implementations.containsKey(iface)) {
            this.add(iface, impl);
        }

        return iface.cast(this.instances.get(iface));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <S> Factory<S> addDefaultFactory(final Class<S> iface, final Factory<S> factory) {
        if (!this.factories.containsKey(iface)) {
            this.factories.put(iface, factory);
        }

        return (Factory<S>) this.factories.get(iface);
    }

    /**
     * Registers a factory for the service, but only if there was no other factory in place.
     *
     * The factory class is instantiated by the resolver before the factory is registered and
     * only if no other factory was in place.
     *
     * @param iface the interface the service implements
     * @param factoryClass the factory class to instantiate and register
     *
     * @return the factory currently in the resolver, after updating
     */
    @Override
    @SuppressWarnings("unchecked")
    public <S> Factory<S> addDefaultFactory(
            final Class<S> iface, final Class<? extends Factory<S>> factoryClass
    ) {
        if (!this.factories.containsKey(iface)) {
            this.factories.put(iface, this.get(factoryClass));
        }

        return (Factory<S>) this.factories.get(iface);
    }

    /** {@inheritDoc} */
    public <S, T extends S> void implement(final Class<S> iface, final Class<T> impl) {
        if (impl.isInterface() || Modifier.isAbstract(impl.getModifiers())) {
            throw new UninstantiableTypeException(impl.getCanonicalName());
        }

        this.implementations.put(iface, impl);
    }

    /** {@inheritDoc} */
    public <S, T extends S> Class<?> implementDefault(final Class<S> iface, final Class<T> impl) {
        if (!this.implementations.containsKey(iface)) {
            this.implement(iface, impl);
        }

        return this.implementations.get(iface);
    }
}
