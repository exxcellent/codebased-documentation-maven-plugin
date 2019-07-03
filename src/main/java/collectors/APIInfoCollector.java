package collectors;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import collectors.models.AnnotationType;
import collectors.models.restapi.APIConsumptionInfoObject;
import collectors.models.restapi.APIInfoObject;
import filemanagement.FileWriter;
import reader.impl.api.JAXRSReader;
import reader.impl.api.SPRINGReader;
import reader.impl.api.SwaggerReader;
import reader.impl.apiconsumption.AnnotationReader;
import reader.impl.apiconsumption.ApacheCommonsClientReader;
import reader.interfaces.APIReader;
import reader.interfaces.ConsumesAPIReader;
import util.ConsumeDescription;
import util.HttpMethods;
import util.OfferDescription;
import util.Pair;

public class APIInfoCollector implements InformationCollector {

	private MavenProject project;
	private Log log;
	private AnnotationType type;
	private List<String> swaggerFilePaths;
	private File apiConfigFilePath;
	private String contextPath;

	public static final String FILE_NAME = "apiInformation";
	public static final String FILE_NAME_CONSUME = "apiConsumption";

	public APIInfoCollector(MavenProject project, Log log, AnnotationType type, List<String> swaggerFilePaths,
			File apiConfigFilePath, String contextPath) {
		this.project = project;
		this.log = log;
		this.type = type;
		this.swaggerFilePaths = swaggerFilePaths;
		this.apiConfigFilePath = apiConfigFilePath;
		this.contextPath = contextPath;
	}

	@Override
	public void collectInfo() {
		log.info("  -- COLLECTING REST API INFO --");

		String dirPath = Paths.get(project.getBasedir().getAbsolutePath(), "target", FOLDER_NAME).toString();
		log.info("target folder: " + dirPath);
		log.info("target file: " + FILE_NAME);

		APIInfoObject infoObject = generateAPIInfo();
		APIConsumptionInfoObject consumeInfoObject = generateAPIConsumptionInfo();
		FileWriter.writeInfoToJSONFile(dirPath, FILE_NAME, infoObject, log);
		FileWriter.writeInfoToJSONFile(dirPath, FILE_NAME_CONSUME, consumeInfoObject, log);

	}

	private APIInfoObject generateAPIInfo() {
		APIReader apiReader;
		switch (type) {
		case JAXRS:
			apiReader = new JAXRSReader(project, log, apiConfigFilePath, contextPath);
			log.info("Using Jax-RS");
			break;
		case SWAGGER_FILE:
			if (swaggerFilePaths != null && !swaggerFilePaths.isEmpty()) {
				apiReader = new SwaggerReader(project, log, swaggerFilePaths, apiConfigFilePath);
				log.info("Using Swagger files");
				break;
			} else {
				log.error("AnnotationType was SWAGGER, but no location of Files was given.");
				log.error("Continues by trying to find Spring Boot annotations.");
			}
		default: // SPRING
			apiReader = new SPRINGReader(log, apiConfigFilePath, contextPath);
			log.info("Using Spring Boot");
		}

		List<OfferDescription> mappings = apiReader.getPathsAndMethods(project.getBasedir());

		APIInfoObject infoObject = new APIInfoObject(getServiceTag()); // TODO: NAME?!
//		for (Pair<String, HttpMethods> mapping : mappings) {
//			infoObject.addMethod(mapping.getLeft(), mapping.getRight().name());
//		}
		infoObject.setApi(mappings);
		return infoObject;
	}

	private APIConsumptionInfoObject generateAPIConsumptionInfo() {
		APIConsumptionInfoObject infoObject = new APIConsumptionInfoObject();
		infoObject.setMicroserviceName(getServiceTag());

		ConsumptionInfo location = getInfoLocation();

		ConsumesAPIReader reader = null;
		switch (location) {
		case apache:
			log.info("Clients: Apache Commons Client.");
			reader = new ApacheCommonsClientReader();
			break;
		case annotation:
			log.info("Clients: none -> annotations");
			reader = new AnnotationReader();
			break;
		case restTemplate:
			log.info("Clients: Spring Boot Rest Template.");
			reader = null;
			break;
		default:
			log.error("Could not determine Http client type -> Using annotations.");
			reader = new AnnotationReader();
		}
		List<ConsumeDescription> triples = reader.getAPIConsumption(project.getBasedir());
		for (ConsumeDescription triple : triples) {
//			infoObject.addServiceToPathToMethod(triple);
			infoObject.addConsumeDescriptionTriple(triple);
		}
		return infoObject;
	}

	private String getServiceTag() {
		String tag = "";
		MavenProject execProject = project.getExecutionProject();

		tag += execProject.getGroupId() + ":";
		tag += execProject.getArtifactId() + ":";
		tag += execProject.getVersion();

		return tag;
	}

	private ConsumptionInfo getInfoLocation() {
		//TODO: check POM whether there are dependencies to the clients.
		return ConsumptionInfo.annotation;
	}

	private enum ConsumptionInfo {
		annotation, apache, restTemplate
	};

}
