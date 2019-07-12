package net.wukl.cacodi;

/**
 * Thrown if the user attempts to register an implementing class (using the
 * {@link InterfaceMappingDependencyResolver#implement(Class, Class)} or
 * {@link InterfaceMappingDependencyResolver#implementDefault(Class, Class)} methods)
 * that is a not instantiable, i.e. another interface or an abstract class.
 */
public class UninstantiableTypeException extends RuntimeException {
    /**
     * Creates a new uninstantiable type exception.
     */
    public UninstantiableTypeException() {
        super();
    }

    /**
     * Creates a new uninstantiable type exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public UninstantiableTypeException(final String message) {
        super(message);
    }

    /**
     * Creates a new uninstantiable type exception.
     *
     * @param cause the exception that caused this exception
     */
    public UninstantiableTypeException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new uninstantiable type exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public UninstantiableTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
