package BersaniChiappiniFraschini.AuthenticationService.persistence;

import lombok.*;

import jakarta.persistence.*;


@Data
@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "keyValue", schema = "AUTH")
public class PairKeyValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "key_")
    private String key;
    @Column(name = "value_")
    private String value;

    public PairKeyValue(String key, String value){
        this.key = key;
        this.value = value;
    }
}
