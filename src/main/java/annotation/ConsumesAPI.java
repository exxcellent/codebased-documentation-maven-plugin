package annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target(METHOD)
public @interface ConsumesAPI {
	
	public static String DEFAULT_SERVICE = "--";

	String method();

	String path();

	String service() default "--";

}
