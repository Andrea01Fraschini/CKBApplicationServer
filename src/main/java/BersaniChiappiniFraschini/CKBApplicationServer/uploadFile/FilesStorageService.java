package BersaniChiappiniFraschini.CKBApplicationServer.uploadFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilesStorageService {

    // substitute System.getProperty("user.dir") with "./"

    private final String save = System.getProperty("user.dir")+"/SAVE";
    private final String extract = System.getProperty("user.dir")+"/EXTRACT";

    private String internName = "";
    private String battle_name;

    public void init(String battle_name) {
        try {
            this.battle_name = battle_name;
            Files.createDirectories(Paths.get(extract));
            Files.createDirectories(Paths.get(save));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public void save(MultipartFile file) throws Exception {
        try {
            Files.createDirectories(Paths.get(this.save+"/"+battle_name));
            Files.copy(file.getInputStream(), Paths.get(this.save+"/"+battle_name+"/"+file.getOriginalFilename()));
        } catch (Exception e) {
            throw new Exception("A file of that name already exists.");

        }
    }

    public Resource load(String filename) {
        try {
            Path file = Paths.get(save+"/"+battle_name+"/"+filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }


    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(save+"/"+battle_name).toFile());
        FileSystemUtils.deleteRecursively(Paths.get(extract+"/"+battle_name).toFile());
    }


    public String pathToGitHub(){

        File[] subdirectories = Paths.get(extract+"/"+battle_name).toFile().listFiles();

        if (subdirectories != null) {
            for (File subdirectory : subdirectories) {
                if (subdirectory.isDirectory() && subdirectory.getName().equals(internName)) {
                   return subdirectory.getPath();
                }
            }
        }

        return null;
    }
    public void unzip(InputStream zipInputStream, String battle_name) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                Files.createDirectories(Paths.get(this.extract+"/"+battle_name));
                String filePath = this.extract + "/" + battle_name+ "/" + entry.getName();

                if(internName.equals("")){
                    internName = entry.getName().split("/")[0];
                }

                File newFile = new File(filePath);

                if (entry.isDirectory()) {
                    if (!newFile.exists()) {
                        newFile.mkdirs();
                    }
                } else {
                    // Crea le directory necessarie
                    new File(newFile.getParent()).mkdirs();

                    // Scrivi il file
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
