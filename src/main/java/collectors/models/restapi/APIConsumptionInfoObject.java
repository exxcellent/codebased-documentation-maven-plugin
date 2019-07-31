package collectors.models.restapi;

import java.util.ArrayList;
import java.util.List;

import collectors.models.InfoObject;
import util.ConsumeDescription;

/**
 * Class containing data about API consumption: Tag of the current service and
 * descriptions of which service to call with what path and method.
 * 
 * @author gmittmann
 *
 */
public class APIConsumptionInfoObject extends InfoObject {

	private String microserviceTag;
	private List<ConsumeDescription> consumes;

	public APIConsumptionInfoObject() {
		this.consumes = new ArrayList<>();
	}

	public String getMicroserviceTag() {
		return microserviceTag;
	}

	public void setMicroserviceTag(String microserviceTag) {
		this.microserviceTag = microserviceTag;
	}

	public List<ConsumeDescription> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<ConsumeDescription> consumes) {
		this.consumes = consumes;
	}

	public void addConsumeDescriptionTriple(ConsumeDescription triple) {
		this.consumes.add(triple);
	}

}
