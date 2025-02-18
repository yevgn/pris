package ru.antonov.securitytest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.antonov.securitytest.entity.Book;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    void deleteByName(String name);
    Optional<Book> findByName(String name);
}
