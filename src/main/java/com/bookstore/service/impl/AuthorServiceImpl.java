package com.bookstore.service.impl;

import com.bookstore.dto.AuthorDto;
import com.bookstore.entity.Author;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.service.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public Page<AuthorDto> getAuthors(String keyword, Pageable pageable) {
        String query = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Author> authorPage = authorRepository.searchAuthors(query, pageable);
        return authorPage.map(this::mapToDto);
    }

    @Override
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorDto getAuthorById(Long id) {
        Author author = getAuthorEntityById(id);
        return mapToDto(author);
    }

    @Override
    public Author getAuthorEntityById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tác giả với ID: " + id));
    }

    @Override
    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        if (authorRepository.existsByNameIgnoreCase(authorDto.getName().trim())) {
            throw new IllegalArgumentException("Tác giả với tên '" + authorDto.getName() + "' đã tồn tại trong hệ thống");
        }
        Author author = Author.builder()
                .name(authorDto.getName().trim())
                .biography(authorDto.getBiography())
                .build();
        Author saved = authorRepository.save(author);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorDto authorDto) {
        Author author = getAuthorEntityById(id);
        author.setName(authorDto.getName().trim());
        author.setBiography(authorDto.getBiography());
        Author saved = authorRepository.save(author);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = getAuthorEntityById(id);
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            throw new IllegalStateException("Không thể xóa tác giả '" + author.getName() + "' vì đang có " 
                    + author.getBooks().size() + " đầu sách liên kết!");
        }
        authorRepository.delete(author);
    }

    @Override
    public List<AuthorDto> searchAuthors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllAuthors();
        }
        return authorRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AuthorDto mapToDto(Author author) {
        long bookCount = (author.getBooks() != null) ? author.getBooks().size() : 0;
        return AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .bookCount(bookCount)
                .build();
    }
}
