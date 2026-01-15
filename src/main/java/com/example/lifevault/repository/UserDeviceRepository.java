package com.example.lifevault.repository;

import com.example.lifevault.entity.User;
import com.example.lifevault.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByDeviceId(String deviceId);

    List<UserDevice> findByUsr(User user);

    Boolean existsByDeviceId(String deviceId);

    Boolean existsByUsrAndDeviceId(User user, String deviceId);
}
