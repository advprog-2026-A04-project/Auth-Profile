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
    private String role = "TITIPER";

    @Column(nullable = false)
    private String kycStatus = "NOT_SUBMITTED";

    @Column
    private String kycDocumentUrl;

    @Column
    private String kycNote;

    @Column(nullable = false)
    private boolean banned = false;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int completedOrders = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int ratingCount = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int ratingTotal = 0;
}
