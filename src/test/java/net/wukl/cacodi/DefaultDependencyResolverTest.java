package net.wukl.cacodi;

import net.wukl.cacodi.classes.AbstractClass;
import net.wukl.cacodi.classes.ComplexNested;
import net.wukl.cacodi.classes.Complex;
import net.wukl.cacodi.classes.ManualConstructor;
import net.wukl.cacodi.classes.NullaryConstructor;
import net.wukl.cacodi.classes.PrivateConstructor;
import net.wukl.cacodi.classes.PrivateDependentConstructor;
import net.wukl.cacodi.classes.RiskyConstructor;
import net.wukl.cacodi.classes.SimpleImplicit;
import net.wukl.cacodi.classes.ThingDoer;
import net.wukl.cacodi.classes.ThingDoerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the default dependency resolver.
 *
 * @author Luc Everse
 */
public class DefaultDependencyResolverTest {
    private DependencyResolver resolver;

    @BeforeEach
    public void before() {
        this.resolver = new DependencyResolver();
    }

    @Test
    public void testNullaryConstructor() {
        final NullaryConstructor instance = this.resolver.get(NullaryConstructor.class);

        assertNotNull(instance);
    }

    @Test
    public void testSimpleImplicitInjection() {
        final SimpleImplicit instance = this.resolver.get(SimpleImplicit.class);

        assertNotNull(instance);

        assertEquals(instance.getLeft(), instance.getRight());
    }
    @Test
    public void testComplex() {
        final int count = 3;
        final String fieldDep = "Injected through field injection!";
        this.resolver.add(String.class, fieldDep);
        this.resolver.add(Integer.class, count);

        final Complex instance = this.resolver.get(Complex.class);

        assertNotNull(instance);
        assertNotNull(instance.getOne());
        assertNotNull(instance.getOther());
        assertNotNull(instance.getFieldDep());
        assertEquals(instance.getFieldDep(), fieldDep);
        assertEquals(instance.getCount(), count);
    }

    @Test
    public void testComplexNested() {
        final int count = 3;
        final String fieldDep = "Injected through field injection!";
        this.resolver.add(String.class, fieldDep);
        this.resolver.add(Integer.class, count);

        final Complex instance = this.resolver.get(ComplexNested.class);

        assertNotNull(instance);
        assertNotNull(instance.getOne());
        assertNotNull(instance.getOther());
        assertNotNull(instance.getFieldDep());
        assertEquals(instance.getFieldDep(), fieldDep);
        assertEquals(instance.getCount(), count);
    }

    @Test
    public void testPrivateConstructor() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(PrivateConstructor.class)
        );
    }

    @Test
    public void testRiskyConstructor() {
        assertThrows(Exception.class, () -> this.resolver.get(RiskyConstructor.class));
    }

    @Test
    public void testPrivateDependentConstructor() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(PrivateDependentConstructor.class)
        );
    }

    @Test
    public void testPrivateInnerClass() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(PrivateInnerClass.class)
        );
    }

    @Test
    public void testFactory() {
        final var str = "test string for testing";
        final Factory<String> factory = di -> str;
        this.resolver.addFactory(String.class, factory);

        assertThat(this.resolver.get(String.class)).isEqualTo(str);
    }

    @Test
    public void testSkipManual() {
        final var man = this.resolver.get(ManualConstructor.class);

        assertThat(man.getCpx()).isNotNull();
    }

    @Test
    public void testInstantiateAbstract() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(AbstractClass.class)
        );
    }

    @Test
    public void testImplementInterface() {
        this.resolver.implement(ThingDoer.class, ThingDoerImpl.class);

        assertThat(this.resolver.get(ThingDoer.class)).isInstanceOf(ThingDoerImpl.class);
    }

    @Test
    public void testManufactureInterface() {
        this.resolver.addFactory(ThingDoer.class, di -> di.get(ThingDoerImpl.class));

        assertThat(this.resolver.get(ThingDoer.class)).isInstanceOf(ThingDoerImpl.class);
    }

    @Test
    public void testFactoryOverInterface() {
        this.resolver.implement(ThingDoer.class, ThingDoerImpl.class);
        this.resolver.addFactory(ThingDoer.class, di -> () -> "other stuff");

        assertThat(this.resolver.get(ThingDoer.class)).isNotInstanceOf(ThingDoerImpl.class);
    }

    @Test
    public void testClone() {
        final var thing = this.resolver.get(String.class);

        final var newResolver = new DependencyResolver(this.resolver);

        final var other = newResolver.get(String.class);

        assertThat(other).isSameAs(thing);
    }

    @Test
    public void testSupplierFactory() {
        this.resolver.addFactory(SimpleImplicit.class,
                () -> new SimpleImplicit(null, new NullaryConstructor())
        );
        final var simpleImplicit = this.resolver.get(SimpleImplicit.class);
        assertThat(simpleImplicit).extracting(SimpleImplicit::getLeft).isNull();
        assertThat(simpleImplicit).extracting(SimpleImplicit::getRight).isNotNull();
    }

    @Test
    public void testDefaultSupplierFactory() {
        final Supplier<SimpleImplicit> supplier =
                () -> new SimpleImplicit(new NullaryConstructor(), null);
        final var factory = this.resolver.addDefaultFactory(SimpleImplicit.class, supplier);
        final var simpleImplicit = this.resolver.get(SimpleImplicit.class);
        assertThat(simpleImplicit).extracting(SimpleImplicit::getLeft).isNotNull();
        assertThat(simpleImplicit).extracting(SimpleImplicit::getRight).isNull();
        assertThat(factory).isInstanceOf(IDependencyResolver.CapturedSupplier.class)
                .extracting(f -> ((IDependencyResolver.CapturedSupplier) f).getSupplier())
                .isSameAs(supplier);
    }

    @Test
    public void testCapturedSupplierIsManual() {
        assertThrows(UnresolvableDependencyException.class,
                () -> this.resolver.get(IDependencyResolver.CapturedSupplier.class)
        );
    }

    private class PrivateInnerClass {

    }
}
