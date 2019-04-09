package mojos;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import collectors.MavenInfoCollector;
import creators.FileAggregator;

/**
 * Mojo for creating the documentation of the project in which it is running.
 * Currently only documentation about Modules.
 * 
 * @author gmittmann
 *
 */
@Mojo(name = "generateDoc")
public class DocumentationMojo extends AbstractMojo {

	@Parameter(property = "documentLocation")
	private File documentLocation;

	@Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(property = "session", defaultValue = "${session}")
	private MavenSession session;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// Set aggregated file location
		// TODO: check location set by aggregator pom
		setDocumentLocation();

		if (project == null) {
			getLog().error("Variable ${project} is null!");
		} else {
			getLog().info("Variable ${project} is filled!");
			getLog().info(project.isExecutionRoot() ? "FOUND THE ROOT" : "NOT A ROOT");

			MavenInfoCollector mavenInfoCollector = new MavenInfoCollector(project, getLog());
			mavenInfoCollector.collectInfo();

			List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();
			if (sortedProjects.get(sortedProjects.size() - 1).equals(project)) {
				getLog().info("SURPRISE");
				FileAggregator aggregator = new FileAggregator(project, getLog());
				aggregator.aggregateFilesTo(documentLocation, "ALL.txt");
			}

		}
	}

	/**
	 * Sets the location to which the aggregated file will be written to. If it is
	 * not set and the current project is not the executionRoot, the parameter is
	 * searched in the aggregator pom. If there is no value there, the default value
	 * of the root directory is set.
	 */
	private void setDocumentLocation() {
		if (documentLocation == null && project.isExecutionRoot()) {
			documentLocation = new File(session.getExecutionRootDirectory() /* + "\\target\\documentation" */);
			getLog().info("path: " + documentLocation.getAbsolutePath());
			return;
		} else if (!project.isExecutionRoot()) {

			// check if target dir is defined in executionRoot pom
			// else set default
		}
		documentLocation = new File(session.getExecutionRootDirectory());

	}

}
