package filemanagement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.logging.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import collectors.models.InfoObject;

/**
 * Helper/Wrapper class for file creation and use.
 * 
 * @author gmittmann
 *
 */
public final class FileWriter {

	public static final String CHARSET = "UTF-8";

	private FileWriter() {

	}

	/**
	 * Writes the given text into the file.
	 * 
	 * @param filePath path to the directory in which the file is to be created
	 * @param fileName name of the file, into which the text is to be written. If
	 *                 the file already exists, it will be overwritten.
	 * @param text     Text to be write to the file.
	 * @return true, if the text was written. False if there was an error or there
	 *         was no file created //TODO: errorhandling
	 */
	public static boolean writeTextIntoFile(String filePath, String fileName, String text, Log log) {

		try (OutputStreamWriter out = createFile(filePath, fileName, log)) {
			out.append(text);
			out.flush();
			return true;
		} catch (IOException e) {
			log.error("Could not write into file: " + filePath);
			log.error(e.getMessage());
		}

		return false;
	}

	/**
	 * Takes the given InfoObject and serializes it to JSON into the given file.
	 * 
	 * @param <T>      Type of InfoObject
	 * @param path     path to the target directory
	 * @param fileName name of the file
	 * @param info     InfoObject which contains the information to be written into
	 *                 the file
	 * @return true, if the file was created and written successfully.
	 */
	public static <T extends InfoObject> boolean writeInfoToJSONFile(String path, String fileName, T info, Log log) {

		Gson gson = createGson();
		try (OutputStreamWriter out = createFile(path, fileName, log)) {
			gson.toJson(info, out);
			out.flush();
			return true;
		} catch (JsonIOException e) {
			log.error("Could not create JSON from object");
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Tries to create the file with the given file name at the given path and an
	 * OutputStreamWriter to it.
	 * 
	 * @param path     path of the target directory.
	 * @param fileName name of the file to be created
	 * @return outputStreamWriter Writer to the created file. null if there was an
	 *         error.
	 */
	private static OutputStreamWriter createFile(String path, String fileName, Log log) {

		try {
			createDir(path, log);

			Path logFile = Paths.get(path, fileName + ".json");

			if (!logFile.toFile().exists()) {
				Files.createFile(logFile);
			}
			FileOutputStream stream = new FileOutputStream(logFile.toFile(), false);
			OutputStreamWriter out = new OutputStreamWriter(stream, CHARSET); // TODO: set charset
			return out;

		} catch (NullPointerException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error("IO: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Checks whether the given file path is null. If it isn't creates all
	 * directories of the path that do not exist already.
	 * 
	 * @param path Path of the target directory to create.
	 * @return Path Path object to the created directory.
	 * @throws NullPointerException if path is null
	 */
	private static Path createDir(String path, Log log) throws NullPointerException {
		if (path != null) {
			try {
				return Files.createDirectories(Paths.get(path));
			} catch (IOException e1) {
				log.error("error creating directories at: " + Paths.get(path).toString());
				log.error(e1.getMessage());
			}
		} else {
			throw new NullPointerException("Path to directory is null!");
		}
		return null;
	}
	
	private static Gson createGson() {
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	}

}
