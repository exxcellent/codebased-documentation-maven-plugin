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
import util.ConsumeDescriptionTriple;

public interface ConsumesAPIReader {
	
	public List<ConsumeDescriptionTriple> getAPIConsumption(File src);

}
