package com.accounted.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.accounted.bean.User;

@Repository("UserRepository")
public interface UserRepository extends JpaRepository<User, String> {
  public User findByEmailAndPassword(String email, String password);
}