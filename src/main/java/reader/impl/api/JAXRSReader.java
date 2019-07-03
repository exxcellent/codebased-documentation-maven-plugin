package reader.impl.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

import reader.interfaces.APIReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import util.HttpMethods;
import util.OfferDescription;
import util.Pair;

public class JAXRSReader implements APIReader {

	private static final List<String> HTTP_METHODS = Arrays.asList(GET.class.getCanonicalName(),
			PUT.class.getCanonicalName(), POST.class.getCanonicalName(), DELETE.class.getCanonicalName(),
			HEAD.class.getCanonicalName(), OPTIONS.class.getCanonicalName());

	private MavenProject project;
	private Log log;
	private File apiConfigFile;
	private String contextPath;

	public JAXRSReader(MavenProject project, Log log, File apiConfigFile, String contextPath) {
		this.project = project;
		this.log = log;
		this.apiConfigFile = apiConfigFile;
		this.contextPath = contextPath;
	}

	@Override
	public List<OfferDescription> getPathsAndMethods(File src) {

		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(src);
		String applicationPath = readPathFromWebXML();

		Map<String, OfferDescription> packageNamesToOffers = new HashMap<>();
		List<Pair<String, HttpMethods>> mappings = new ArrayList<>();

		for (JavaClass currentClass : builder.getClasses()) {
			for (JavaAnnotation annotation : currentClass.getAnnotations()) {
				if (applicationPath == null
						&& annotation.getType().getCanonicalName().equals(ApplicationPath.class.getCanonicalName())) {
					applicationPath = annotation.getNamedParameter("value").toString();
				} else if (annotation.getType().getCanonicalName().equals(Path.class.getCanonicalName())) {
					String classPath = annotation.getNamedParameter("value") == null ? ""
							: annotation.getNamedParameter("value").toString();

					for (JavaMethod method : currentClass.getMethods()) {
						Pair<String, HttpMethods> methodPair = getMethodAnnotations(method, classPath);
						if (methodPair != null) {
							mappings.add(methodPair);

							String packageName = currentClass.getPackageName();
							OfferDescription currentOffer = null;
							if (packageNamesToOffers.containsKey(packageName)) {
								currentOffer = packageNamesToOffers.get(packageName);
							} else {
								currentOffer = new OfferDescription();
								currentOffer.setPackageName(packageName);
								packageNamesToOffers.put(packageName, currentOffer);
							}
							currentOffer.addPathToMethod(methodPair);
						}

					}
				}
			}
		}

		if (contextPath != null) {
			applicationPath = formatBasePath(contextPath)
					+ (applicationPath == null ? "" : formatConcatPath(applicationPath));
		}
		String glassfishPath = readPathFromGlassfishWebXML();
		if (glassfishPath != null) {
			applicationPath = formatBasePath(glassfishPath) + formatConcatPath(applicationPath);
		}

		return concatApplicationPathTo(packageNamesToOffers.values(), applicationPath);
	}

	/**
	 * Iterates through the annotations on the given method. If there is a Http
	 * method annotation, the pair containing the found mapping. If there is a Path
	 * annotation to the http method, the path is concatenated to the given path
	 * (from the path annotation on class level). Currently methods with only path
	 * annotations are not supported and return null.
	 * 
	 * @param method    JavaMethod to be analyzed.
	 * @param classPath path annotated on class level.
	 * @return Pair containing path and method. Null, if there is no path/method.
	 */
	private Pair<String, HttpMethods> getMethodAnnotations(JavaMethod method, String classPath) {
		String path = formatConcatPath(classPath);
		boolean pathWasChanged = false;
		HttpMethods meth = null;
		for (JavaAnnotation annotation : method.getAnnotations()) {
			String annotationClass = annotation.getType().getCanonicalName();

			if (HTTP_METHODS.contains(annotationClass)) {
				meth = extractHttpMethod(annotationClass);
			} else if (annotationClass.equals(Path.class.getCanonicalName())) {
				path += annotation.getNamedParameter("value") == null ? ""
						: formatConcatPath(annotation.getNamedParameter("value").toString());
				path = setTypeInPath(method, path);
				pathWasChanged = true;
			}
		}

		if (meth != null) {
			return new Pair<String, HttpMethods>(path, meth);
		} else if (pathWasChanged) { // && meth.isEmpty()
			// TODO find subresource (class that is returned by the method and is not
			// annotated with @Path on classlevel) and get the methods there. Problem: could
			// return Object -> any resource possible. would need to read code -> ?
		}
		return null;

	}

	private String setTypeInPath(JavaMethod method, String path) {
		String newPath = removeRegularExpressionsFromPath(path);
		for (JavaParameter param : method.getParameters()) {
			for (JavaAnnotation annotation : param.getAnnotations()) {
				if (annotation.getType().getCanonicalName().equalsIgnoreCase(PathParam.class.getCanonicalName())) {
					String paramName = annotation.getNamedParameter("value") == null ? param.getName()
							: annotation.getNamedParameter("value").toString();
					paramName = paramName.replaceAll("\"", "");
					if (path.contains("{" + paramName + "}")) {
						newPath = newPath.replace("{" + paramName + "}",
								"{" + param.getJavaClass().getSimpleName().toUpperCase(Locale.ROOT) + "}");
					}
				}
			}
		}
		return newPath;
	}

	private String removeRegularExpressionsFromPath(String path) {
		return path.replaceAll(":[^}]*}", "}");
	}

	private HttpMethods extractHttpMethod(String annotationClass) {
		int index = HTTP_METHODS.indexOf(annotationClass);
		return HttpMethods.values()[index]; // get(index);
	}

	/**
	 * Concatenates the given applicationPath to the paths of the given pairs. If
	 * the application path is null, the given path of the pair is formatted as base
	 * path.
	 * 
	 * @param paths           List of pairs. The applicationPath is to be added to
	 *                        the left side of the pair.
	 * @param applicationPath String containing the path given as path of the
	 *                        application. Can be null.
	 * @return List of pairs onto which the application path was concatenated.
	 */
	private List<OfferDescription> concatApplicationPathTo(Collection<OfferDescription> offerDescriptions,
			String applicationPath) {

		List<OfferDescription> returnOffers = new ArrayList<>();

		if (applicationPath != null) {
			applicationPath = formatBasePath(applicationPath);

			for (OfferDescription offer : offerDescriptions) {
				OfferDescription newOffer = new OfferDescription();
				newOffer.setPackageName(offer.getPackageName());
				for (Entry<String, Set<HttpMethods>> entry : offer.getPathToMethodMappings().entrySet()) {
					for (HttpMethods meth : entry.getValue()) {
						Pair<String, HttpMethods> longPathPair = new Pair<>(
								applicationPath + formatConcatPath(entry.getKey()), meth);
						newOffer.addPathToMethod(longPathPair);
					}
				}
				returnOffers.add(newOffer);
			}
		} else {
			for (OfferDescription offer : offerDescriptions) {
				OfferDescription newOffer = new OfferDescription();
				newOffer.setPackageName(offer.getPackageName());
				for (Entry<String, Set<HttpMethods>> entry : offer.getPathToMethodMappings().entrySet()) {
					for (HttpMethods meth : entry.getValue()) {
						Pair<String, HttpMethods> longPathPair = new Pair<>(
								formatBasePath(entry.getKey()), meth);
						newOffer.addPathToMethod(longPathPair);
					}
				}
				returnOffers.add(newOffer);
			}
		}
		return returnOffers;
	}

	/**
	 * Searches for the web.xml of the application. If there is one (either given
	 * through plug-in parameter or in the resources of the project), the file is
	 * parsed and the 'url-pattern' is read.
	 * 
	 * @return String containing the value of 'url-pattern', null if no web.xml was
	 *         found or value couldn't be read.
	 */
	private String readPathFromWebXML() {
		File configFile = null;
		if (apiConfigFile != null) {
			configFile = apiConfigFile;
		} else {
			;
			try (Stream<java.nio.file.Path> fileStream = Files.find(Paths.get(project.getBasedir().getAbsolutePath()),
					6, (p, a) -> p.endsWith("web.xml") && a.isRegularFile())) {
				Optional<java.nio.file.Path> path = fileStream.findFirst();
				if (path.isPresent()) {
					configFile = path.get().toFile();
				} else {
					log.info("No web.xml found.");
					return null;
				}
				fileStream.close();
			} catch (IOException e) {
				log.error("Error searching for web.xml. " + e.getMessage());
				return null;
			}
		}

		return readUrlPattern(configFile, "url-pattern", "servlet-mapping");
	}

	/**
	 * Searches for the glassfish-web.xml of the application. If there is one
	 * (either given through plug-in parameter or in the resources of the project),
	 * the file is parsed and the 'url-pattern' is read.
	 * 
	 * @return String containing the value of 'url-pattern', null if no
	 *         glassfish-web.xml was found or value couldn't be read.
	 */
	private String readPathFromGlassfishWebXML() {
		File configFile = null;
		try (Stream<java.nio.file.Path> fileStream = Files.find(Paths.get(project.getBasedir().getAbsolutePath()), 6,
				(p, a) -> p.endsWith("glassfish-web.xml") && a.isRegularFile())) {

			Optional<java.nio.file.Path> path = fileStream.findFirst();
			if (path.isPresent()) {
				configFile = path.get().toFile();
			} else {
				log.info("No glassfish-web.xml found.");
				System.out.println(Paths.get(project.getBasedir().getAbsolutePath()).toString());
				return null;
			}
			fileStream.close();
		} catch (IOException e) {
			log.error("Error searching for glassfish-web.xml. " + e.getMessage());
			return null;
		}

		return readUrlPattern(configFile, "context-root", "glassfish-web-app");
	}

	/**
	 * Searches for the url-pattern element in the given file. File should be xml.
	 * 
	 * @param xmlFile file being parsed.
	 * @return the value found or null.
	 */
	private String readUrlPattern(File xmlFile, String tagName, String parentName) {
		if (xmlFile == null) {
			return null;
		}
		String basePath = "";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e1) {
			factory.setValidating(false);
			System.err.println("Could not stop external dtd loading");
		}
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlFile);
			NodeList urls = doc.getElementsByTagName(tagName);

			for (int i = 0; i < urls.getLength(); i++) {
				if (urls.item(i).getParentNode().getNodeName().equals(parentName)) {
					basePath = urls.item(i).getTextContent();
					return basePath;
				}
			}

		} catch (IOException e) {
			log.error("Error reading file." + e.getMessage());
		} catch (ParserConfigurationException e) {
			log.error("Parser error while reading web.xml: " + e.getMessage());
		} catch (SAXException e) {
			log.error("Error while parsing web.xml: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Formats the given String to /path by trimming, removing " and making sure,
	 * that it starts with a slash and ends without a slash or star.
	 * 
	 * @param basePath path to be transformed.
	 * @return transformed String
	 */
	private String formatBasePath(String basePath) {
		basePath = basePath.trim();
		basePath = basePath.replace("\"", "");
		if (basePath.endsWith("*")) {
			basePath = basePath.substring(0, basePath.length() - 2);
		}
		if (!basePath.startsWith("/")) {
			basePath = "/" + basePath;
		}
		if (basePath.endsWith("/")) {
			basePath = basePath.substring(0, basePath.length() - 2);
		}
		return basePath;
	}

	/**
	 * Formats the given String to /path by trimming, removing " and making sure,
	 * that it starts with a slash and ends without a slash.
	 * 
	 * @param concatPathpath to be transformed.
	 * @return transformed String
	 */
	private String formatConcatPath(String concatPath) {
		concatPath = concatPath.trim();
		concatPath = concatPath.replace("\"", "");
		if (!concatPath.startsWith("/")) {
			concatPath = "/" + concatPath;
		}
		if (concatPath.endsWith("/")) {
			concatPath = concatPath.substring(0, concatPath.length() - 2);
		}
		return concatPath;
	}

}
