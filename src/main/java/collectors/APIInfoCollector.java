package collectors;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import collectors.models.AnnotationType;
import collectors.models.restapi.APIInfoObject;
import filemanagement.FileWriter;
import reader.APIReader;
import reader.JAXRSReader;
import reader.SPRINGReader;
import reader.SwaggerReader;
import util.Pair;

public class APIInfoCollector implements InformationCollector {
	
	private MavenProject project;
	private Log log;
	private AnnotationType type;
	private List<String> swaggerFilePaths;
	private File apiConfigFilePath;
	
	public static final String FILE_NAME = "apiInformation";
	
	public APIInfoCollector(MavenProject project, Log log, AnnotationType type, List<String> swaggerFilePaths, File apiConfigFilePath) {
		this.project = project;
		this.log = log;
		this.type = type;
		this.swaggerFilePaths = swaggerFilePaths;
		this.apiConfigFilePath = apiConfigFilePath;
	}

	@Override
	public void collectInfo() {
		log.info("  -- COLLECTING REST API INFO --");
		
		String dirPath = Paths.get(project.getBasedir().getAbsolutePath(), "target", FOLDER_NAME).toString();
		log.info("target folder: " + dirPath);
		log.info("target file: " + FILE_NAME);
		
		APIReader apiReader;
		switch (type) {
		case JAXRS:
			apiReader = new JAXRSReader(project, log, apiConfigFilePath);
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
		default: //SPRING
			apiReader = new SPRINGReader(log, apiConfigFilePath);
			log.info("Using Spring Boot");
		}
		
		List<Pair<String, String>> mappings = apiReader.getPathsAndMethods(project.getBasedir());
		
		APIInfoObject infoObject = new APIInfoObject(project.getExecutionProject().getName()); //TODO: NAME?!
		for (Pair<String, String> mapping : mappings) {
			infoObject.addMethod(mapping.getLeft(), mapping.getRight());
		}
		
		FileWriter.writeInfoToJSONFile(dirPath, FILE_NAME, infoObject, log);

	}

}
