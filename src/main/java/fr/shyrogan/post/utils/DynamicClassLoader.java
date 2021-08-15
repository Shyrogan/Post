package fr.shyrogan.post.utils;

import java.util.Optional;

/**
 * A dynamic class loader that allows injection of custom cla
 */
public class DynamicClassLoader extends ClassLoader {


    /**
     * Creates a new {@link DynamicClassLoader}.
     */
    public DynamicClassLoader() {
        super();
    }

    /**
     * Creates a new {@link DynamicClassLoader} that has specified parent.
     *
     * @param parent Parent {@link DynamicClassLoader}
     */
    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Tries to find a already loaded class in the class loader.
     *
     * @param className The classes name.
     *
     * @return The class.
     */
    public Optional<Class<?>> lookForClass(String className) {
        return Optional.ofNullable(this.findLoadedClass(className));
    }

    /**
     * Loads a class by the class name and the bytecode.
     *
     * @param className The classes name.
     * @param bytecode  The classes bytecode.
     *
     * @return The class.
     *
     * @see ClassLoader#defineClass(String, byte[], int, int)
     */
    public Class<?> createClass(String className, byte[] bytecode) {
        try {
            return this.defineClass(className, bytecode, 0, bytecode.length);
        } catch (LinkageError e) {
            return this.findLoadedClass(className);
        }
    }
}