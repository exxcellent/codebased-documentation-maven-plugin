package annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target(METHOD)
@Repeatable(ConsumesAPIs.class)
public @interface ConsumesAPI {
	
	public static String DEFAULT_SERVICE = "--";

	String method();

	String path();

	String service() default DEFAULT_SERVICE;

}
