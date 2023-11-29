package com.kimbos.onlinecommunity.repository;

import com.kimbos.onlinecommunity.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
}
