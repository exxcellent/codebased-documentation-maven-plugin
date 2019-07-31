package collectors.models.maven;

import java.util.Set;

import collectors.models.InfoObject;

/**
 * Class containing data about a component: it's name and on which component it
 * depeds on.
 * 
 * @author gmittmann
 *
 */
public class ComponentInfoObject extends InfoObject {

	private String packageName;
	private Set<String> dependsOn;

	public ComponentInfoObject(String name) {
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
