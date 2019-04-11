package collectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
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
	private MavenSession session;
	private Log log;
	private FileWriter out;

	public static final String FILE_NAME = "mavenInformation";

	public MavenInfoCollector(MavenProject project, MavenSession session, Log log) {
		this.project = project;
		this.session = session;
		this.log = log;
		this.out = new FileWriter(log);
	}

	public MavenInfoCollector(MavenProject project, MavenSession session, Log log, FileWriter fileWriter) {
		this.project = project;
		this.session = session;
		this.log = log;
		this.out = fileWriter;
	}

	/**
	 * Collects the information about the current project and its dependencies and
	 * stores them into a file in the target directory
	 */
	public void collectInfo() {
		log.info("-- COLLECTING MAVEN INFO --");
		log.info("ExecProject: " + project.getExecutionProject().getArtifactId());

		String dirPath = project.getBasedir() + "\\target\\" + FOLDER_NAME;
		if (out.createFile(dirPath, FILE_NAME)) {
			
			JSONObject json = new JSONObject();
			json.put("name", project.getName());
			json.put("artifactId", project.getArtifactId());
			json.put("groupId", project.getGroupId());
			json.put("version", project.getVersion());
			json.put("tagName", project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
			json.put("dependencies", project.getDependencies());
			
			//TODO: Decide: up- or downstream
			List<String> dependsOn = new ArrayList<>();
			for(MavenProject prj : session.getProjectDependencyGraph().getUpstreamProjects(project, false)) {
				if (!prj.isExecutionRoot() && !prj.getPackaging().equalsIgnoreCase("pom")) {
					dependsOn.add((prj.getGroupId() + ":" + prj.getArtifactId() + ":" + prj.getVersion()));
				}
			}
			json.put("dependsOn", dependsOn);
			
			List<String> dependedOnBy = new ArrayList<>();
			for(MavenProject prj : session.getProjectDependencyGraph().getDownstreamProjects(project, false)) {
				if (!prj.isExecutionRoot() && !prj.getPackaging().equalsIgnoreCase("pom")) {
					dependedOnBy.add(prj.getGroupId() + ":" + prj.getArtifactId() + ":" + prj.getVersion());
				}
			}
			json.put("dependedOnBy", dependedOnBy);
			
			out.writeJSONObjectIntoFile(json);
			out.finishFile();
		} else {
			log.error("Failed file creation at: " + dirPath + "\\" + FILE_NAME);
		}

	}
}
