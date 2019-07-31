package annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation on Method to note an Api call. Can be repeated.
 * @author gmittmann
 *
 */
@Retention(CLASS)
@Target(METHOD)
@Repeatable(ConsumesAPIs.class)
public @interface ConsumesAPI {

	public static String DEFAULT_SERVICE = "--";

	/**
	 * HTTP-method, which is used.
	 * @return
	 */
	String method();

	/**
	 * Path that is calles.
	 * @return
	 */
	String path();

	/**
	 * (optional) tag of the service to be called. Should be of form
	 * [groupId]:[artifactId]:[version], version is optional.
	 * 
	 * @return
	 */
	String service() default DEFAULT_SERVICE;

}
