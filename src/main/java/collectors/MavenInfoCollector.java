package collectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.json.JSONObject;

import filemanagement.FileWriter;

/**
 * Collects info available from Maven (currently: Modules and their dependence
 * onto each other)
 * 
 * @author gmittmann
 *
 */
public class MavenInfoCollector implements InformationCollector {

	private MavenProject project;
	private Log log;
	private FileWriter out;

	public static final String FILE_NAME = "mavenInformation";

	public MavenInfoCollector(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
		this.out = new FileWriter(log);
	}

	public MavenInfoCollector(MavenProject project, Log log, FileWriter fileWriter) {
		this.project = project;
		this.log = log;
		this.out = fileWriter;
	}

	/**
	 * Collects the information about the current project and its dependencies and
	 * stores them into a file in the target directory
	 */
	public void collectInfo() {
		log.info("--COLLECTING MAVEN INFO--");
		log.info("ExecProject: " + project.getExecutionProject().getArtifactId());

		String dirPath = project.getBasedir() + "\\target\\" + FOLDER_NAME;
		if (out.createFile(dirPath, FILE_NAME)) {
			
			JSONObject json = new JSONObject();
			json.put("name", project.getName());
			json.put("artifactId", project.getArtifactId());
			json.put("groupId", project.getGroupId());
			json.put("dependencies", project.getDependencies());
			
			List<String> moduleDependency = new ArrayList<>();
			for (Dependency dependency : project.getDependencies()) {
				// TODO check via artifact id against modules
				if (dependency.getGroupId().equals(project.getGroupId())) {
					moduleDependency.add(dependency.getGroupId() + ":" + dependency.getArtifactId());
				}
			}
			json.put("moduleDependencies", moduleDependency);
			
			out.writeJSONObjectIntoFile(json);
			out.finishFile();
		} else {
			log.error("path that failed: " + dirPath + "\\" + FILE_NAME);
		}

	}
}
