package BersaniChiappiniFraschini.AuthenticationService.service;

import BersaniChiappiniFraschini.AuthenticationService.returnMessage.MessageReturn;
import BersaniChiappiniFraschini.AuthenticationService.returnMessage.ReturnCode;
import BersaniChiappiniFraschini.AuthenticationService.SHA256;
import BersaniChiappiniFraschini.AuthenticationService.persistence.PairKeyValue;
import BersaniChiappiniFraschini.AuthenticationService.persistence.PairKeyValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {

    private final PairKeyValueRepository pairKeyValueRepository;

    @Autowired
    public AuthenticationService(PairKeyValueRepository pairKeyValueRepository) {
        this.pairKeyValueRepository = pairKeyValueRepository;
    }

    /**
     * Inserts a new account into the system with the provided information.
     *
     * @param username The username for the new account.
     * @param email    The email address associated with the new account.
     * @param password The password for the new account.
     * @return A MessageReturn object representing the result of the operation.
     *         - If the insertion is successful, it returns a success message.
     *         - If hashing the password fails, it returns an error message.
     *         - If the username or email already exists in the system, it returns an error message.
     */
    @Transactional
    public MessageReturn insertNewAccount(String username, String email, String password) {
        String hashPassword = SHA256.hashSHA256(password);

        if(control(username) && control(email)){

            if(hashPassword != null) {
                PairKeyValue user = PairKeyValue.builder().key(username).value(hashPassword).build();
                PairKeyValue emailData = PairKeyValue.builder().key(email).value(hashPassword).build();

                pairKeyValueRepository.save(user);
                pairKeyValueRepository.save(emailData);

                return new MessageReturn(ReturnCode.SUCCESS.getDefaultMessage(), "OK");
            }else {
                return new MessageReturn(ReturnCode.NOT_WORK_HASHING.getDefaultMessage(), "Hashing process didn't work");
            }
        }else{
            return new MessageReturn(ReturnCode.ALREADY_EXISTS.getDefaultMessage(), "Value already exists");
        }
    }

    /**
     * Authenticates a user by verifying the provided key-value pair against stored data.
     *
     * @param key   The key used for authentication (e.g., username or email).
     * @param value The value associated with the provided key (e.g., password).
     * @return A MessageReturn object representing the result of the authentication attempt.
     *         - If the hashing of the provided value fails, it returns an error message.
     *         - If the provided key is found and the hashed value matches, it returns a success message.
     *         - If the authentication fails (key not found or hashed values do not match), it returns a failure message.
     */
    @Transactional
    public MessageReturn authentication(String key, String value){
        String hashValue = SHA256.hashSHA256(value);

        if(hashValue == null){
            return new MessageReturn(ReturnCode.NOT_WORK_HASHING.getDefaultMessage(), "Hashing process didn't work");
        }

        Optional<PairKeyValue> pairKeyValue = pairKeyValueRepository.findPairKeyValueByKey(key);

        if(pairKeyValue.isPresent()) {
            if (pairKeyValue.get().getValue().equals(hashValue)) {
                return new MessageReturn(ReturnCode.SUCCESS.getDefaultMessage(), "OK");
            }
        }

        return new MessageReturn(ReturnCode.FAILED.getDefaultMessage(), "KO");

    }

    /**
     * Creates an API authentication token for the provided group identifier.
     *
     * @param id The group identifier for whom the API authentication token is created.
     * @return A MessageReturn object representing the result of the token creation.
     *         - If the token is successfully created and stored, it returns a success message with the generated token.
     *         - If hashing the token or user identifier fails, it returns an error message.
     *         - If the user identifier already exists, it returns an error message indicating that the token already exists.
     */
    @Transactional
    public MessageReturn createAPIAuthToken(String id){
        Random random = new Random();

        String hashToken = SHA256.hashSHA256(id+String.valueOf(random.nextInt()));

        if(hashToken == null){
            return new MessageReturn(ReturnCode.NOT_WORK_HASHING.getDefaultMessage(), "Hashing process didn't work");
        }

        if(!control(id)){
            return new MessageReturn(ReturnCode.ALREADY_EXISTS.getDefaultMessage(), "token already exists");
        }

        // PairKeyValue pairKeyValue = PairKeyValue.builder().key(id).value(SHA256.hashSHA256(hashToken)).build();
        PairKeyValue pairKeyValue = new PairKeyValue(id, SHA256.hashSHA256(hashToken));
        pairKeyValueRepository.save(pairKeyValue);

        return new MessageReturn(ReturnCode.SUCCESS.getDefaultMessage(), hashToken);
    }

    private Boolean control(String key){
        Optional<PairKeyValue> pair = pairKeyValueRepository.findPairKeyValueByKey(key);
        return pair.isEmpty();
    }
}
