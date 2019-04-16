package collectors.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

public class MavenInfoObject extends InfoObject{
	
	private String artifactID;
	private String groupID;
	private String version;
	private String tag;
	
	private List<DependencyInfoObject> dependencies;
	private List<String> dependsOn;
	
	public MavenInfoObject(String name, String artifactID, String groupId, String version, String tag, List<DependencyInfoObject> dependencies, List<String> dependsOn) {
		super(name);
		this.artifactID = artifactID;
		this.groupID = groupId;
		this.version = version;
		this.tag = tag;
		this.dependencies = dependencies;
		this.dependsOn = dependsOn;
	}

	public MavenInfoObject(MavenProject project, List<DependencyInfoObject> dependencies, List<String> dependsOn) {
		super(project.getName());
		this.artifactID = project.getArtifactId();
		this.groupID = project.getGroupId();
		this.version = project.getVersion();
		this.tag = this.groupID + ":" + this.artifactID + ":" + this.version;
		this.dependencies = dependencies;
		this.dependsOn = dependsOn;
	}
	
	public String getArtifactID() {
		return artifactID;
	}
	
	public void setArtifactID(String artifactID) {
		this.artifactID = artifactID;
	}
	
	public String getGroupID() {
		return groupID;
	}
	
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public List<DependencyInfoObject> getDependencies() {
		return dependencies;
	}
	
	public void setDependencies(List<DependencyInfoObject> dependencies) {
		this.dependencies = dependencies;
	}
	
	public void addDependency(DependencyInfoObject dependency) {
		if(dependencies == null) {
			this.dependencies = new ArrayList<DependencyInfoObject>();
		}
		this.dependencies.add(dependency);
	}
	
	public List<String> getDependsOn() {
		return dependsOn;
	}
	
	public void setDependsOn(List<String> dependsOn) {
		this.dependsOn = dependsOn;
	}
	
	public void addDependsOn(String dependsOn) {
		if(dependsOn == null) {
			this.dependsOn = new ArrayList<String>();
		}
		this.dependsOn.add(dependsOn);
	}

}
