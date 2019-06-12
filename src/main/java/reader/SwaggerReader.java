package reader;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

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

	@Override
	public List<Pair<String, String>> getPathsAndMethods(File src) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	private String getSwaggerFilePath() {
//		String location = "";
//		Object domObject = project.getPlugin("io.swagger.core.v3:swagger-maven-plugin").getConfiguration();
//
//		if (domObject instanceof Xpp3Dom) {
//			Xpp3Dom docLocationChild = ((Xpp3Dom) domObject).getChild("outputPath");
//			if (docLocationChild != null) {
//				location += docLocationChild.getValue();
//			}
//			docLocationChild = ((Xpp3Dom) domObject).getChild("outputFileName");
//			if (docLocationChild != null) {
//				location += docLocationChild.getValue();
//			}
//			docLocationChild = ((Xpp3Dom) domObject).getChild("outputFormat");
//			if (docLocationChild != null) {
//				switch (docLocationChild.getValue()) {
//				case "YAML":
//					location += ".yaml";
//					break;
//				default:
//					location += ".json";
//					break;
//				}
//			} else {
//				location += ".json";
//			}
//
//		}
//
//		return location;
//	}

}
