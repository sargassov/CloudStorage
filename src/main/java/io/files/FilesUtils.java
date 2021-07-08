package io.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilesUtils {

    private static File file;
    private static File copyFile;
    private static byte buffer[];

    public static void main(String[] args) {
        file = new File("4065503.jpg");
        copyFile = new File("copyFile.jpg");

        buffer = new byte[256];
        try(FileInputStream fis = new FileInputStream(file)){
            FileOutputStream fos = new FileOutputStream(copyFile);
            int read;
            while((read = fis.read(buffer)) != -1){
                fos.write(buffer, 0 , read);
            }
            fos.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
