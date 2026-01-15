package com.example.lifevault.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user_device")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY fetch type for performance
    @JoinColumn(name = "user_id", nullable = false)
    private User usr;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;
    private Date createdAt;
    private Date updatedAt;
    private String status;
}
