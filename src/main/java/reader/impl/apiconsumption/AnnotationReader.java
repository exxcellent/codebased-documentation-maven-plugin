package reader.impl.apiconsumption;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.logging.Log;

import com.google.common.collect.Sets;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.impl.DefaultJavaAnnotation;

import annotation.ConsumesAPI;
import annotation.ConsumesAPIs;
import reader.interfaces.ConsumesAPIReader;
import util.ConsumeDescription;
import util.HttpMethods;

/**
 * Class for reading ConsumesAPI and ConsumesAPIs annotations.
 * 
 * @author gmittmann
 *
 */
public class AnnotationReader implements ConsumesAPIReader {

	private Log log;

	public AnnotationReader(Log log) {
		this.log = log;
	}

	@Override
	public List<ConsumeDescription> getAPIConsumption(File src) {
		List<ConsumeDescription> returnList = new ArrayList<>();

		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(src);

		for (JavaClass clazz : builder.getClasses()) {
			if (clazz.getSource().getImports().contains(ConsumesAPI.class.getCanonicalName())) {
				log.info("Annotations in class: " + clazz.getCanonicalName());
				searchForAPIInfo(clazz, returnList);
			}

		}

		return returnList;
	}

	/**
	 * Iterates through the methods of the given class and their annotations. If the
	 * ConsumesAPI or ConsumesAPIs annotation are found, read the info and add it to
	 * the returnList.
	 * 
	 * @param clazz
	 *            class to be evaluated.
	 * @param returnList
	 *            List containing the found api consumption info.
	 */
	private void searchForAPIInfo(JavaClass clazz, List<ConsumeDescription> returnList) {
		for (JavaMethod method : clazz.getMethods()) {
			for (JavaAnnotation annotation : method.getAnnotations()) {
				if (annotation.getType().getCanonicalName().equals(ConsumesAPI.class.getCanonicalName())) {
					addConsumesAPIInfo(clazz, annotation, returnList);
				}
				if (annotation.getType().getCanonicalName().equals(ConsumesAPIs.class.getCanonicalName())) {
					addAllConsumesAPIInfo(clazz, annotation, returnList);
				}
			}
		}
	}

	/**
	 * Use this method to analyze ConsumesAPIs-Annotations. Splits this annotation
	 * in the ConsumeAPI annotations within and calls addConsumesAPIInfo for each of
	 * them.
	 * 
	 * @param clazz
	 *            JavaClass Object of the current class.
	 * @param annotation
	 *            Annotation, should be of type ConsumesAPIs.
	 * @param consumeList
	 *            List containing all found consume info. Results are added into it.
	 */
	private void addAllConsumesAPIInfo(JavaClass clazz, JavaAnnotation annotation,
			List<ConsumeDescription> consumeList) {
		Object val = annotation.getNamedParameter("value");

		if (val instanceof LinkedList) {
			LinkedList<Object> valList = (LinkedList<Object>) val;

			for (Object obj : valList) {

				if (obj instanceof DefaultJavaAnnotation) {
					addConsumesAPIInfo(clazz, (DefaultJavaAnnotation) obj, consumeList);
				}
			}
		}
	}

	/**
	 * Reads the parameter values in the annotation and wraps them in an
	 * ConsumeDescriptionTriple object, which is added to the list, if there was not
	 * already an object with the same path and service name.
	 * 
	 * @param clazz
	 *            JavaClass Object of the current class. Needed to find the current
	 *            package.
	 * @param annotation
	 *            JavaAnnotation of type ConsumesAPI.class.
	 * @param consumeList
	 *            List to which the new info is to be added to.
	 */
	private void addConsumesAPIInfo(JavaClass clazz, JavaAnnotation annotation, List<ConsumeDescription> consumeList) {
		String name = ConsumesAPI.DEFAULT_SERVICE;

		if (annotation.getNamedParameter("service") != null) {
			name = format(annotation.getNamedParameter("service").toString());
			if (!couldBeValidService(name)) {
				log.error(name + " is not a valid tag. Please follow the pattern of [groupId]:[artifactId].");
				log.error("line: " + annotation.getLineNumber());
				name = ConsumesAPI.DEFAULT_SERVICE;
			}
		}

		String path = setTypeInPath(format(annotation.getNamedParameter("path").toString()));
		String methodName = format(annotation.getNamedParameter("method").toString());
		String packageName = clazz.getPackageName();

		try {
			HttpMethods.valueOf(methodName.toUpperCase(Locale.ROOT));
			log.info(name + "  -  " + path + "  -  " + methodName);
			if (!addToList(consumeList, name, packageName, path, methodName)) {
				ConsumeDescription newTriple = new ConsumeDescription();
				newTriple.setServiceName(name);
				newTriple.setPackageName(packageName);
				newTriple.addPathToMethod(path, Sets.newHashSet(methodName));
				consumeList.add(newTriple);
			}
		} catch (IllegalArgumentException e) {
			log.error("Error at line: " + annotation.getLineNumber());
			log.error(methodName + " is not a valid HttpMethod");
		}
	}

	/**
	 * Checks if service name conforms to either [groupId]:[artifactId] or
	 * [groupId]:[artifactId]:[version]
	 * 
	 * @param serviceName
	 *            name to check
	 * @return true, if name is a valid service tag
	 */
	private boolean couldBeValidService(String serviceName) {
		return serviceName.matches("([a-zA-Z\\.\\-\\_\\d]++\\:){1,2}([a-zA-Z\\.\\-\\_\\d]++)");
	}

	/**
	 * trims given String and removes all quotation marks.
	 * 
	 * @param toFormat
	 *            String to be formatted. won't be changed.
	 * @return formatted String
	 */
	private String format(String toFormat) {
		return toFormat.trim().trim().replaceAll("\"", "");
	}

	private String setTypeInPath(String path) {
		String setPath = "";
		String[] splitPath = path.split("/");
		if (splitPath == null) {
			System.out.println("splitPath is null");
			return path;
		}
		for (String part : splitPath) {
			if (part.startsWith("{") && part.endsWith("}")) {
				setPath = String.join("/", setPath, part.toUpperCase(Locale.ROOT));
			} else if (!part.isEmpty()) {
				setPath = String.join("/", setPath, part);
			}
		}
		if (setPath == null || setPath.isEmpty()) {
			return path;
		}
		return setPath;
	}

	/**
	 * Searches for an ConsumeDescriptionTriple in the given list, which has the
	 * given name and path set as its serviceName and path. If there is such an
	 * object, the given method is added to it and the method returns true, else
	 * nothing is done and false is returned.
	 * 
	 * @param consume
	 * @param serviceName
	 * @param path
	 * @param method
	 * @return
	 */
	private boolean addToList(List<ConsumeDescription> consume, String serviceName, String packageName, String path,
			String method) {

		for (ConsumeDescription currentTriple : consume) {
			if (currentTriple.getServiceName().equals(serviceName)
					&& currentTriple.getPackageName().equals(packageName)) {
				currentTriple.addPathToMethod(path, Sets.newHashSet(method));
				return true;
			}
		}

		return false;

	}
}
