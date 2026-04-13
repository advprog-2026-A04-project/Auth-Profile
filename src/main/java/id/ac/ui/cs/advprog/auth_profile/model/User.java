package id.ac.ui.cs.advprog.auth_profile.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String username;

    @Column
    private String fullName;

    @Column
    private String role = "TITIPERS";

    @Enumerated(EnumType.STRING)
    @Column
    private KycStatus kycStatus = KycStatus.NONE;

    @Column
    private String kycFullName;

    @Column
    private String kycNik;

    @Column
    private String kycPhoneNumber;

    @Column(nullable = false)
    private boolean banned = false;
}