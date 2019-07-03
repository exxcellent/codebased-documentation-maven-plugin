package reader.impl.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.emory.mathcs.backport.java.util.Arrays;
import reader.interfaces.APIReader;
import util.HttpMethods;
import util.OfferDescription;
import util.Pair;

public class SwaggerReader implements APIReader {
	
	private MavenProject project;
	private Log log;
	private List<String> swaggerFilePaths;
	private File apiConfigFile;
	
	public SwaggerReader(MavenProject project, Log log, List<String> swaggerFilePaths, File apiConfigFile) {
		this.project = project;
		this.log = log;
		this.swaggerFilePaths = swaggerFilePaths;
		this.apiConfigFile = apiConfigFile;
	}

	/**
	 * src is dir in which the files are to be searched for.
	 */
	@Override
	public List<OfferDescription> getPathsAndMethods(File src) {
		
		evaluateConfigFile();
		
		// TODO Auto-generated method stub
		List<Path> swaggerFiles = new ArrayList<>();
		if (swaggerFilePaths == null || swaggerFilePaths.isEmpty()) {
			swaggerFiles = getSwaggerFiles(src);
		} else {
			for(String file : swaggerFilePaths) {
				File currentFile = Paths.get(file).toFile();
				if (currentFile.exists() && isSwaggerFile(currentFile)) {
					swaggerFiles.add(currentFile.toPath());
				} else if (currentFile.isDirectory()) {
					swaggerFiles.addAll(getSwaggerFiles(currentFile));
				}
			}
		}
		
		return turnToMapping(swaggerFiles);
	}
	
	private void evaluateConfigFile() {
		if (apiConfigFile == null || !apiConfigFile.exists()) {
			apiConfigFile = checkForDefaultConfigFile();
			if (apiConfigFile == null) {
				return;
			}
		}
		
				
		//TODO: read relevant info from swagger config file.
	}
	
	private File checkForDefaultConfigFile() {
		// TODO swagger-config.yaml in project root.
		
		try (Stream<Path> stream = Files.walk(project.getBasedir().toPath(), 3,
				FileVisitOption.FOLLOW_LINKS)) {
			Optional<Path> path = stream.filter(p -> p.endsWith("swagger-config.yaml")).findFirst();
			if (path.isPresent()) {
				return path.get().toFile();
			} else {
				return null;
			}
			
		} catch (IOException e) {
			System.out.println("Error while searching for default config file");
			System.out.println(e.getMessage());
		}
		return null;
	}

	private boolean isSwaggerFile(File file) {
		
		String fileName = file.getName();
		if (fileName.endsWith("swagger.json") || fileName.endsWith("swagger.yml") || fileName.endsWith("swagger.yaml")) {
			return true;
		}
		return false;
	}
	
	private List<Path> getSwaggerFiles(File dir) {
		List<Path> returnFiles = new ArrayList<>();
		if (!dir.isDirectory()) {
			return returnFiles;
		}
		try (Stream<Path> stream = Files.walk(dir.toPath(), 5,
				FileVisitOption.FOLLOW_LINKS)) {
			returnFiles = stream.filter(p -> isSwaggerFile(p.toFile())).collect(Collectors.toList());
									
		} catch (IOException e) {
			System.out.println("Error while searching for default config file");
			System.out.println(e.getMessage());
		}
		return returnFiles;
	}
	
	private List<OfferDescription> turnToMapping(List<Path> swaggerFiles) {
		
		List<OfferDescription> offers = new ArrayList<>();
		List<Pair<String, HttpMethods>> mapping = new ArrayList<>();
		
		for (Path currentPath : swaggerFiles) {
			Map<String, Object> fileMap = new HashMap<>();
			if (currentPath.endsWith("swagger.yaml") || currentPath.endsWith("swagger.yml")) {
				Yaml yaml = new Yaml();
				try (InputStream in = new FileInputStream(currentPath.toFile())) {
					fileMap = yaml.load(in);
					in.close();
				} catch (IOException e) {
					log.error("Could not parse swagger file " + currentPath.toString() + " as yaml");
					log.error(e.getMessage());
				}
				
			} else if (currentPath.endsWith("swagger.json")) {
				Gson gson = new Gson();				
				try(FileReader reader = new FileReader(currentPath.toFile())) {
					Type type = new TypeToken<Map<String, Object>>(){}.getType();
					fileMap = gson.fromJson(reader, type);
					reader.close();
				} catch (IOException e) {
					log.error("Could not parse swagger file " + currentPath.toString() + " as json");
					log.error(e.getMessage());
				}
			}
			
			if (fileMap == null || fileMap.isEmpty()) {
				return offers;
			}
			
			String pathBase = "";
			// check for content
			if (fileMap.get("basePath") != null) {
				String basePath = fileMap.get("basePath").toString();
				String[] splitPath = basePath.split("/");

				if (splitPath.length >= 4) {
					pathBase += String.join("/", (CharSequence[]) Arrays.copyOfRange(splitPath, 3, splitPath.length - 1));
					pathBase += "/";
				}
			}
			
			if (fileMap.get("resourcePath") != null) {
				pathBase += fileMap.get("resourcePath").toString();
			}
			
			if (fileMap.get("apis") != null) {
				
				
			} else {
				return offers;
			}
			
		}
		return null;
	}
}
