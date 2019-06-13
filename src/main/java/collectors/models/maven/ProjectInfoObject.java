package collectors.models.maven;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;

import collectors.models.InfoObject;

public class ProjectInfoObject extends InfoObject{

	private String projectName;
	private String groupId;
	private String artifactId;
	private String version;
	private String type;
	private String scope;
	private List<Exclusion> exclusions;
	
	public ProjectInfoObject(String name) {
		this.setProjectName(name);
	}
	
	public ProjectInfoObject(String name, String groupId, String artifactId, String version, String type, String scope, List<Exclusion> exclusions) {
		this.setProjectName(name);
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.type = type;
		this.scope = scope;
		this.exclusions = exclusions;
	}
	
	public ProjectInfoObject(Dependency dependency) {
		this.setProjectName(dependency.getManagementKey());
		this.groupId = dependency.getGroupId();
		this.artifactId = dependency.getArtifactId();
		this.version = dependency.getVersion();
		this.type = dependency.getType();
		this.scope = dependency.getScope();
		this.exclusions = dependency.getExclusions();
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public List<Exclusion> getExclusions() {
		return exclusions;
	}

	public void setExclusions(List<Exclusion> exclusions) {
		this.exclusions = exclusions;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
