package reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import util.HttpMethods;
import util.Pair;

public class JAXRSReaderTest {
	
	MavenProject project;
	Log log;
	
	@BeforeAll
	public static void setUp() {
		
	}
	
	@Test
	public void testBasicReader() {
		project = Mockito.mock(MavenProject.class);
		Mockito.when(project.getBasedir()).thenReturn(Paths.get("src/test/resources").toFile());
		
		log = Mockito.mock(Log.class);
		Mockito.doNothing().when(log).info(Mockito.anyString());
		Mockito.doNothing().when(log).error(Mockito.anyString());		
		
		
		File file = Paths.get("src/test/java/reader/testclass1").toFile();
		JAXRSReader reader = new JAXRSReader(project, log, null);
		List<Pair<String, HttpMethods>> result = reader.getPathsAndMethods(file);
		
		List<Pair<String, HttpMethods>> expected = new ArrayList<>();
		expected.add(new Pair<String, HttpMethods>("/rest/test/{param}", HttpMethods.GET));
		expected.add(new Pair<String, HttpMethods>("/rest/test", HttpMethods.PUT));
		
		
		assertTrue(result.size() == 2);
		assertTrue(result.get(0).equals(expected.get(0)) || result.get(0).equals(expected.get(1)));
		assertTrue(result.get(1).equals(expected.get(0)) || result.get(1).equals(expected.get(1)));
		assertEquals(expected, result);
	}
	
	@Test
	public void testApplicationPath() {
		project = Mockito.mock(MavenProject.class);
		Mockito.when(project.getBasedir()).thenReturn(Paths.get("src/test/resources").toFile());
		
		log = Mockito.mock(Log.class);
		Mockito.doNothing().when(log).info(Mockito.anyString());
		Mockito.doNothing().when(log).error(Mockito.anyString());		
		
		
		File file = Paths.get("src/test/java/reader/testclass2").toFile();
		JAXRSReader reader = new JAXRSReader(project, log, null);
		List<Pair<String, HttpMethods>> result = reader.getPathsAndMethods(file);
		
		List<Pair<String, HttpMethods>> expected = new ArrayList<>();
		expected.add(new Pair<String, HttpMethods>("/application/test/all", HttpMethods.POST));
		
		assertTrue(result.size() == 1);
		assertEquals(expected.get(0), result.get(0));
	}

}
