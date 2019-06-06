package mojos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import collectors.ModuleInfoCollector;
import collectors.models.AnnotationType;
import collectors.APIInfoCollector;
import collectors.ComponentInfoCollector;
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

	@Parameter(property = "session", defaultValue = "${session}", required = true)
	private MavenSession session;

	@Parameter(property = "packageWhiteList")
	private Map<String, Integer> packageWhiteList;

	@Parameter(property = "packageBlackList")
	private Set<String> packageBlackList;

	@Parameter(property = "system", defaultValue = "default_system")
	private String system;

	@Parameter(property = "subsystem", defaultValue = "default_subsystem")
	private String subsystem;
	
	@Parameter(property = "annotation", defaultValue = "SPRING")
	private AnnotationType annotationType;
	
	@Parameter(property = "swaggerFilePaths")
	private List<String> swaggerFilePaths;

	public static final String MAVEN_AGGREGATE_NAME = "mavenInfo";
	public static final String API_AGGREGATE_NAME = "apiInfo";
	public static final String SUFFIX = "ALL";

	public void execute() throws MojoExecutionException, MojoFailureException {

		/* Collect Info */
		if (!project.getPackaging().equals("pom")) {
			ModuleInfoCollector mavenInfoCollector = new ModuleInfoCollector(project, session, getLog());
			mavenInfoCollector.collectInfo();

			ComponentInfoCollector packageInfoCollector = new ComponentInfoCollector(packageWhiteList, packageBlackList,
					project, getLog());
			packageInfoCollector.collectInfo();
			
			APIInfoCollector apiInfoCollector = new APIInfoCollector(project, getLog(), annotationType, swaggerFilePaths);
			apiInfoCollector.collectInfo();
		} else {
			getLog().info("Skipping data collection: pom");
		}

		

		/* If this is the last project/module start file aggregation */
		List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();
		if (sortedProjects.get(sortedProjects.size() - 1).equals(project)) {
			getLog().info("  -- AGGREGATING FILES --");
			setDocumentLocation();
			FileAggregator aggregator = new FileAggregator(session, getLog());
			aggregator.aggregateMavenFilesTo(documentLocation, SUFFIX);
			
		}

	}


	/**
	 * Sets the location to which the aggregated file will be written to. If the
	 * current project is not the executionRoot, the parameter is searched in the
	 * pom of the execution project. If the current project is the executionRoot,
	 * the defined value is used, if available, else the default value is applied.
	 */
	private void setDocumentLocation() {
		MavenProject root = session.getTopLevelProject();
		String subfolder = (root.getGroupId() + "_" + root.getArtifactId() + "_" + root.getVersion()).replaceAll(".,:", "_");
		
		
		if (!project.isExecutionRoot()) {
			setDocumentLocationByExecutionRoot();
		}
		/*
		 * If the documentLocation is still null, it was not set in the execution pom.
		 * Set default value
		 */
		if (documentLocation == null) {
			setDocumentLocationToDefault();
		}
		
		documentLocation = Paths.get(documentLocation.getAbsolutePath(), subfolder).toFile();
		
	}

	/**
	 * Sets the documentLocation to the value defined in th eexecution root project.
	 * If there is no value set there or the value is not a valid path, the document
	 * location is set to null.
	 */
	private void setDocumentLocationByExecutionRoot() {
		/* check value in aggregator pom and overwrite, if there is a value */
		MavenProject root = session.getTopLevelProject();
		for (MavenProject prj : session.getAllProjects()) {
			if (prj.isExecutionRoot()) {
				root = prj;
			}
		}
		String pathInAggregatorPom = extractDocumentLocationFromConfigurationDOM(
				root.getPlugin("codebased-documentation:cd-maven-plugin").getConfiguration());

		if (pathInAggregatorPom != null && !pathInAggregatorPom.isEmpty()) {
			try {
				documentLocation = Paths.get(pathInAggregatorPom).toFile();
				getLog().info("Documentation location set to: " + documentLocation.getAbsolutePath());
			} catch (InvalidPathException e) {
				getLog().error("documentLocation defined in the top level project can't be converted to a path.");
				documentLocation = null;
			}
		} else {
			documentLocation = null;
		}
	}

	/**
	 * Sets the documentLocation to the default value of
	 * executionRootDirectory/documentation and tries to create the needed
	 * directories for this. If this fails, the document location is set to the
	 * execution root directory.
	 */
	private void setDocumentLocationToDefault() {
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
