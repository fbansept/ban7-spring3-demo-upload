package edu.ban7.springdemo.upload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.datatransfer.MimeTypeParseException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileService {

    @Value("${public.upload.folder}")
    private String publicUploadFolder;

    @Value("${private.upload.folder}")
    private String privateUploadFolder;

    /**
     * Si l'envoie est effectuée via un paramètre MultipartFile il est convertie en InputStream
     * Puis appel de uploadToLocalFileSystem
     *
     * @param multipartFile fichier à transférer
     * @param fileName nom qui sera affecté au fichier
     * @param publicFile si VRAI alors le transfert se fait dans le dossier publique
     * @throws IOException
     */
    public void uploadToLocalFileSystem(MultipartFile multipartFile, String fileName, boolean publicFile) throws IOException {

        uploadToLocalFileSystem(multipartFile.getInputStream(), fileName, publicFile);
    }

    /**
     * Transféré dans le dossier d'upload public ou privé selan la valeur de "publicFile"
     *
     * @param inputStream fichier à transférer
     * @param fileName nom qui sera affecté au fichier
     * @param publicFile si VRAI alors le transfert se fait dans le dossier publique
     * @throws IOException
     */
    public void uploadToLocalFileSystem(InputStream inputStream, String fileName, boolean publicFile) throws IOException {

        Path storageDirectory = Paths.get(publicFile ? publicUploadFolder : privateUploadFolder);

        if(!Files.exists(storageDirectory)){
            try {
                Files.createDirectories(storageDirectory);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Path destination = Paths.get(storageDirectory.toString() + "/" + fileName);

        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);

    }

    // Méthode pour vérifier l'extension du fichier
    public boolean isValidExtension(MultipartFile  file, List<String> accptedExtensions) {
        String fileName = file.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return accptedExtensions.contains(fileExtension);
    }

    // Méthode pour vérifier la taille du fichier
    public boolean isValidSize(MultipartFile file, int maxSize) {
        return file.getSize() <= maxSize;
    }

    public byte[] getImageByName(String nomImage, boolean publicFile) throws FileNotFoundException {
        Path storageDirectory = Paths.get(publicFile ? publicUploadFolder : privateUploadFolder, nomImage);

        try {
            return Files.readAllBytes(storageDirectory);
        } catch (IOException e) {
            throw new FileNotFoundException("Le fichier " + nomImage + " n'a pas pu être trouvé ou lu.");
        }
    }

    public ResponseEntity<byte[]> getResponseEntityFromImageName(String imageName, boolean publicFile)
            throws IOException, MimeTypeParseException {

        byte[] image = this.getImageByName(imageName, publicFile);

        HttpHeaders enTete = new HttpHeaders();
        String mimeType = Files.probeContentType(Paths.get(imageName));

        if (mimeType == null) {
            throw new MimeTypeParseException();
        }

        enTete.setContentType(MediaType.valueOf(mimeType));
        return new ResponseEntity<>(image, enTete, HttpStatus.OK);
    }

}