package com.example.demo;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        //example of key => username, email of the user or id of the group
        //all values are encrypt in hash-256 look at the SHA256 class

        //here It controls if the pair key1 and key2 already exists
        if(control(username) && control(email)){
            String hashUser = SHA256.hashSHA256(username);
            String hashEmail = SHA256.hashSHA256(email);

            if(hashUser != null && hashEmail!= null) {
                PairKeyValue pairKeyValue = new PairKeyValue(hashUser, hashEmail);
                // pairKeyValueRepository.insert(pairKeyValue);
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
        // search to login
        // pairKeyValueRepository.findPairKeyValueByKey()
        return new MessageReturn(0,  null);
    }

    private Boolean control(String key){
        Optional<PairKeyValue> pair = pairKeyValueRepository.findPairKeyValueByKey(key);

        return pair.isEmpty();
    }
}
