package com.ssig.sensorsmanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Compression {

    public static void compressFiles(Map<String, File> filesToCompress, File compressedFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(compressedFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);

        for (Map.Entry<String, File> fileToCompress : filesToCompress.entrySet()){
            FileInputStream fileInputStream = new FileInputStream(fileToCompress.getValue());
            ZipEntry zipEntry = new ZipEntry(fileToCompress.getKey());
            zipOutputStream.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fileInputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
            fileInputStream.close();
        }
        zipOutputStream.close();
        fileOutputStream.close();
    }
}
