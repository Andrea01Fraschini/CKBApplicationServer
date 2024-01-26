package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;


public interface TournamentRepository extends MongoRepository<Tournament, String> {
    boolean existsByTitle(String title);
    @Query("{ 'is_open': true }")
    Collection<Tournament> findOpenTournaments();

    @Query("{ 'educators.username':  ?0 }")
    Collection<Tournament> findTournamentsByEducator(String educator_username);

    @Query("{ 'subscribed_users.username':  ?0 }")
    Collection<Tournament> findTournamentsByStudent(String student_username);

    @Query("{ 'title': ?0 }")
    Tournament findTournamentByTitle(String title);


}
