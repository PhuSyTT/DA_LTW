package com.bookstore.service;

import com.bookstore.dto.AuthorDto;
import com.bookstore.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuthorService {
    Page<AuthorDto> getAuthors(String keyword, Pageable pageable);
    List<AuthorDto> getAllAuthors();
    AuthorDto getAuthorById(Long id);
    Author getAuthorEntityById(Long id);
    AuthorDto createAuthor(AuthorDto authorDto);
    AuthorDto updateAuthor(Long id, AuthorDto authorDto);
    void deleteAuthor(Long id);
    List<AuthorDto> searchAuthors(String query);
}
