package com.example.lifevault.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String mobile;

    private String mpin; // Hashed MPIN
    private String access_token;
    private String roles; // Comma separated roles, e.g., "ROLE_USER"
    private Date createdAt;
    private Date updatedAt;
    private String status;

    @OneToMany(mappedBy = "usr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDevice> user_devices = new ArrayList<>();

}
