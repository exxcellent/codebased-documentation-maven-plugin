package collectors.models.restapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import collectors.models.InfoObject;
import util.HttpMethods;
import util.OfferDescription;

/**
 * Class containing data about offered REST API: name and tag of the current
 * service, descriptions of paths and their offered methods.
 * 
 * @author gmittmann
 *
 */
public class APIInfoObject extends InfoObject {

	private String microserviceTag;
	private String microserviceName;
	private List<OfferDescription> api;

	public APIInfoObject(String tag, String name) {
		this.setMicroserviceTag(tag);
		this.setMicroserviceName(name);
		this.api = new ArrayList<>();
	}

	public String getMicroserviceTag() {
		return microserviceTag;
	}

	public void setMicroserviceTag(String microserviceTag) {
		this.microserviceTag = microserviceTag;
	}

	public String getMicroserviceName() {
		return microserviceName;
	}

	public void setMicroserviceName(String microserviceName) {
		this.microserviceName = microserviceName;
	}

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
