package reader.impl.apiconsumption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Sets;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import annotation.ConsumesAPI;
import reader.interfaces.ConsumesAPIReader;
import util.ConsumeDescription;

public class AnnotationReader implements ConsumesAPIReader {

	@Override
	public List<ConsumeDescription> getAPIConsumption(File src) {
		List<ConsumeDescription> returnList = new ArrayList<>();

		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(src);

		for (JavaClass clazz : builder.getClasses()) {
			if (clazz.getSource().getImports().contains(ConsumesAPI.class.getCanonicalName())) {
				System.out.println("found class -> " + clazz.getCanonicalName());
				searchForAPIInfo(clazz, returnList);
			}

		}

		return returnList;
	}

	/**
	 * Iterates through the methods of the given class and their annotations. If the
	 * ConsumesAPI annotation is found, read the info and add it to the returnList.
	 * 
	 * @param clazz      class to be evaluated.
	 * @param returnList List containing the found api consumption info.
	 */
	private void searchForAPIInfo(JavaClass clazz, List<ConsumeDescription> returnList) {
		for (JavaMethod method : clazz.getMethods()) {
			for (JavaAnnotation annotation : method.getAnnotations()) {
				if (annotation.getType().getCanonicalName().equals(ConsumesAPI.class.getCanonicalName())) {
					addConsumesAPIInfo(clazz, annotation, returnList);
					break;
				}
			}
		}
	}

	/**
	 * Reads the parameter values in the annotation and wraps them in an
	 * ConsumeDescriptionTriple object, which is added to the list, if there was not
	 * already an object with the same path and service name.
	 * 
	 * @param annotation JavaAnnotation of type ConsumesAPI.class.
	 * @param consumeList LIst to which the new info is to be added to.
	 */
	private void addConsumesAPIInfo(JavaClass clazz, JavaAnnotation annotation, List<ConsumeDescription> consumeList) {
		String name = ConsumesAPI.DEFAULT_SERVICE;
		if (annotation.getNamedParameter("service") != null) {
			name = format(annotation.getNamedParameter("service").toString());
		}

		String path = setTypeInPath(format(annotation.getNamedParameter("path").toString()));
		String methodName = format(annotation.getNamedParameter("method").toString());
		String packageName = clazz.getPackageName();

		System.out.println(name + "  -  " + path + "  -  " + methodName);

		if (!addToList(consumeList, name, packageName, path, methodName)) {
			ConsumeDescription newTriple = new ConsumeDescription();
			newTriple.setServiceName(name);
			newTriple.setPackageName(packageName);
			newTriple.addPathToMethod(path, Sets.newHashSet(methodName));
			consumeList.add(newTriple);
		}
	}

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
			if (currentTriple.getServiceName().equals(serviceName) && currentTriple.getPackageName().equals(packageName)) {
				currentTriple.addPathToMethod(path, Sets.newHashSet(method));
				return true;
			}
		}

		return false;

	}
}
