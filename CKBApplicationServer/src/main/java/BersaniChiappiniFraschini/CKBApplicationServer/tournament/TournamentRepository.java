package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;


public interface TournamentRepository extends MongoRepository<Tournament, String> {
    boolean existsByTitle(String title);
    @Query("{ is_open: true }")
    Collection<Tournament> findOpenTournaments();

    @Query("{ \"educators.username\":  educator_username }")
    Collection<Tournament> findTournamentsByEducator(String educator_username);

    @Query("{ \"subscribed_users.username\":  student_username }")
    Collection<Tournament> findTournamentsByStudent(String student_username);
}
