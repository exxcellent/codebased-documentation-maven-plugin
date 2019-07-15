package collectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaSource;

import collectors.models.maven.ModuleToComponentInfoObject;
import collectors.models.maven.ComponentInfoObject;
import edu.emory.mathcs.backport.java.util.Arrays;
import filemanagement.FileWriter;

/**
 * Collects information about the packages and their dependencies. Can be
 * started with a WhiteList of Packages. Then, only these and their sub-packages
 * will be looked at. If there is no WhiteList, all Packages in the project are
 * analyzed. The maxDepth is important to define the granularity of a package.
 * If the maxDepth was 0, there would be just one big package.
 * 
 * @author gmittmann
 *
 */
public class ComponentInfoCollector implements InformationCollector {

	private Map<String, Integer> whiteListMap;
	private Set<String> blackListSet;
	private MavenProject project;
	private Log log;

	private Set<String> whiteListPackageNames = new HashSet<>();
	private boolean defaultValuesUsed = false;

	public static final String FILE_NAME = "componentInformation";

	public ComponentInfoCollector(Map<String, Integer> whiteList, Set<String> blackList, MavenProject project, Log log) {
		this.whiteListMap = whiteList;
		this.blackListSet = blackList;
		this.project = project;
		this.log = log;
	}

	@Override
	public void collectInfo() {
		JavaProjectBuilder builder = new JavaProjectBuilder();
		String baseFileName = Paths.get(project.getBasedir().getAbsolutePath(), "\\src", "main", "java").toString();

		if (whiteListMap == null || whiteListMap.isEmpty()) {
			whiteListMap.put("", 1);
			log.warn("No WhiteList of packages defined! The results might not be as intended.");
			log.warn("Default values set. BaseFile: " + baseFileName + "; Depth: 1");
			defaultValuesUsed = true;
		}
		whiteListPackageNames = whiteListMap.keySet();

		Map<String, Set<String>> packageDependencies = new HashMap<>();

		File baseFile = new File(baseFileName);
		if (baseFile.exists()) {
			whiteListPackageNames = filterWhiteList();
			for (String name : whiteListPackageNames) {
				File dir = Paths.get(baseFileName, name.split("\\.")).toFile();
				collectBySourceAdd(name, dir, name, 0, builder, packageDependencies);
			}
		}
		
		String dirPath = Paths.get(project.getBasedir().getAbsolutePath(), "target", FOLDER_NAME).toString();
		log.info("target file: " + FILE_NAME);
		
		List<ComponentInfoObject> packageInfo = new ArrayList<>();
		for (Entry<String, Set<String>> entry : packageDependencies.entrySet()) {
			ComponentInfoObject pkgInfo = new ComponentInfoObject(entry.getKey());
			pkgInfo.setDependsOn(entry.getValue());
			packageInfo.add(pkgInfo);
		}
		String moduleId = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
		ModuleToComponentInfoObject info = new ModuleToComponentInfoObject(moduleId);
		info.setComponents(packageInfo);
		FileWriter.writeInfoToJSONFile(dirPath, FILE_NAME, info, log);
	}

	/**
	 * Filters the whiteList by removing subpackages. If a subpackage is found and
	 * the associated depth of this one leads to more packages reached than the with
	 * the other package, the value of the package is overwritten to reach them,
	 * too.
	 * 
	 * @return Set of String containing the remaining package names.
	 */
	private Set<String> filterWhiteList() {
		Set<String> filteredNames = new HashSet<>(whiteListPackageNames);
		for (String name : whiteListPackageNames) {
			for (String otherName : whiteListPackageNames) {
				if (!otherName.equals(name) && otherName.startsWith(name)) {
					int otherNameDepth = otherName.split("\\.").length + whiteListMap.get(otherName);
					int nameDepth = name.split("\\.").length + whiteListMap.get(name);
					int diff = otherNameDepth - nameDepth;

					filteredNames.remove(otherName);
					if (diff > 0) {
						whiteListMap.put(name, whiteListMap.get(name) + diff);
					}
				}
			}
		}
		return filteredNames;
	}

	/**
	 * Recursive method, that calls itself, until it finds a file. When it does, it
	 * reads its imports and adds them to the import dependencies map. The package
	 * name under which the info is saved is determined by the maxDepth and the
	 * directories, that were traversed until the file was reached.
	 * 
	 * @param currentFile         the file or directory that is currently looked at.
	 * @param packageName         name of the package that is currently worked with
	 *                            and into which the imports are to be saved.
	 * @param currentDepth        Integer value containing info about how many
	 *                            recursion steps we are into (therefore how many
	 *                            directories were stepped into since the base
	 *                            folder).
	 * @param builder             JavaProjectBuilder used to read import
	 *                            information.
	 * @param packageDependencies Map into which the package Dependencies shall be
	 *                            saved.
	 */
	private void collectBySourceAdd(String whiteListPackage, File currentFile, String packageName, int currentDepth,
			JavaProjectBuilder builder, Map<String, Set<String>> packageDependencies) {

		if (currentFile.isDirectory()) {
			if (currentDepth <= getDepthValueOfPackage(whiteListPackage) && currentDepth != 0) {
				packageName += "." + currentFile.getName();
			}
			currentDepth++;
			if (!isInBlackList(packageName)) {
				for (File file : currentFile.listFiles()) {
					collectBySourceAdd(whiteListPackage, file, packageName, currentDepth, builder, packageDependencies);
				}
			}
			currentDepth = 0;
		} else {
			try {
				JavaSource src = builder.addSource(currentFile);
				if (!isInBlackList(packageName)) {
					addSetToMap(packageName, getRelevantImportNames(packageName, src.getImports()),
							packageDependencies);
				}
			} catch (IOException e) {
				log.info("could not open file: " + currentFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Shortens the given imports to maxLength. If WhiteList exist, sorts out these,
	 * that are part of the whitelisted folders.
	 * 
	 * @param srcImports List of Strings representing the imports in this source.
	 * @return Shortened and (maybe) filtered import Strings.
	 */
	private Set<String> getRelevantImportNames(String currentPackage, List<String> srcImports) {
		Set<String> relevantImportPackages = new HashSet<>();

		for (String currentImport : srcImports) {
			if (isInWhiteList(currentImport) && !isInBlackList(currentImport)) {
				String[] currentImportSplit = currentImport.split("\\.");
				// remove last part to get package of imported class
				currentImportSplit = (String[]) Arrays.copyOf(currentImportSplit, currentImportSplit.length - 1);
				// remove anything longer than maxLength
				int maxBaseLength = getMaxBaseLength(currentImport);
				if (currentImportSplit.length > maxBaseLength && maxBaseLength != -1) {
					currentImportSplit = (String[]) Arrays.copyOf(currentImportSplit, maxBaseLength);
				}
				if (!String.join(".", currentImportSplit).equals(currentPackage)) {
					relevantImportPackages.add(String.join(".", currentImportSplit));
				}
			}
		}

		if (defaultValuesUsed) {
			log.warn("Used default values -> could not filter relevant imports"
					+ ((blackListSet == null || blackListSet.isEmpty()) ? "." : " except filter by blacklist."));
		}
		return relevantImportPackages;
	}

	/**
	 * Add the given Set to the given Map under the given name.
	 * 
	 * @param packageName key under which the set id to be saved.
	 * @param imports     set of Strings that are to be saved.
	 * @param map         Map to which the entry is to be added.
	 */
	private void addSetToMap(String packageName, Set<String> imports, Map<String, Set<String>> map) {
		if (map.get(packageName) != null) {
			map.get(packageName).addAll(imports);
		} else {
			map.put(packageName, imports);
		}
	}

	/**
	 * Check if the given package is in the whitelisted packages. If there were no
	 * whitelisted packages and the default value was used, this method always
	 * returns true.
	 * 
	 * @param packageName name of the package to be checked. Has to be full name.
	 * @return true, if the given package is part of the whitelisted packages or
	 *         there is no WhiteList.
	 */
	private boolean isInWhiteList(String packageName) {
		for (String base : whiteListPackageNames) {
			if (packageName.startsWith(base)) { // TODO: check if startsWith is enough
				return true;
			}
		}
		return defaultValuesUsed;
	}

	private boolean isInBlackList(String packageName) {
		for (String base : blackListSet) {
			if (base != null && packageName.startsWith(base)) { // TODO: check if startsWith is enough
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the maximum allowed depth. If there is no whitelist, the default
	 * value of 1 is returned. If the WhiteList is not empty, the package that
	 * contains the given package is searched for and its length added to the
	 * defined depth is returned.
	 * 
	 * @param packageName Name of the package, whose allowed maximum depth is
	 *                    searched for.
	 * @return maximum allowed depth. If there is a whitelist but the given package
	 *         is not contained in these, -1 is returned.
	 */
	private int getMaxBaseLength(String packageName) {
		if (defaultValuesUsed) {
			return 1;
		}
		for (String base : whiteListPackageNames) {
			if (packageName.startsWith(base)) {
				return base.split("\\.").length + getDepthValueOfPackage(base);
			}
		}
		return -1;
	}

	private int getDepthValueOfPackage(String packageName) {
		return whiteListMap.get(packageName) == null ? 1 : whiteListMap.get(packageName);
	}

}
