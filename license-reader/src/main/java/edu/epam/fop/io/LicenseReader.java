package edu.epam.fop.io;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class LicenseReader {

    public void collectLicenses(File root, File outputFile) {
        // Verify that root and outputFile are not null
        if (root == null || outputFile == null) {
            throw new IllegalArgumentException("root and outputFile must not be null");
        }

        // Verify that root exists, is readable, and, if a directory, is executable
        if (!root.exists() || !root.canRead() || (root.isDirectory() && !root.canExecute())) {
            throw new IllegalArgumentException("Invalid root directory");
        }

        // Clear the outputFile if it exists
        if (outputFile.exists()) {
            outputFile.delete();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath())) {
            processDirectory(root, writer);
        } catch (NullPointerException e) {
            // Handle the original NullPointerException
            throw new IllegalArgumentException("Invalid argument provided", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while processing files", e);
        }
    }

    private void processDirectory(File directory, BufferedWriter writer) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(file, writer);
            } else if (isLicenseFile(file)) {
                processLicenseFile(file, writer);
            }
        }
    }

    private boolean isLicenseFile(File file) {
        // Check if the file has a .lic extension
        return file.getName().toLowerCase().endsWith(".lic");
    }

    private void processLicenseFile(File file, BufferedWriter writer) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            // Process the license file and write to the output using BufferedWriter
            processLicenseContent(reader, writer, file.getName());
        } catch (IOException e) {
            // Handle or log the exception
        }
    }

    private void processLicenseContent(BufferedReader reader, BufferedWriter writer, String fileName) throws IOException {
        // Initialize variables to store license properties
        String license = null;
        String issuedBy = null;
        String issuedOn = null;
        String expiresOn = "unlimited"; // Default value
    
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
    
                if ("License".equalsIgnoreCase(key)) {
                    license = value;
                } else if ("Issued on".equalsIgnoreCase(key)) {
                    issuedOn = value;
                } else if ("Issued by".equalsIgnoreCase(key)) {
                    issuedBy = value;
                } else if ("Expires on".equalsIgnoreCase(key)) {
                    expiresOn = value;
                }
            }
        }
    
        if (license != null && issuedBy != null && issuedOn != null) {
            // Write license information to the output file
            String outputLine = "License for " + fileName + " is " + license + " issued by " +
                issuedBy + " [" + issuedOn + " - " + expiresOn + "]" + System.lineSeparator();
            writer.write(outputLine);
        } else {
            throw new IllegalArgumentException("Invalid license header in " + fileName);
        }
    }
    
}
