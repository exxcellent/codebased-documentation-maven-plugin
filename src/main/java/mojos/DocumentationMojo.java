package mojos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import collectors.MavenInfoCollector;
import filemanagement.FileAggregator;

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

		getLog().info(project.isExecutionRoot() ? "ROOT" : "NOT A ROOT");

		/* Collect Info */
		if (!project.getPackaging().equals("pom")) {
			MavenInfoCollector mavenInfoCollector = new MavenInfoCollector(project, session, getLog());
			mavenInfoCollector.collectInfo();
		} else {
			getLog().info("Skipping data collection: pom");
		}

		/* If this is the last project/module start file aggregation */
		List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();
		if (sortedProjects.get(sortedProjects.size() - 1).equals(project)) {
			setDocumentLocation();
			FileAggregator aggregator = new FileAggregator(session, getLog());
			aggregator.aggregateFilesTo(documentLocation, "ALL");
		}

	}

	/**
	 * Sets the location to which the aggregated file will be written to. If the
	 * current project is not the executionRoot, the parameter is searched in the
	 * aggregator pom. If there is no value there, the default value of the root
	 * directory is set. If the current project is the executionRoot, the defined
	 * value is used, if available, else the default value is applied.
	 */
	private void setDocumentLocation() {
		if (!project.isExecutionRoot()) {
			/* check value in aggregator pom and overwrite, if there is a value */
			MavenProject root = session.getTopLevelProject();
			String pathInAggregatorPom = extractDocumentLocationFromConfigurationDOM(
					root.getPlugin("codebased-documentation:cd-maven-plugin").getConfiguration());
			if (pathInAggregatorPom != null && !pathInAggregatorPom.isEmpty()) {
				documentLocation = Paths.get(pathInAggregatorPom).toFile();
				getLog().info("Documentation location set to: " + documentLocation.getAbsolutePath());
			}
		}

		/*
		 * If the documentLocation is still null, it was not set in the aggregator pom.
		 * Set default value
		 */
		if (documentLocation == null) {
			documentLocation = Paths.get(session.getExecutionRootDirectory(), "documentation").toFile();
			try {
				Files.createDirectories(documentLocation.toPath());
			} catch (IOException e) {
				getLog().error(e.getMessage());
				getLog().error("documentation folder could not be created. Document location set to root directory.");
				documentLocation = Paths.get(session.getExecutionRootDirectory()).toFile();
			}
			if (!documentLocation.exists()) {
				documentLocation = Paths.get(session.getExecutionRootDirectory()).toFile();
			}
			if (project.isExecutionRoot()) {
				getLog().info("documentLocation in execution pom undefined");
			}
			getLog().info("Documentation location was set to default: " + documentLocation.getAbsolutePath());
		}

	}

	/**
	 * Tries to extract the content of the documentLocation tag.
	 * 
	 * @return String with value defined in documentLocationTag. Null if not
	 *         defined.
	 */
	private String extractDocumentLocationFromConfigurationDOM(Object domObject) {
		if (domObject instanceof Xpp3Dom) {
			Xpp3Dom docLocationChild = ((Xpp3Dom) domObject).getChild("documentLocation");
			if (docLocationChild != null) {
				return docLocationChild.getValue();
			}
		}

		return null;
	}

}
