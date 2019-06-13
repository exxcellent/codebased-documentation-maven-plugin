package collectors.models.maven;

import java.util.Set;

import collectors.models.InfoObject;

public class PackageInfoObject extends InfoObject {
	
	private String packageName;
	private Set<String> dependsOn;

	public PackageInfoObject(String name) {
		this.setPackageName(name);
	}

	public Set<String> getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(Set<String> dependsOn) {
		this.dependsOn = dependsOn;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
