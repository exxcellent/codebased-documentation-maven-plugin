package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Class containing all info needed to describe REST API
 * @author gesam
 *
 */
public class OfferDescription {
	
	private String packageName;
	private Map<String, Set<HttpMethods>> pathToMethodMappings;
	
	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Map<String, Set<HttpMethods>> getPathToMethodMappings() {
		return pathToMethodMappings;
	}

	public void setPathToMethodMappings(Map<String, Set<HttpMethods>> pathToMethodMappings) {
		this.pathToMethodMappings = pathToMethodMappings;
	}

	
	public void addPathToMethod(Pair<String, HttpMethods> mapping) {
		if (pathToMethodMappings == null) {
			pathToMethodMappings = new HashMap<>();
		} 

		if (pathToMethodMappings.get(mapping.getLeft()) == null) {
			pathToMethodMappings.put(mapping.getLeft(), Sets.newHashSet(mapping.getRight()));
		} else {
			pathToMethodMappings.get(mapping.getLeft()).add(mapping.getRight());
		}
	}
	
	public void addPathToMethods(Map<String, Set<HttpMethods>> pathToMethodMappings) {
		if (this.pathToMethodMappings == null) {
			this.pathToMethodMappings = pathToMethodMappings;
		} else {
			for (Entry<String, Set<HttpMethods>> entry : pathToMethodMappings.entrySet()) {
				for(HttpMethods meth : entry.getValue()) {
					addPathToMethod(new Pair<String, HttpMethods>(entry.getKey(), meth));
				}
			}
		}
	}

}
