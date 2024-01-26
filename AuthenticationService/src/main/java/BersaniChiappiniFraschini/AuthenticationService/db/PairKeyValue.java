package BersaniChiappiniFraschini.AuthenticationService.db;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class PairKeyValue {
    @Id
    private String id;
    // @Indexed
    private String key;
    private String value;

    public PairKeyValue(String key, String value){
        this.key = key;
        this.value = value;
    }
}
