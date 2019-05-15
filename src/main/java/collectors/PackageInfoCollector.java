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

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Collects information about the packages and their dependencies. Can be
 * started with a WhiteList of Packages. Then, only these and their sub-packages
 * will be looked at. If there is no WhiteList, all Packages in the project are
 * analysed. The maxDepth is important to define the granularity of a package.
 * If the maxDepth was 0, there would be just one big package.
 * 
 * @author gmittmann
 *
 */
public class PackageInfoCollector implements InformationCollector {

	private MavenProject project;
	private Log log;

	private int maxDepth = 2;
	private Set<String> whiteListPackageNames = new HashSet<>();
	private Set<String> blackListPackageNames = new HashSet<>();

	public static final String FILE_NAME = "packageInformation";

	public PackageInfoCollector(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}

	@Override
	public void collectInfo() {
		whiteListPackageNames.add("de.bogenliga.application");
		JavaProjectBuilder builder = new JavaProjectBuilder();
		String baseFileName = Paths.get(project.getBasedir().getAbsolutePath(), "\\src", "main", "java").toString();
		log.info(baseFileName);

		Map<String, Set<String>> packageDependencies = new HashMap<>();

		File baseFile = new File(baseFileName);
		if (whiteListPackageNames != null && !whiteListPackageNames.isEmpty()) {
			whiteListPackageNames = filterWhiteList();
			for (String name : whiteListPackageNames) {
				File dir = Paths.get(baseFileName, name.split("\\.")).toFile();
				collectBySourceAdd(dir, name, 0, builder, packageDependencies);
			}
		} else if (baseFile.exists()){
			for (File file : baseFile.listFiles()) {
				collectBySourceAdd(file, "", 0, builder, packageDependencies);
			}
		}

//		for (Entry<String, Set<String>> entry : packageDependencies.entrySet()) {
//			entry.setValue(removeSubPackages(entry.getValue()));
//		}

		log.info("-----------------------------------------------");
		for (Entry<String, Set<String>> entry : packageDependencies.entrySet()) {
			log.info(entry.getKey());
			for (String pkg : entry.getValue()) {
				log.info("  -" + pkg);
			}
		}
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
	private void collectBySourceAdd(File currentFile, String packageName, int currentDepth, JavaProjectBuilder builder,
			Map<String, Set<String>> packageDependencies) {

		if (currentFile.isDirectory()) {
			if (currentDepth == 0 && whiteListPackageNames.isEmpty()) {
				packageName += currentFile.getName();
			} else if (currentDepth <= maxDepth && currentDepth != 0) {
				packageName += "." + currentFile.getName();
			}
			currentDepth++;
			for (File file : currentFile.listFiles()) {
				collectBySourceAdd(file, packageName, currentDepth, builder, packageDependencies);
			}
			currentDepth = 0;
		} else {
			try {
				JavaSource src = builder.addSource(currentFile);
				addSetToMap(packageName, getRelevantImportNames(src.getImports()), packageDependencies);
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
	private Set<String> getRelevantImportNames(List<String> srcImports) {
		Set<String> relevantImportPackages = new HashSet<>();

		for (String currentImport : srcImports) {
			if ((whiteListPackageNames == null || whiteListPackageNames.isEmpty()) || isInBasePackage(currentImport)) {
				String[] currentImportSplit = currentImport.split("\\.");
				// remove last bit
				currentImportSplit = (String[]) Arrays.copyOf(currentImportSplit, currentImportSplit.length - 1);
				// remove anything longer than maxLength
				int maxBaseLength = getMaxBaseLength(currentImport);
				if (currentImportSplit.length > maxBaseLength && maxBaseLength != -1) {
					currentImportSplit = (String[]) Arrays.copyOf(currentImportSplit, maxBaseLength);
				}
				relevantImportPackages.add(String.join(".", currentImportSplit));
			}
		}

		return relevantImportPackages;
	}

	/**
	 * Add teh given Set to the given Map under the given name.
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
	 * Check if the given package is in the whitelisted packages.
	 * 
	 * @param packageName name of the package to be checked. Has to be full name.
	 * @return true, if the given package is part of the whitelisted packages.
	 */
	private boolean isInBasePackage(String packageName) {
		for (String base : whiteListPackageNames) {
			if (packageName.startsWith(base)) { // TODO: check if startsWith is enough
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the maximum allowed depth. If there is no whitelist, the defined
	 * maxDepth is returned. If the WhiteList is not empty, the package that
	 * contains the given package is searched for and its length added to the
	 * maxDepth is returned.
	 * 
	 * @param packageName Name of the package, whose allowed maximum depth is
	 *                    searched for.
	 * @return maximum allowed depth. If there is a whitelist but the given package
	 *         is not contained in these, -1 is returned.
	 */
	private int getMaxBaseLength(String packageName) {
		if (whiteListPackageNames == null || whiteListPackageNames.isEmpty()) {
			return maxDepth;
		}
		for (String base : whiteListPackageNames) {
			if (packageName.startsWith(base)) {
				return base.split("\\.").length + maxDepth;
			}
		}
		return -1;
	}

	/**
	 * Returns a set of the given packages, in which the packages that are in the
	 * set twice in form of package and subpackage (e.g. example.common and
	 * example.common.util) are removed. 
	 * 
	 * @param packageSet Set of package names that is to be filtered.
	 * @return Filtered set.
	 */
	private Set<String> removeSubPackages(Set<String> packageSet) {
		List<String> shortForms = new ArrayList<>();
		for (String str : packageSet) {
			if (str.split("\\.").length < getMaxBaseLength(str)) {
				shortForms.add(str);
//				log.info("SHORT: " + str);
			}
		}
		Set<String> returnSet = new HashSet<String>();
		returnSet.addAll(shortForms);
		for (String str : packageSet) {
			boolean retain = true;
			for (String shortForm : shortForms) {
				if (str.startsWith(shortForm)) {
//					log.info("remove: " + str);
					retain = false;
					break;
				}
			}
			if (retain) {
				returnSet.add(str);
			}
		}
		return returnSet;
	}
	
	private Set<String> filterWhiteList() {
		Set<String> filteredNames = new HashSet<>(whiteListPackageNames);
		for (String name : whiteListPackageNames) {
			for (String otherName : whiteListPackageNames) {
				if (!otherName.equals(name) && otherName.startsWith(name)) {
					filteredNames.remove(otherName);
				}
			}
		}
		return filteredNames;
	}
	
}
