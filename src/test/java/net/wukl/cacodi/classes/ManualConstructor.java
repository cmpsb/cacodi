package net.wukl.cacodi.classes;

import net.wukl.cacodi.Manual;

public class ManualConstructor {
    private final NullaryConstructor cpx;

    @Manual
    public ManualConstructor() {
        throw new AssertionError("No one accesses this one");
    }

    public ManualConstructor(final NullaryConstructor cpx) {
        this.cpx = cpx;
    }

    public NullaryConstructor getCpx() {
        return this.cpx;
    }
}
