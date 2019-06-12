package reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import java.util.Arrays;
import util.Pair;

public class JAXRSReader implements APIReader {

	private static final List<String> HTTP_METHODS = Arrays.asList(GET.class.getSimpleName(), PUT.class.getSimpleName(),
			POST.class.getSimpleName(), DELETE.class.getSimpleName(), HEAD.class.getSimpleName(),
			OPTIONS.class.getSimpleName());

	private MavenProject project;
	private Log log;
	private File apiConfigFile;

	public JAXRSReader(MavenProject project, Log log, File apiConfigFile) {
		this.project = project;
		this.log = log;
		this.apiConfigFile = apiConfigFile;
	}

	@Override
	public List<Pair<String, String>> getPathsAndMethods(File src) {

		JavaProjectBuilder builder = new JavaProjectBuilder();
		builder.addSourceTree(src);
		String applicationPath = null;

		List<Pair<String, String>> paths = new ArrayList<>();

		for (JavaClass currentClass : builder.getClasses()) {
			for (JavaAnnotation annotation : currentClass.getAnnotations()) {
				if (annotation.getType().getSimpleName().equals(ApplicationPath.class.getSimpleName())) {
					applicationPath = annotation.getNamedParameter("value").toString();
				} else if (annotation.getType().getSimpleName().equals(Path.class.getSimpleName())) {
					String classPath = annotation.getNamedParameter("value") == null ? ""
							: annotation.getNamedParameter("value").toString();

					for (JavaMethod method : currentClass.getMethods()) {
						Pair<String, String> methodPair = getMethodAnnotations(method, classPath);
						if (methodPair != null) {
							paths.add(methodPair);
						}
					}
				}
			}
		}

		if (applicationPath == null) {
			applicationPath = readPathFromWebXML();
		}

		return concatApplicationPathTo(paths, applicationPath);
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
	private Pair<String, String> getMethodAnnotations(JavaMethod method, String classPath) {
		String path = formatConcatPath(classPath);
		boolean pathWasChanged = false;
		String meth = "";
		for (JavaAnnotation annotation : method.getAnnotations()) {
			String annotationClass = annotation.getType().getSimpleName();

			if (HTTP_METHODS.contains(annotationClass)) {
				meth = extractHttpMethod(annotationClass);
			} else if (annotationClass.equals(Path.class.getSimpleName())) {
				path += annotation.getNamedParameter("value") == null ? ""
						: formatConcatPath(annotation.getNamedParameter("value").toString());
				pathWasChanged = true;
			}
		}

		if (!meth.isEmpty()) {
			return new Pair<String, String>(path, meth);
		} else if (pathWasChanged) { // && meth.isEmpty()
			// TODO find subresource (class that is returned by the method and is not
			// annotated with @Path on classlevel) and get the methods there. Problem: could
			// return Object -> any resource possible. would need to read code -> ?
		}
		return null;

	}

	private String extractHttpMethod(String annotationClass) {
		int index = HTTP_METHODS.indexOf(annotationClass);
		return HTTP_METHOD_TYPE.get(index);
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
	private List<Pair<String, String>> concatApplicationPathTo(List<Pair<String, String>> paths, String applicationPath) {
		List<Pair<String, String>> returnPaths = new ArrayList<>();
		if (applicationPath != null) {
			applicationPath = formatBasePath(applicationPath);
			for (Pair<String, String> currentPair : paths) {
				Pair<String, String> longPathPair = new Pair<>(
						applicationPath + formatConcatPath(currentPair.getLeft()), currentPair.getRight());
				returnPaths.add(longPathPair);
			}
		} else {
			for (Pair<String, String> currentPair : paths) {
				Pair<String, String> longPathPair = new Pair<>(formatBasePath(currentPair.getLeft()),
						currentPair.getRight());
				returnPaths.add(longPathPair);
			}
		}
		return returnPaths;
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
			try (Stream<java.nio.file.Path> fileStream = Files.walk(Paths.get(project.getBasedir().getAbsolutePath()),
					4, FileVisitOption.FOLLOW_LINKS)) {
				Optional<java.nio.file.Path> path = fileStream.filter(p -> p.endsWith("web.xml")).findFirst();
				if (path.isPresent()) {
					configFile = path.get().toFile();
				} else {
					log.info("No web.xml found.");
					return null;
				}
			} catch (IOException e) {
				log.error("Error searching for web.xml. " + e.getMessage());
				return null;
			}
		}

		return readUrlPattern(configFile);
	}

	/**
	 * Searches for the url-pattern element in the given file. File should be xml.
	 * 
	 * @param xmlFile file being parsed.
	 * @return the value found or null.
	 */
	private String readUrlPattern(File xmlFile) {
		if (xmlFile == null) {
			return null;
		}
		String basePath = "";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlFile);
			NodeList urls = doc.getElementsByTagName("url-pattern");
			if (urls != null && urls.getLength() != 0) {
				basePath = urls.item(0).getTextContent();
				if (basePath != null) {
					basePath = formatBasePath(basePath);
				}

				return basePath;
			}
		} catch (IOException e) {
			log.error("Error searching for web.xml. " + e.getMessage());
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
