package edu.ban7.springdemo.upload.controller;

import edu.ban7.springdemo.upload.annotation.ValidFile;
import edu.ban7.springdemo.upload.dao.DocumentDao;
import edu.ban7.springdemo.upload.dao.UserDao;
import edu.ban7.springdemo.upload.model.Document;
import edu.ban7.springdemo.upload.model.User;
import edu.ban7.springdemo.upload.service.FileService;
import edu.ban7.springdemo.upload.service.ImageResizer;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.datatransfer.MimeTypeParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
public class UserController {

    protected FileService fileService;
    protected ImageResizer imageResizer;
    protected UserDao userDao;
    protected DocumentDao documentDao;

    @Autowired
    public UserController(FileService fileService, ImageResizer imageResizer, UserDao userDao, DocumentDao documentDao) {
        this.fileService = fileService;
        this.imageResizer = imageResizer;
        this.userDao = userDao;
        this.documentDao = documentDao;
    }

    /**
     * Enregistrement d'un utilisateur envoyé au format JSON
     * Avec un fichier représentant son avatar et une liste de document
     * La requête est à envoyer à l'aide d'un FormData
     *
     * @param user
     * @param avatar
     * @param documents
     * @return
     */
    @PostMapping(path = "/utilisateur")
    public ResponseEntity<User> save(
            @RequestPart("user") @Valid User user,
            @RequestPart(value = "avatar", required = false) @ValidFile(maxSize = 2 * 1024 * 1024) MultipartFile avatar,
            @RequestPart(value = "documents", required = false) @ValidFile(acceptedTypes = {"image/jpeg","image/png","application/pdf", "application/msword"}) MultipartFile[] documents) throws IOException {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

        //Afin d'obtenir un nom de fichier optimal :
        // - Il est nécessaire de trouver un nom unique au document, l'ajout d'un UUID permet de s'en assurer
        // - Il peut être intéressant de préfixer le nom du fichier par la date du jour afin de faciliter le tri
        // - Il est également possible d'ajouter des informations sur l'entité auquel le document est relié (un produit, un utilisateur...)
        // - Enfin, il est nécessaire de conserver l'extension du fichier (en faisant une extraction de l'extension
        //     via une sous chaine de la dernière chaine de texte aprés le dernier point su no du fichier
        //     ou en ajoutant le nom complet du fichier à la fin

        if (avatar != null) {

            InputStream resizedAvatar = imageResizer.resizeAndCrop(avatar, 1000, 1000, ImageResizer.ResizeMode.COVER);

            String avatarFileName = date + "_" + user.getPseudo() + "_" + UUID.randomUUID() + "_" + avatar.getOriginalFilename();
            fileService.uploadToLocalFileSystem(resizedAvatar, avatarFileName, true);

            user.setAvatarFileName(avatarFileName);
        }

        userDao.save(user);

        if (documents != null) {
            for (MultipartFile documentFile : documents) {
                String documentName = date + "_" + user.getPseudo() + "_" + UUID.randomUUID() + "_" + documentFile.getOriginalFilename();
                fileService.uploadToLocalFileSystem(documentFile, documentName, false);

                Document documentModel = new Document();
                documentModel.setUri(documentName);
                documentModel.setOwner(user);
                documentDao.save(documentModel);
            }
        }

        return new ResponseEntity<>(user, HttpStatus.OK);

    }

    @GetMapping("/image-profil/{idUtilisateur}")
    public ResponseEntity<byte[]> getImageProfil(@PathVariable int idUtilisateur) {

        //Note : ajouter Spring JPA et un DAO pour utilisateur
        //Optional<User> optional = userDao.findById(idUtilisateur);
        User fauxUtilisateur = new User();
        fauxUtilisateur.setAvatarFileName("mon_avatar.jpg");
        Optional<User> optional = Optional.of(fauxUtilisateur);

        if (optional.isPresent()) {

            try {
                return fileService.getResponseEntityFromImageName(
                        optional.get().getAvatarFileName(), true);

            } catch (FileNotFoundException e) {
                //image introuvable
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (IOException | MimeTypeParseException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        //utilisateur introuvable
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
