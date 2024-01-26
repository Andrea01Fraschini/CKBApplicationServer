package BersaniChiappiniFraschini.AuthenticationService.persistence;

import lombok.*;

import jakarta.persistence.*;


@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "keyValue", schema = "AUTH")
public class PairKeyValue {

    @Id
    @Column(name = "key_")
    private String key;
    @Column(name = "value_")
    private String value;
}
