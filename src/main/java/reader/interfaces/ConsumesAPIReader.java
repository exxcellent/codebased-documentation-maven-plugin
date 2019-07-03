package reader.interfaces;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import annotation.ConsumesAPI;
import util.ConsumeDescription;

public interface ConsumesAPIReader {
	
	public List<ConsumeDescription> getAPIConsumption(File src);

}
