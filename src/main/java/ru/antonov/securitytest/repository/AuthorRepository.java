package ru.antonov.securitytest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.antonov.securitytest.entity.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
}
