package collectors.models.maven;

import java.util.Set;

import collectors.models.InfoObject;

public class PackageInfoObject extends InfoObject {
	
	private Set<String> dependsOn;

	public PackageInfoObject(String name) {
		super(name);
	}

	public Set<String> getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(Set<String> dependsOn) {
		this.dependsOn = dependsOn;
	}

}
