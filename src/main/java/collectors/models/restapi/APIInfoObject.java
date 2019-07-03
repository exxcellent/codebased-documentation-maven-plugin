package collectors.models.restapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import collectors.models.InfoObject;
import util.HttpMethods;
import util.OfferDescription;

public class APIInfoObject extends InfoObject {

	private String microserviceName;
	private List<OfferDescription> api;
//	private String packageName;
//	private Map<String, Set<String>> pathToMethod;

	public APIInfoObject(String name) {
		this.setMicroserviceName(name);
		this.api = new ArrayList<>();
	}

//	public Map<String, Set<String>> getPathToMethod() {
//		return pathToMethod;
//	}
//
//	public void setPathToMethod(Map<String, Set<String>> pathToMethod) {
//		this.pathToMethod = pathToMethod;
//	}

//	public void addMethod(String path, String method) {
//		if (pathToMethod.containsKey(path)) {
//			pathToMethod.get(path).add(method);
//			return;
//		}
//		Set<String> methods = new HashSet<>();
//		methods.add(method);
//		pathToMethod.put(path, methods);
//	}
//	
//	public void addMethod(String path, Collection<String> method) {
//		if (pathToMethod.containsKey(path)) {
//			pathToMethod.get(path).addAll(method);
//			return;
//		}
//		Set<String> methods = new HashSet<>();
//		methods.addAll(method);
//		pathToMethod.put(path, methods);
//	}
//	
//	public void addPath(String path) {
//		if (pathToMethod.containsKey(path)) {
//			return;
//		}
//		pathToMethod.put(path, new HashSet<>());
//	}

	public String getMicroserviceName() {
		return microserviceName;
	}

	public void setMicroserviceName(String microserviceName) {
		this.microserviceName = microserviceName;
	}

//	public String getPackageName() {
//		return packageName;
//	}
//
//	public void setPackageName(String packageName) {
//		this.packageName = packageName;
//	}

	public List<OfferDescription> getApi() {
		return api;
	}

	public void setApi(List<OfferDescription> api) {
		this.api = api;
	}

	public void addOffer(OfferDescription offer) {

		if (this.api.isEmpty()) {
			this.api = Lists.newArrayList(offer);
		} else {
			boolean found = false;
			for (OfferDescription currentOffer : this.api) {
				if (currentOffer.getPackageName().equals(offer.getPackageName())) {
					Map<String, Set<HttpMethods>> mergedPathToMethods = joinPathToMethods(
							currentOffer.getPathToMethodMappings(), offer.getPathToMethodMappings());
					currentOffer.setPathToMethodMappings(mergedPathToMethods);
					found = true;
					break;
				}
			}
			if (!found) {
				this.api.add(offer);
			}
		}
	}

	private Map<String, Set<HttpMethods>> joinPathToMethods(Map<String, Set<HttpMethods>> pTM1,
			Map<String, Set<HttpMethods>> pTM2) {
		Map<String, Set<HttpMethods>> returnMap = new HashMap<>();
		if (pTM1 == null || pTM1.isEmpty()) {
			return pTM2;
		}
		if (pTM2 == null || pTM2.isEmpty()) {
			return pTM1;
		}

		returnMap.putAll(pTM1);
		pTM2.forEach((path, methods) -> returnMap.merge(path, methods, (v1, v2) -> {
			if (v1.containsAll(v2)) {
				return v1;
			} else {
				HashSet<HttpMethods> returnSet = new HashSet<>(v1);
				returnSet.addAll(v2);
				return returnSet;
			}
		}));
		return returnMap;
	}

	public void addOffers(List<OfferDescription> offers) {
		for (OfferDescription offer : offers) {
			addOffer(offer);
		}
	}

}
