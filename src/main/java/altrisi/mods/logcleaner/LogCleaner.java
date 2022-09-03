package altrisi.mods.logcleaner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class LogCleaner {
	static class Config {
		final int daysOld = 14;
		final boolean silent = false;
		//final Set<String> filtered = Collections.emptySet(); //TBD
	}
	
	public static void run() {
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		Config config;
		
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("logcleaner.json");
		try (BufferedReader reader = Files.newBufferedReader(configPath)) {
			config = gson.fromJson(reader, Config.class);
			if (config == null) config = new Config(); // Empty files will do this
		} catch (NoSuchFileException e) {
			// Creating new config file
			config = new Config();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read config file!", e);
		}

		// Save, adds possibly missing fields if they weren't present, or generates the file if it didn't exist
		try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
			gson.toJson(config, writer);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to save config file!", e);
		}

		Pattern logNamePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}-\\d\\.log\\.gz$"); // yyyy-MM-dd-i.log.gz (from Minecraft's log4j config)

		Instant maxKept = Instant.now().minus(config.daysOld, ChronoUnit.DAYS);

		int deleted = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("logs"))) {
			for (Path logPath : stream) {
				String fileName = logPath.getFileName().toString();
				if (logNamePattern.matcher(fileName).matches()) {
					BasicFileAttributes attributes = Files.readAttributes(logPath, BasicFileAttributes.class);

					Instant accessed = newest(
							attributes.lastAccessTime(),
							attributes.lastModifiedTime(),
							attributes.creationTime()
					);
					if (maxKept.compareTo(accessed) > 0) {
						// Bye
						Files.delete(logPath);
						deleted++;
					}
				}
			}
		} catch (IOException e) {
			Logger logger = LogManager.getLogger("Log Cleaner");
			logger.error("Exception while trying to clean log files", e);
		}
		if (deleted > 0 && !config.silent) {
			Logger logger = LogManager.getLogger("Log Cleaner"); // if there was an IOE above (shouldn't), this returns the same Logger, not a new one
			logger.info("Successfully deleted {} old log files", deleted);
		}
	}
	
	private static Instant newest(FileTime... times) {
		Instant newest = Instant.EPOCH;
		for (FileTime time : times) {
			if (time.toInstant().compareTo(newest) >= 0) {
				newest = time.toInstant(); // FileTime caches the Instant, so we don't need to
			}
		}
		return newest;
	}
}
