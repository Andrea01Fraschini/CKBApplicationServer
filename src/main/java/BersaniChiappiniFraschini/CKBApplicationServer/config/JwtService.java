package BersaniChiappiniFraschini.CKBApplicationServer.config;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.BattleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;


/**
 * Service that deals with JWT creation and validation
 */
@Service
public class JwtService {
    private static final String SECRET_KEY = "bc2769b2ea3ce1a069f8a2d5a4c84ffa87c1d38ce267e4ba54293748c35834f3";
    final Integer TOKEN_DURATION_MILLIS = 1000 * 60 * 60 * 24 * 7;
    public String generateJWT(UserDetails userDetails){
        //return generateJWT(userDetails, new HashMap<>());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TOKEN_DURATION_MILLIS))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateJWT(String group_id){
        //return generateJWT(userDetails, new HashMap<>());
        return Jwts.builder()
                .subject(group_id)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TOKEN_DURATION_MILLIS))
                .signWith(getSecretKey())
                .compact();
    }

    public boolean isValid(String token, UserDetails userDetails){
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isExpired(token));
    }


    public boolean isExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date(System.currentTimeMillis()));
    }

    /**
     * Extracts username from a JWT
     * @param token JSON Web Token
     * @return String under "username" field saved in the claims of the token
     */
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration date from a JWT
     * @param token JSON Web Token
     * @return Expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> extractor){
        Claims claims = extractClaims(token);
        return extractor.apply(claims);
    }

    public Claims extractClaims(String token){
        var parser = Jwts.parser().verifyWith(getSecretKey()).build();
        return parser.parseSignedClaims(token).getPayload();
    }

    private SecretKey getSecretKey() {
        byte[] KEY = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(KEY);
    }
}
