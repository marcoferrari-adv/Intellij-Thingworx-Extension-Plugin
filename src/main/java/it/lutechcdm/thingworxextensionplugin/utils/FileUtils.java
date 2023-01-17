package it.lutechcdm.thingworxextensionplugin.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static void copyStreamToOut(InputStream in, String filePath) throws IOException {
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = in.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static String readStreamToString(InputStream in) throws IOException {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = in.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }
}
