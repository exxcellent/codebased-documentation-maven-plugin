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

	private static final List<String> HTTP_METHODS = Arrays.asList(
			GET.class.getSimpleName(), 
			PUT.class.getSimpleName(),
			POST.class.getSimpleName(), 
			DELETE.class.getSimpleName(),
			HEAD.class.getSimpleName(), 
			OPTIONS.class.getSimpleName());

	private MavenProject project;
	private Log log;

	public JAXRSReader(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
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

		if (applicationPath != null) {
			List<Pair<String, String>> returnPaths = new ArrayList<>();
			for (Pair<String, String> currentPair : paths) {
				Pair<String, String> longPathPair = new Pair<>(applicationPath + currentPair.getLeft(),
						currentPair.getRight());
				returnPaths.add(longPathPair);
			}
			return returnPaths;
		}

		return paths;
	}

	private Pair<String, String> getMethodAnnotations(JavaMethod method, String classPath) {
		String path = classPath;
		String meth = "";
		for (JavaAnnotation annotation : method.getAnnotations()) {
			String annotationClass = annotation.getType().getSimpleName();

			if (HTTP_METHODS.contains(annotationClass)) {
				meth = extractHttpMethod(annotationClass);
			} else if (annotationClass.equals(Path.class.getSimpleName())) {
				path += annotation.getNamedParameter("value") == null ? ""
						: "/" + annotation.getNamedParameter("value").toString();
			}
		}

		if (!meth.isEmpty()) {
			return new Pair<String, String>(path, meth);
		} else { // TODO: check if there was @Path, if there was get subresource for methods.
			return null;
		}
	}

	private String extractHttpMethod(String annotationClass) {
		int index = HTTP_METHODS.indexOf(annotationClass);
		return HTTP_METHOD_TYPE.get(index);
	}

	private String readPathFromWebXML() {
		String basePath = "";

		try (Stream<java.nio.file.Path> fileStream = Files.walk(Paths.get(project.getBasedir().getAbsolutePath()), 4,
				FileVisitOption.FOLLOW_LINKS)) {
			Optional<java.nio.file.Path> path = fileStream.filter(p -> p.endsWith("web.xml")).findFirst();
			if (path.isPresent()) {
				java.nio.file.Path webXmlPath = path.get();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				Document doc = builder.parse(webXmlPath.toFile());
				NodeList urls = doc.getElementsByTagName("url-pattern");
				if (urls != null && urls.getLength() != 0) {
					basePath = urls.item(0).getNodeValue();
					if (basePath.endsWith("*")) {
						basePath = basePath.subSequence(0, basePath.length() - 2).toString();
					}
					return basePath;
				}

			} else {
				log.info("No web.xml found.");
				return null;
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

}
