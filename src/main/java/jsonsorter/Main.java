package jsonsorter;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");
    }

    public static void main(String[] args) {
        String inFileName = args[0];
        String outFileName = inFileName;

        try {
            Path inPath = Paths.get(inFileName);
            Path outPath = Paths.get(outFileName);

            // Write to temp file first, then overwrite original
            Path tmpPath = Files.createTempFile(null, null);
            try {
                System.out.println("Reading " + inPath);
                try (InputStream in = Files.newInputStream(inPath)) {
                    try (OutputStream out = Files.newOutputStream(tmpPath)) {
                        new JsonSorter().process(in, out);
                    }
                }

                System.out.println("Writing " + outPath);
                Files.copy(tmpPath, outPath, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(tmpPath);
            }

            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
