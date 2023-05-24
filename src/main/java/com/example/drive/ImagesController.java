package com.example.drive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.UUID;

@RestController
public class ImagesController {

    public static void downloadFile(URL url, String outputFileName)
    {
        try {
            try (InputStream in = url.openStream();
                 ReadableByteChannel rbc = Channels.newChannel(in);
                 FileOutputStream fos = new FileOutputStream(outputFileName)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    static String fileName;

    @PutMapping("/images/{link}")
    public String images(String link){
        link = link.replace("%2F", "/");
        System.out.println("Origin: " + link);
        String[] split = link.split("\\?");
        split = split[0].split("/");
        fileName = split[split.length-1];
        fileName = fileName.replace(" ", "");
        fileName = fileName.replace("|", "");
        fileName = fileName.replace("@", "");
        fileName = fileName.split("\\?")[0];
        System.out.println("Grabbed filename: " + fileName);
        if (fileName.split("\\.")[0].length() > 30)
        {
            long unixTime = Instant.now().getEpochSecond();
            fileName = unixTime + "." + fileName.split("\\.")[1];
            System.out.println("Filename was >30 chars. Changed to \"" + fileName + split[split.length - 1] + "\"");
        }
        if (new File("/var/www/html/images/custom/" + fileName).exists())
        {
            UUID guid = UUID.randomUUID();
            fileName = fileName.split("\\.")[0] + "(" + guid.toString().split("-")[0] + ")" + "." + fileName.split("\\.")[fileName.split(".").length - 1];
            System.out.println("File with same name already exists. New file name: " + fileName);
        }
        try {
            URL url = new URL(link);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            int bytes_total = urlConnection.getContentLength();
            System.out.println("File size: " + bytes_total + " bytes.");
            if (bytes_total < 8000000) {
                downloadFile(url, "/var/www/html/images/custom/" + fileName);
                System.out.println("Downloading file...");
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return "https://matgames.net/apcs/images/custom/" + fileName;
    }
}
