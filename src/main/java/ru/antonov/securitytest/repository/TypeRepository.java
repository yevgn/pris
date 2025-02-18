package ru.antonov.securitytest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.antonov.securitytest.entity.Type;

@Repository
public interface TypeRepository extends JpaRepository<Type, Integer> {
}
