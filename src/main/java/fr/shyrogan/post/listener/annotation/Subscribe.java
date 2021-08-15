package fr.shyrogan.post.listener.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark an field/method that needs to be registered by the
 * {@link fr.shyrogan.post.factory.impl.AnnotatedFieldFactory}/{@link fr.shyrogan.post.factory.impl.AnnotatedMethodFactory}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Subscribe {

    /**
     * Returns the priority of the marked (future) receiver.
     *
     * @return The priority of this receiver.
     */
    int priority() default 0;

}
