package ytgetarrays.yt4fstack.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ytgetarrays.yt4fstack.model.Contact;

import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact, String> {

    Optional<Contact> findById(String id);
}
