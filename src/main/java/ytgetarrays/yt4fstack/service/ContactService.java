package ytgetarrays.yt4fstack.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ytgetarrays.yt4fstack.model.Contact;
import ytgetarrays.yt4fstack.repo.ContactRepo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ytgetarrays.yt4fstack.constant.Constant.PHOTO_DIRECTORY;


@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepo contactRepo;

    public Page<Contact> getAllContacts(int page, int size){
        return contactRepo.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id){
        return contactRepo.findById(id).orElseThrow(()-> new RuntimeException("Contact not found"));
    }

    public Contact createContact(Contact contact){
        return contactRepo.save(contact);
    }

    public void deleteContact(String id){
        contactRepo.deleteById(id);
    }

    public String uploadPhoto(String id, MultipartFile file){
        log.info("Uploading photo for user id : {}", id);
        Contact contact = getContact(id);
        String photoUrl = "";
        contact.setPhotoUrl(photoUrl);
        contactRepo.save(contact);
        return photoUrl;
    }

    private final Function<String, String> fileExtension = fileName ->
      Optional.of(fileName).filter(name -> name.contains("."))
              .map(name-> "." + name.substring(fileName.lastIndexOf(".") + 1)).orElse(".png");


    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) ->{
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try{
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocation)) { Files.createDirectories(fileStorageLocation); }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(id + fileExtension.apply(image.getOriginalFilename())), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/" + id + fileExtension.apply(image.getOriginalFilename())).toUriString();
        } catch (Exception ex){
            throw new RuntimeException("unable to save image");
        }
    };

}
