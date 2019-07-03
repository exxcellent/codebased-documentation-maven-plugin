package reader.impl.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.PathParam;

import org.apache.maven.plugin.logging.Log;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

import reader.interfaces.APIReader;
import util.HttpMethods;
import util.OfferDescription;
import util.Pair;

/**
 * REST Api reader for Spring Boot. Currently externalized configuration is
 * ignored. e.g. @Value annotations can't be resolved. TODO: read
 * application.properties or application.yml from src/main/resources TODO:
 * REFACTOR THIS....
 * 
 * @author gmittmann
 *
 */
public class SPRINGReader implements APIReader {

	private static final List<String> CONTROLLER_ANNOTATIONS = Arrays.asList(Controller.class.getCanonicalName(),
			RestController.class.getCanonicalName(), Component.class.getCanonicalName(),
			Service.class.getCanonicalName());

	private static final List<String> HTTP_METHODS = Arrays.asList(
			RequestMethod.class.getSimpleName() + "." + RequestMethod.GET.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.PUT.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.POST.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.DELETE.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.HEAD.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.OPTIONS.name(),
			RequestMethod.class.getSimpleName() + "." + RequestMethod.PATCH.name());

	private static final List<String> HTTP_METHODS_MAPPING = Arrays.asList(GetMapping.class.getCanonicalName(),
			PutMapping.class.getCanonicalName(), PostMapping.class.getCanonicalName(),
			DeleteMapping.class.getCanonicalName(), null, "", PatchMapping.class.getCanonicalName());

	private Log log;
	private File apiConfigFile;
	private String contextPath;

	public SPRINGReader(Log log, File apiConfigFile, String contextPath) {
		this.log = log;
		this.apiConfigFile = apiConfigFile;
		this.contextPath = contextPath;
	}

	@Override
	public List<OfferDescription> getPathsAndMethods(File src) {
		String basePath;
		if (contextPath != null) {
			basePath = contextPath;
		} else {
			basePath = getApplicationPath();
		}

		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(src);

		Map<String, OfferDescription> packageNameToOffer = new HashMap<>();

		for (JavaClass currentClass : builder.getClasses()) {

			List<Pair<String, HttpMethods>> paths = new ArrayList<>();
			Pair<List<Pair<String, HttpMethods>>, Boolean> mappingAndController = getBaseMappingAndController(
					currentClass);
			boolean controller = mappingAndController.getRight();
			List<Pair<String, HttpMethods>> baseMapping = mappingAndController.getLeft();

			if (controller || baseMapping != null) { // there was a mapping annotation or the class is a controller.
				baseMapping = addContextPathToBaseMapping(basePath, baseMapping);
				for (JavaMethod method : currentClass.getMethods()) {
					paths.addAll(getMethodAnnotations(method, baseMapping));
				}
			}
			
			if (!paths.isEmpty()) {
				String packageName = currentClass.getPackageName();
				
				OfferDescription currentOffer = null;
				if (packageNameToOffer.containsKey(packageName)) {
					currentOffer = packageNameToOffer.get(packageName);
				} else {
					currentOffer = new OfferDescription();
					currentOffer.setPackageName(packageName);
					packageNameToOffer.put(packageName, currentOffer);
				}
				
				for (Pair<String, HttpMethods> pair : paths) {
					currentOffer.addPathToMethod(pair);
				}
				
			}
		}

		return new ArrayList<>(packageNameToOffer.values());
	}

	/**
	 * Checks whether the given JavaClass has controller annotations and/or mapping
	 * annotations. If it has,the boolean is set to true and/or the mapping found is
	 * evaluated and returned.
	 * 
	 * @param clz class under inspection.
	 * @return Pair containing the found mapping or null and boolean whether the
	 *         class is a controller class.
	 */
	private Pair<List<Pair<String, HttpMethods>>, Boolean> getBaseMappingAndController(JavaClass clz) {
		boolean controller = false;
		List<Pair<String, HttpMethods>> baseMapping = null;
		for (JavaAnnotation annotation : clz.getAnnotations()) {
			String annotationClass = annotation.getType().getCanonicalName();

			if (CONTROLLER_ANNOTATIONS.contains(annotationClass)) {
				controller = true;
			} else if (annotationClass.equals(RequestMapping.class.getCanonicalName())
					|| HTTP_METHODS_MAPPING.contains(annotationClass)) {
				baseMapping = getMapping(annotation);
			}
		}
		return new Pair<List<Pair<String, HttpMethods>>, Boolean>(baseMapping, controller);
	}

	private List<Pair<String, HttpMethods>> addContextPathToBaseMapping(String contextPath,
			List<Pair<String, HttpMethods>> baseMappings) {
		if (contextPath == null || contextPath.isEmpty()) {
			return baseMappings;
		}
		String path = startWithSlash(contextPath);
		List<Pair<String, HttpMethods>> mappings = new ArrayList<>();
		if (baseMappings == null) {
			Pair<String, HttpMethods> newPair = new Pair<>(path, null);
			mappings.add(newPair);
			return mappings;
		}
		for (Pair<String, HttpMethods> currentPair : baseMappings) {
			Pair<String, HttpMethods> newPair = new Pair<>(path + currentPair.getLeft(), currentPair.getRight());
			mappings.add(newPair);
		}
		return mappings;
	}

	/**
	 * Searches through the annotations on the given method for mapping annotations.
	 * Evaluates them and concatenates them to the base mapping, if given and
	 * necessary.
	 * 
	 * @param method       JavaMethod whose annotations are to be evaluated
	 * @param baseMappings mappings on class level.
	 * @return List of found mappings. One Pair for each method available on the
	 *         paths found.
	 */
	private List<Pair<String, HttpMethods>> getMethodAnnotations(JavaMethod method,
			List<Pair<String, HttpMethods>> baseMappings) {

		List<Pair<String, HttpMethods>> pairList = new ArrayList<>();

		for (JavaAnnotation annotation : method.getAnnotations()) {
			String annotationClass = annotation.getType().getCanonicalName();

			if (annotationClass.equals(RequestMapping.class.getCanonicalName())
					|| HTTP_METHODS_MAPPING.contains(annotationClass)) {
				List<Pair<String, HttpMethods>> methodMapping = getMapping(annotation);

				for (Pair<String, HttpMethods> currentMapping : methodMapping) {
					if (currentMapping.getRight() != null && baseMappings != null) {
						for (Pair<String, HttpMethods> base : baseMappings) {
							String path = startWithEndsWithoutSlash(base.getLeft())
									+ startWithSlash(currentMapping.getLeft());
							path = setTypeInPath(method, path);
							pairList.add(new Pair<String, HttpMethods>(path, currentMapping.getRight()));
						}
					} else if (currentMapping.getRight() != null) {
						String path = setTypeInPath(method, currentMapping.getLeft());
						pairList.add(new Pair<String, HttpMethods>(path, currentMapping.getRight()));
					} else {
						if (baseMappings != null) {
							for (Pair<String, HttpMethods> base : baseMappings) {
								if (base.getRight() != null) {
									String path = startWithEndsWithoutSlash(base.getLeft())
											+ startWithSlash(currentMapping.getLeft());
									path = setTypeInPath(method, path);
									pairList.add(new Pair<String, HttpMethods>(path, base.getRight()));
								} else { // all methods are allowed.
									for (HttpMethods httpMethod : HttpMethods.values()) {
										String path = startWithEndsWithoutSlash(base.getLeft())
												+ startWithSlash(currentMapping.getLeft());
										path = setTypeInPath(method, path);
										pairList.add(new Pair<String, HttpMethods>(path, httpMethod));
									}
								}
							}
						} else {
							for (HttpMethods httpMethod : HttpMethods.values()) {
								pairList.add(new Pair<String, HttpMethods>(
										setTypeInPath(method, startWithEndsWithoutSlash(currentMapping.getLeft())), httpMethod));
							}
						}
					}
				}
			}
		}

		return pairList;
	}

	private String setTypeInPath(JavaMethod method, String path) {
		String newPath = removeRegularExpressionsFromPath(path);
		for (JavaParameter param : method.getParameters()) {
			for (JavaAnnotation annotation : param.getAnnotations()) {
				if (annotation.getType().getCanonicalName().equalsIgnoreCase(PathVariable.class.getCanonicalName())) {
					Object name = annotation.getNamedParameter("value");
					if (name == null) {
						name = annotation.getNamedParameter("name") == null ? param.getName()
								: annotation.getNamedParameter("name");
					}
					String paramName = name.toString().replaceAll("\"", "");
					if (newPath.contains("{" + paramName + "}")) {
						newPath = newPath.replace("{" + paramName + "}", "{" + param.getJavaClass().getSimpleName().toUpperCase(Locale.ROOT) + "}");
					}
				}
			}
		}

		return newPath;
	}
	
	private String removeRegularExpressionsFromPath(String path) {
		return path.replaceAll(":[^}]*}", "}");
	}

	/**
	 * Evaluates the given JavaAnnotation (should be of type RequestMapping or
	 * GetMapping, PutMapping etc.) and returns pairs with the found path and
	 * method.
	 * 
	 * @param annotation JavaAnnotation to be evaluated.
	 * @return List of found mappings.
	 */
	private List<Pair<String, HttpMethods>> getMapping(JavaAnnotation annotation) {
		List<Pair<String, HttpMethods>> pairs = new ArrayList<>();

		List<String> paths = new ArrayList<>();
		List<HttpMethods> methods = new ArrayList<>();

		Object obj = annotation.getPropertyMap().get("value");
		obj = (obj == null) ? null : annotation.getPropertyMap().get("value").getParameterValue();
		if (obj instanceof String) {
			paths.add(((String) obj).trim());
		} else if (obj instanceof List) {
			for (Object o : (List) obj) {
				paths.add(o.toString());
			}
		} else {
			if (obj == null) {
				paths.add("");
			} else {
				log.error("Value type: " + obj.toString());
			}
		}

		if (annotation.getType().getCanonicalName().contentEquals(RequestMapping.class.getCanonicalName())) {
			obj = annotation.getNamedParameter("method");

			if (obj instanceof List) {
				for (String meth : (List<String>) obj) {
					if (HTTP_METHODS.contains(meth.toString())) {
						int index = HTTP_METHODS.indexOf(meth.toString());
						methods.add(HttpMethods.values()[index]);
					}
				}
			} else if (obj instanceof String) {
				if (HTTP_METHODS.contains(obj.toString())) {
					int index = HTTP_METHODS.indexOf(obj.toString());
					methods.add(HttpMethods.values()[index]);
				}
			} else {
				methods = null;
			}

		} else {
			int index = HTTP_METHODS_MAPPING.indexOf(annotation.getType().getCanonicalName());
			if (index != -1) {
				methods.add(HttpMethods.values()[index]);
			}
		}

		for (String path : paths) {
			path = path.replace("\"", "");
			if (methods != null && !methods.isEmpty()) {
				for (HttpMethods method : methods) {
					pairs.add(new Pair<String, HttpMethods>(path, method));
				}
			} else {
				pairs.add(new Pair<String, HttpMethods>(path, null));
			}
		}

		return pairs;
	}

	/**
	 * Makes sure the given String starts with a '/'.
	 * 
	 * @param toAdd String to be brought to form.
	 * @return the given String starting with a '/'
	 */
	private String startWithSlash(String toAdd) {
		toAdd = toAdd.trim();
		if (toAdd.isEmpty() || toAdd.startsWith("/")) {
			return toAdd;
		}
		return "/" + toAdd;
	}

	/**
	 * Makes sure the given string starts with a '/' but does not end with '/'.
	 * 
	 * @param uri String to be brought to form.
	 * @return the given String without a '/' in front.
	 */
	private String startWithEndsWithoutSlash(String uri) {
		uri = uri.trim();
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 2);
		}
		return uri;

	}

	private String getApplicationPath() {
		if (apiConfigFile == null || !apiConfigFile.exists()) {
			return "";
		}

		String fileType = "";
		if (apiConfigFile.isFile()) {
			String[] fileName = apiConfigFile.getName().split(".");
			fileType = fileName[fileName.length - 1];
		}

		ClassLoader loader = this.getClass().getClassLoader();
		try (InputStream stream = loader.getResourceAsStream(apiConfigFile.getAbsolutePath())) {
			if (fileType.equalsIgnoreCase("properties")) {
				Properties props = new Properties();
				props.load(stream);
				String value = props.getProperty("server.servlet.context-path");
				return value == null ? "" : value;
			} else if (fileType.equalsIgnoreCase("yml")) {
				Yaml yaml = new Yaml();
				Map<String, Object> obj = yaml.load(stream);
				Object value = obj.get("server.servlet.context-path");
				return value == null ? "" : value.toString();
			}
		} catch (IOException e) {
			log.error("Error reading property in file: " + apiConfigFile.getAbsolutePath());
			log.error(e.getMessage());
			return "";
		}

		return "";
	}

}
