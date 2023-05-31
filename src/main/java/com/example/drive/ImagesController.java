package com.example.drive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
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

    @GetMapping("/images")
    public String Get()
    {
        return "Bruh";
    }

    @GetMapping("/images/{subdir}/{pass}")
    @ResponseBody
    public String Get(@PathVariable String subdir, @PathVariable String pass) throws URISyntaxException, IOException {
        System.out.println("Got request");
        System.out.println(System.getProperty("user.dir"));
        subdir = subdir.replace("!2F","/");
        subdir = subdir.replace("!3D","=");
        System.out.println("Current subdir: " + subdir);
        if (!pass.equals("^"))
        {
            byte[] data = Base64.getDecoder().decode(pass);
            pass = new String(data, StandardCharsets.UTF_8);
        }
        String readText = readFile(System.getProperty("user.dir") + "/media/pi/Long-Term1/validImageLogins.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<String> passes = objectMapper.readValue(readText, new TypeReference<>() {});
        if (pass.equals("^"))
        {
            System.out.println("Logged in returning files at " + Instant.now().toString());
            return collectManifest(subdir);
        }
        else
        {
            if (passes != null && passes.size() > 0)
                for (String temp : passes)
            if (pass.equals(temp))
            {
                System.out.println("Logged in returning files at " + Instant.now().toString());
                return collectManifest(subdir);
            }
        }
        return "login failed";
    }
    public static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    public static String[] fileNames(String directoryPath) {

        File dir = new File(directoryPath);

        Collection<String> files  =new ArrayList<String>();

        if(dir.isDirectory()){
            File[] listFiles = dir.listFiles();

            for(File file : listFiles){
                if(file.isFile()) {
                    files.add(file.getName());
                }
            }
        }

        return files.toArray(new String[]{});
    }
    static class ManifestFile
    {
        public int Index;
        public String Path;
        public int MediaType;
            public ManifestFile(int i, String p, int m)
        {
            Index = i;
            Path = p;
            MediaType = m;
        }
    }
    public static String collectManifest(String subdir) throws IOException {
        System.out.println("Begining export of manifest...");
        ArrayList<ManifestFile> manifestFiles = new ArrayList<ManifestFile>();
        System.out.println("Counting files...");
        String[] files = fileNames(System.getProperty("user.dir") + "/media/pi/Long-Term1/images" + subdir);
        if(files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                manifestFiles.add(new ManifestFile(i, "https://matgames.net/apcsStorage/images/" + subdir + "/" + files[i].split("/")[files[i].split("/").length - 1], FindFileType(files[i])));
            }
        }
        if (!(new File(System.getProperty("user.dir") + "/var/www/html/image" + subdir).exists()))
        {
            new File(System.getProperty("user.dir") + "/var/www/html/image" + subdir).mkdirs();
        }
        if (!(new File(System.getProperty("user.dir") + "/var/www/html/image" + subdir + "manifest.json").exists()))
        {
            var file = new File(System.getProperty("user.dir") + "/var/www/html/image" + subdir + "manifest.json").createNewFile();
        }
        var mapper = new ObjectMapper();
        String fileData = mapper.writeValueAsString(manifestFiles);
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/var/www/html/image" + subdir + "manifest.json");
        fos.write(fileData.getBytes());
        fos.flush();
        fos.close();
        return readFile(System.getProperty("user.dir") + "/var/www/html/image" + subdir + "manifest.json",StandardCharsets.UTF_8);
    }
    static String[] imageTypes = { "jpg", "jpeg", "png", "webp", "dng", "tif", "heic" };
    static String[] videoTypes = { "mp4", "webm", "gif", "mov", "avi" };
    public static int FindFileType(String file)
    {
            String fileType = file.split("\\.")[file.split("\\.").length - 1];
        for (String localFileType : imageTypes)
        {
            if (fileType.equals(localFileType))
            {
                return 1;
            }
        }
        for (String localFileType : videoTypes)
        {
            if (fileType.equals(localFileType))
            {
                if (!new File(System.getProperty("user.dir") + "/media/pi/Long-Term1/thumbnails/" + file.split("/")[file.split("/").length - 1].split("\\.")[0] + ".png").exists())
                {
                    if (file.contains(" "))
                    {
                        System.out.println("File contained space");
                        file = file.replace(" ", "\\ ");
                    }
                    if (file.contains("("))
                    {
                        System.out.println("File contained (");
                        file = file.replace("(", "\\(");
                    }
                    if (file.contains(")"))
                    {
                        System.out.println("File contained )");
                        file = file.replace(")", "\\)");
                    }
                }
                return 2;
            }
        }
        return 0;
    }
    @PutMapping("/images/{link}")
    @ResponseBody
    public String images(@PathVariable String link){ //link is b64
        System.out.println(link);

        link = link.replace("!2F","/");
        link = link.replace("!3D","=");
        byte[] data = Base64.getDecoder().decode(link);
        link = new String(data, StandardCharsets.UTF_8);

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
        if (new File(System.getProperty("user.dir") + "/var/www/html/images/custom/" + fileName).exists())
        {
            UUID guid = UUID.randomUUID();
            fileName = fileName.split("\\.")[0] + "(" + guid.toString().split("-")[0] + ")" + "." + fileName.split("\\.")[fileName.split("\\.").length - 1];
            System.out.println("File with same name already exists. New file name: " + fileName);
        }
        try {
            URL url = new URL(link);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            int bytes_total = urlConnection.getContentLength();
            System.out.println("File size: " + bytes_total + " bytes.");
            if (bytes_total < 8000000) {
                downloadFile(url, System.getProperty("user.dir") + "/media/pi/Long-Term1/images/" + fileName);
                System.out.println("Downloading file...");
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return "https://matgames.net/apcsStorage/images/" + fileName;
    }
}
