package annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Collection of ConsumesAPI annotations.
 * @author gmittmann
 *
 */
@Retention(CLASS)
@Target(METHOD)
public @interface ConsumesAPIs {

	ConsumesAPI[] value();
	
}
