package com.example.demo;

import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import com.example.demo.service.CacheManagerService;

import java.util.Optional;
import java.util.OptionalInt;

@SpringBootApplication
@EnableCaching
public class CachingProxyApplication {

	public static final int DEFAULT_PORT = 3000;
	public static final String DEFAULT_ORIGIN_URL = "http://dummyjson.com";
	public static final String ORIGIN_ARG = "origin";
	public static final String PORT_ARG = "port";
	public static final String CLEAR_CACHE_ARG = "clear-cache";

	public static void main(String[] args) {
		// Parse command line arguments
		CommandLine cmd = parseCommandLine(args);
		
		// Handle clear cache command
		if (cmd.hasOption(CLEAR_CACHE_ARG)) {
			clearCache();
			return;
		}
		
		// Get port and origin from command line or use defaults
		int port = getPortFromCmdLine(cmd).orElse(DEFAULT_PORT);
		String origin = getOriginFromCmdLine(cmd).orElse(DEFAULT_ORIGIN_URL);
		
		// Set system properties for Spring Boot configuration
		System.setProperty("server.port", String.valueOf(port));
		System.setProperty("global.origin", origin);
		
		// Start Spring Boot application
		ConfigurableApplicationContext context = SpringApplication.run(CachingProxyApplication.class, args);
		
		System.out.println("🚀 Caching Proxy Server started!");
		System.out.println("📍 Port: " + port);
		System.out.println("🎯 Origin: " + origin);
		System.out.println("📝 Usage: http://localhost:" + port + "/<path>");
	}

	private static CommandLine parseCommandLine(String[] args) {
		Options options = new Options();
		
		options.addOption(Option.builder()
			.longOpt(PORT_ARG)
			.hasArg()
			.desc("Port number for the proxy server")
			.build());
			
		options.addOption(Option.builder()
			.longOpt(ORIGIN_ARG)
			.hasArg()
			.desc("Origin server URL")
			.build());
			
		options.addOption(Option.builder()
			.longOpt(CLEAR_CACHE_ARG)
			.desc("Clear the cache")
			.build());
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		
		try {
			return parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error parsing command line arguments: " + e.getMessage());
			formatter.printHelp("caching-proxy", options);
			System.exit(1);
			return null;
		}
	}

	private static OptionalInt getPortFromCmdLine(CommandLine cmd) {
		if (cmd.hasOption(PORT_ARG)) {
			try {
				int port = Integer.parseInt(cmd.getOptionValue(PORT_ARG));
				if (port > 0 && port <= 65535) {
					return OptionalInt.of(port);
				} else {
					System.err.println("Port must be between 1 and 65535");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid port number: " + cmd.getOptionValue(PORT_ARG));
				System.exit(1);
			}
		}
		return OptionalInt.empty();
	}

	private static Optional<String> getOriginFromCmdLine(CommandLine cmd) {
		if (cmd.hasOption(ORIGIN_ARG)) {
			String origin = cmd.getOptionValue(ORIGIN_ARG);
			if (origin != null && !origin.trim().isEmpty()) {
				return Optional.of(origin);
			}
		}
		return Optional.empty();
	}
	
	private static void clearCache() {
		System.out.println("🗑️  Clearing cache...");
		// Start Spring Boot application to get access to CacheManagerService
		ConfigurableApplicationContext context = SpringApplication.run(CachingProxyApplication.class, new String[]{});
		CacheManagerService cacheManager = context.getBean(CacheManagerService.class);
		
		int cacheSize = cacheManager.getCacheSize();
		cacheManager.clearCache();
		
		System.out.println("✅ Cache cleared successfully! Removed " + cacheSize + " entries.");
		context.close();
	}

}
