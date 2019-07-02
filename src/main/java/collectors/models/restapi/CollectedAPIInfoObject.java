package collectors.models.restapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import annotation.ConsumesAPI;
import collectors.models.InfoObject;
import util.ConsumeDescriptionTriple;

public class CollectedAPIInfoObject extends InfoObject {

	private String serviceName;
	private APIInfoObject provide;
	private List<ConsumeDescriptionTriple> consume;

	public CollectedAPIInfoObject(String name) {
		if (name != null) {
			this.serviceName = name;
		} else {
			this.serviceName = ConsumesAPI.DEFAULT_SERVICE;
		}
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public APIInfoObject getProvide() {
		return provide;
	}

	public void setProvide(APIInfoObject provide) {
		this.provide = provide;
	}

	public List<ConsumeDescriptionTriple> getConsume() {
		return consume;
	}

	public void setConsume(List<ConsumeDescriptionTriple> consume) {
		this.consume = consume;
	}

	public void addConsumeDescriptionTriple(ConsumeDescriptionTriple triple) {
		if (this.consume == null) {
			this.consume = new ArrayList<>();
		}
		this.consume.add(triple);
	}

	public void addConsumeDescriptionTriples(Collection<ConsumeDescriptionTriple> triples) {
		if (this.consume == null) {
			this.consume = new ArrayList<>();
		}
		this.consume.addAll(triples);
	}

}
