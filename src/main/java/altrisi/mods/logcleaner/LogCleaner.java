package altrisi.mods.logcleaner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import static java.util.Arrays.asList;
import static java.util.Collections.max;

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

		FileTime maxKept = FileTime.from(Instant.now().minus(config.daysOld, ChronoUnit.DAYS));

		Logger logger = LogManager.getLogger("Log Cleaner");
		for (int i = 0; i < 100; i+=4) {
			logger.info("Starting in " + (100 - i) + " seconds...");
			try{Thread.sleep(4000);}catch(InterruptedException e){}
		}
		logger.info("Start of LogCleaner section");
		Thread.sleep(30000);
		logger.info("Current time: " + new Date());
		int deleted = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("logs"))) {
			for (Path logPath : stream) {
				String fileName = logPath.getFileName().toString();
				if (logNamePattern.matcher(fileName).matches()) {
					logger.info("Log: " + fileName);
					BasicFileAttributes attributes = Files.readAttributes(logPath, BasicFileAttributes.class);
					logger.info("Attributes pre-read: " + stringify(attributes));
					logger.info("Attributes post-read: " + stringify(Files.readAttributes(logPath, BasicFileAttributes.class)));
					// save
					Files.setAttribute(logPath, "lastAccessTime", attributes.lastAccessTime());
					logger.info("Attributes post-trying to keep: " + stringify(Files.readAttributes(logPath, BasicFileAttributes.class)));
					Files.setAttribute(logPath, "lastAccessTime", attributes.lastAccessTime()); // need to do it again cause we read it for logging
					// check it also when closing
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						try {
							logger.info("Attributes of " + fileName + " when leaving " + stringify(Files.readAttributes(logPath, BasicFileAttributes.class)));
							Files.setAttribute(logPath, "lastAccessTime", attributes.lastAccessTime()); // need to do it again cause we read it for logging
						} catch (IOException e) {}
					}));

					FileTime accessed = max(asList(
							attributes.lastAccessTime(),
							attributes.lastModifiedTime(),
							attributes.creationTime()
					));

					if (maxKept.compareTo(accessed) > 0) {
						// Bye
						//Files.delete(logPath);
						deleted++;
					}
				}
			}
		} catch (IOException e) {
			//Logger logger = LogManager.getLogger("Log Cleaner");
			logger.error("Exception while trying to clean log files", e);
		}
		if (deleted > 0 && !config.silent) {
			//Logger logger = LogManager.getLogger("Log Cleaner"); // if there was an IOE above (shouldn't), this returns the same Logger, not a new one
			//logger.info("Successfully deleted {} old log files", deleted);
		}
		logger.info("End of LogCleaner section");
		System.exit(0);
	}
	private static String stringify(BasicFileAttributes attributes) {
		return "[lastAccess=" + Date.from(attributes.lastAccessTime().toInstant()) + ", lastMod="+Date.from(attributes.lastModifiedTime().toInstant())+"]";
	}
}
