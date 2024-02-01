package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.Optional;


public interface TournamentRepository extends MongoRepository<Tournament, String> {
    boolean existsByTitle(String title);
    @Query("{ 'is_open': true }")
    Collection<Tournament> findOpenTournaments();

    @Query("{ 'educators.username':  ?0 }")
    Collection<Tournament> findTournamentsByEducator(String educator_username);

    @Query("{ 'subscribed_users.username':  ?0 }")
    Collection<Tournament> findTournamentsByStudent(String student_username);

    @Query("{ 'title': ?0 }") // Exact match search
    Tournament findTournamentByTitle(String title);

    @Query(" { 'title': {$regex : ?0, $options: 'i'}}")
    Collection<Tournament> findByTitleSearch(String tournamentTitle);

    @Query(" { '$and': [ { 'subscribed_users.username': ?0 }, { 'title': ?1 } ] }")
    Optional<Tournament> findBySubscribed_user(String username, String titleTournament);


}
