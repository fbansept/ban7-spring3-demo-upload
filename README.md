# AVANT PROPOS

### Fichiers publiques / privés

Le projet gère 2 dossiers différents : un pour les images publiques et l'autre pour les images privées

A noter qu'il est préférable de faire en sorte que les images publiques n'est pas besoin d'être récupérée via spring en y accédant via un serveur statique (type Nginx)

### Contraintes des fichiers : poids, extensions

Il est possible d'ajouter un reverse-proxy (ex : Nginx) afin d'établir des contraintes en amont :
* requête par seconde 
* poids
* type de fichier

Faites attention s'il y a différentes limites :
* Au niveau du reverse proxy (ex : client_max_body_size sur Nginx)
* Au niveau de Spring globalement (ex : spring.servlet.multipart.max-file-size dans application.properties)
* Au niveau des contrôleurs (ex : l'annotation @ValidFile qui existe dans ce projet)
* Si l'application front, met en place également des limites
* Si il y a des restrictions réseaux particulières

On peut résumé l'ordre de priorité comme ceci :
Réseau > Reverse proxy > Config Spring > Contrôleur > Front end

# FICHIER

## service/FileService

Contient toute la logique concernant l'enregistrement et la lecture des fichiers.
Il utilise les propriétés :
* public.upload.folder 
* private.upload.folder 

du fichier application.properties, afin de déterminer ou seront stockés les fichiers privés et publiques.

Note : Comme vu précédemment, les images publiques devrait être déplacés dans un dossier accessible en lecture à un serveur statique, alors que les fichier privés devraient rester accessibles à spring et uniquement à spring

## controller/UserController

Contient 2 exemple :
* envoie d'une requête contenant à la fois le json d'un utilisateur, mais également des fichiers avec des contraintes de type et de poids différentes
* récupération d'une image en fonction de l'id d'un utilisateur

## le package annotation et ses fichiers

* FileConfig : charge les valeur d'application.properties 
* FileValidationUtil : logique appliques par FileValidator et ArrayFileValidator
* FileValidator : logique de l'annotation @ValidFile lorsqu'elle concerne 1 fichier
* ArrayFileValidator : logique de l'annotation @ValidFile lorsqu'elle concerne N fichiers
* L'annotation @ValidFile

L'annotation ValidFile à 2 paramètres spécifiques :
* maxSize (limite la taille en octet d'un fichier
* acceptedTypes (limite les types acceptés, utilise "file.default.accepted.types" de "application.properties" si aucune valeur n'est renseignées)

A noter que ces restrictions ne peuvent donner moins de contraintes que les composant supérieures (voir ci-dessus)

## Fichiers de test unitaire / composant