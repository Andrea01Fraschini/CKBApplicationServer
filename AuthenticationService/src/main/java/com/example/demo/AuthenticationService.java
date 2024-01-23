package com.example.demo;

import com.example.demo.db.PairKeyValue;
import com.example.demo.db.PairKeyValueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@AllArgsConstructor
@Service
public class AuthenticationService {

    private final PairKeyValueRepository pairKeyValueRepository;

    /**
     * Method to insert new key value in the db, but first it'll check if the kay already exists
     * in this case the user can log in with username or e-mail
     *
     * @param username
     * @param email
     * @param password
     */
    public MessageReturn insertNewAccount(String username, String email, String password) {
        String hashUser = SHA256.hashSHA256(username);
        String hashEmail = SHA256.hashSHA256(email);
        String hashPassword = SHA256.hashSHA256(password);

        //example of key => username, email of the user or id of the group
        //all values are encrypt in hash-256 look at the SHA256 class

        //here It controls if the pair key1 and key2 already exists
        if(control(hashUser) && control(hashEmail)){

            if(hashUser != null && hashEmail!= null && hashPassword != null) {
                PairKeyValue user = new PairKeyValue(hashUser, hashPassword);
                PairKeyValue emailData = new PairKeyValue(hashEmail, hashPassword);

                pairKeyValueRepository.insert(user);
                pairKeyValueRepository.insert(emailData);

                return new MessageReturn(200, "OK");
            }else {
                return new MessageReturn(404, "Hashing process didn't work");
            }
        }else{
            return new MessageReturn(400, "Value already exists");
        }
    }

    /**
     * Return true if the authentication is correct false otherwise
     * this is for the authentication
     * @param key
     */
    public MessageReturn authentication(String key, String value){
        String hashKey = SHA256.hashSHA256(key);
        String hashValue = SHA256.hashSHA256(value);

        if( hashKey == null && hashValue == null){
            // qui ritornare errore interno => con un http....
            //TODO POSSO SOTITUIRLO CON UN'ECCEZIONE CHE MANDO IO VEDI CARTELLA SECURITY => SI FARE COSì
            return new MessageReturn(404, "Hashing process didn't work");
        }

        Optional<PairKeyValue> pairKeyValue = pairKeyValueRepository.findPairKeyValueByKey(hashKey);

        if(pairKeyValue.isPresent()) {
            if (pairKeyValue.get().getValue().equals(hashValue)) {
                return new MessageReturn(200, "OK");
            }
        }

        return new MessageReturn(205, "KO");

    }

    public MessageReturn createAPIAuthToken(String id){
        String hashId = SHA256.hashSHA256(id);

        if(hashId == null){
            return new MessageReturn(404, "Hashing process didn't work");
        }

        if(!control(hashId)){
            return new MessageReturn(400, "token already exists");
        }

        Random random = new Random();
        String hashToken = SHA256.hashSHA256(id+String.valueOf(random.nextInt()));

        //manda un errore
        assert hashToken != null;
        PairKeyValue pairKeyValue = new PairKeyValue(hashId,  SHA256.hashSHA256(hashToken));

        pairKeyValueRepository.insert(pairKeyValue);

        return new MessageReturn(200, hashToken);
    }

    private Boolean control(String key){
        Optional<PairKeyValue> pair = pairKeyValueRepository.findPairKeyValueByKey(key);
        return pair.isEmpty();
    }
}
