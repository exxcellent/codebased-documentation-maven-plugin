package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class containing info needed for describing a consumption of a REST-API.
 * 
 * @author gmittmann
 *
 */
public class ConsumeDescription {

	private String serviceName;
	private String packageName;
	private Map<String, Set<String>> pathToMethods;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Map<String, Set<String>> getPathToMethods() {
		return pathToMethods;
	}

	public void setPathToMethods(Map<String, Set<String>> pathToMethods) {
		this.pathToMethods = pathToMethods;
	}

	public void addPathToMethod(String path, Set<String> methods) {
		if (pathToMethods == null || pathToMethods.isEmpty()) {
			this.pathToMethods = new HashMap<>();
			this.pathToMethods.put(path, methods);
		} else {
			if (this.pathToMethods.containsKey(path)) {
				this.pathToMethods.get(path).addAll(methods);
			} else {
				this.pathToMethods.put(path, methods);
			}
		}
	}
}
