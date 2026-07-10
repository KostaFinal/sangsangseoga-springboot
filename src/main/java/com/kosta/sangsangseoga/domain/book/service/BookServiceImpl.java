package com.kosta.sangsangseoga.domain.book.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.dto.BookContentsResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookDetailDto;
import com.kosta.sangsangseoga.domain.book.dto.BookListItemDto;
import com.kosta.sangsangseoga.domain.book.dto.BookListResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPageDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPublishRequestDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPublishResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookRecommendItemDto;
import com.kosta.sangsangseoga.domain.book.dto.BookRecommendResponseDto;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import com.kosta.sangsangseoga.domain.book.entity.BookPage;
import com.kosta.sangsangseoga.domain.book.enums.AgeGroup;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.entity.BookTag;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import com.kosta.sangsangseoga.domain.book.enums.CreationMode;
import com.kosta.sangsangseoga.domain.book.exception.BookErrorCode;
import com.kosta.sangsangseoga.domain.book.repository.BookImageRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookPageRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookTagRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookLikeRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookmarkRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.myLibrary.repository.MyReadingRepository;
import com.kosta.sangsangseoga.domain.subscription.service.UsageService;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final BookLikeRepository bookLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final BookPageRepository bookPageRepository;
    private final MyReadingRepository myReadingRepository;
    private final UsageService usageService;
    private final BookTagRepository bookTagRepository;

    private static final List<String> VALID_SORTS = Arrays.asList("latest", "popular", "likes");

    // QA/테스트 전용 계정. 무료 체험(책 1권) 제한과 무관하게 책을 계속 만들어볼 수 있어야 해서
    // PREMIUM 여부와 상관없이 이 이메일만 예외적으로 무제한 허용한다.
    private static final String UNLIMITED_TEST_EMAIL = "writer@sangsang.com";

    /**
     * 책 생성(최종 저장).
     * FREE 회원은 생애 1회 체험(canStartFreeTrial)을 통과해야 하며, 그 1권 안에서는 페이지 수 제한이 없다.
     * PREMIUM 회원과 UNLIMITED_TEST_EMAIL 계정은 이 체험판 제한과 무관하게 항상 허용한다.
     */
    @Override
    @Transactional
    public BookPublishResponseDto publish(Long memberId, BookPublishRequestDto request) {
        if (memberId == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        boolean isPremium = member.getSubscriptionPlan().isPremium()
                || UNLIMITED_TEST_EMAIL.equals(member.getEmail());
        int pageCount = request.getPages() != null ? request.getPages().size() : 0;

        if (!isPremium && !usageService.canStartFreeTrial(memberId)) {
            throw new CustomException(BookErrorCode.FREE_TRIAL_ALREADY_USED);
        }

        BookType bookType;
        try {
            bookType = BookType.valueOf(request.getBookType());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(BookErrorCode.INVALID_BOOK_TYPE);
        }

        Book book = Book.builder()
                .member(member)
                .bookType(bookType)
                .creationMode(parseEnum(CreationMode.class, request.getCreationMode()))
                .authorAgeGroup(parseEnum(AgeGroup.class, request.getAuthorAgeGroup()))
                .readerAgeGroup(parseEnum(AgeGroup.class, request.getReaderAgeGroup()))
                .title(request.getTitle())
                .description(request.getDescription())
                .confirmedSettings(request.getConfirmedSettings())
                .status(BookStatus.PUBLISHED)
                .pageCount(pageCount)
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .build();
        book = bookRepository.save(book);

        if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().isBlank()) {
            BookImage coverImage = bookImageRepository.save(
                    buildBookImage(book, null, BookImage.ImageType.COVER, request.getCoverImageUrl()));
            book.setCoverImageId(coverImage.getId());
            bookRepository.save(book);
        }

        if (request.getPages() != null) {
            BookPage.ContentType contentType = contentTypeFor(bookType);
            for (BookPublishRequestDto.PageRequest pageRequest : request.getPages()) {
                BookPage page = bookPageRepository.save(BookPage.builder()
                        .book(book)
                        .pageNo(pageRequest.getPageNo())
                        .title(pageRequest.getTitle())
                        .contentType(contentType)
                        .contentTextKo(pageRequest.getContentTextKo())
                        .contentTextEn(pageRequest.getContentTextEn())
                        .imageUrl(pageRequest.getImageUrl())
                        .build());

                if (pageRequest.getImageUrl() != null && !pageRequest.getImageUrl().isBlank()) {
                    bookImageRepository.save(
                            buildBookImage(book, page.getId(), BookImage.ImageType.PAGE, pageRequest.getImageUrl()));
                }
            }
        }

        if (!isPremium) {
            usageService.markFreeTrialUsed(memberId);
        }

        return BookPublishResponseDto.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .status(book.getStatus().name())
                .pageCount(book.getPageCount())
                .build();
    }

    // book.bookType에 따른 book_page.content_type 매핑 (동화=PAGE, 소설=CHAPTER, 시=POEM, 나머지=PAGE)
    private BookPage.ContentType contentTypeFor(BookType bookType) {
        switch (bookType) {
            case FAIRY_TALE:
                return BookPage.ContentType.PAGE;
            case NOVEL:
                return BookPage.ContentType.CHAPTER;
            case POEM:
                return BookPage.ContentType.POEM;
            default:
                return BookPage.ContentType.PAGE;
        }
    }

    private BookImage buildBookImage(Book book, Long bookPageId, BookImage.ImageType imageType, String fileUrl) {
        String fileName = extractFileName(fileUrl);
        return BookImage.builder()
                .book(book)
                .bookPageId(bookPageId)
                .imageType(imageType)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .fileExtension(extractExtension(fileName))
                .build();
    }

    // Replicate 임시 delivery URL(예: .../out-0.webp)에서 file_name/file_extension을 뽑아낸다.
    private String extractFileName(String url) {
        String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        int slashIdx = path.lastIndexOf('/');
        String name = slashIdx >= 0 ? path.substring(slashIdx + 1) : path;
        return name.isBlank() ? "image" : name;
    }

    private String extractExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx >= 0 ? fileName.substring(dotIdx + 1) : null;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }
    }

    /**
     * 책 목록 조회
     * - bookType 필터, 키워드 검색, 정렬, 페이징 지원
     */
    @Override
    public BookListResponseDto getBooks(String bookType, String sort, String keyword, int page, int size, Long memberId) throws Exception {
        if (sort != null && !VALID_SORTS.contains(sort)) {
            throw new CustomException(BookErrorCode.INVALID_SORT);
        }

        BookType bookTypeEnum = null;
        if (bookType != null && !bookType.isBlank()) {
            try {
                bookTypeEnum = BookType.valueOf(bookType);
            } catch (IllegalArgumentException e) {
                throw new CustomException(BookErrorCode.INVALID_BOOK_TYPE);
            }
        }

        Sort sortCondition;
        if ("likes".equals(sort)) {
            sortCondition = Sort.by(Sort.Direction.DESC, "likeCount");
        } else if ("popular".equals(sort)) {
            sortCondition = Sort.unsorted();
        } else {
            sortCondition = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String keywordFilter = (keyword == null || keyword.isBlank()) ? null : keyword;

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortCondition);
        Page<Book> bookPage = "popular".equals(sort)
                ? bookRepository.findBooksByPopular(bookTypeEnum, keywordFilter, pageRequest)
                : bookRepository.findBooks(bookTypeEnum, keywordFilter, pageRequest);

        Member member = (memberId != null) ? memberRepository.findById(memberId).orElse(null) : null;

        List<BookListItemDto> items = new ArrayList<>();
        for (Book book : bookPage.getContent()) {
            String coverImageUrl = bookImageRepository
                    .findByBookAndImageTypeAndDeletedAtIsNull(book, BookImage.ImageType.COVER)
                    .map(BookImage::getFileUrl)
                    .orElse(null);

            boolean isLikedByMe = member != null && bookLikeRepository.existsByMemberAndBook(member, book);

            items.add(BookListItemDto.builder()
                    .id(book.getId())
                    .authorId(book.getMember().getId())
                    .title(book.getTitle())
                    .author(book.getMember().getNickname())
                    .bookType(book.getBookType() != null ? book.getBookType().name() : null)
                    .coverImageUrl(coverImageUrl)
                    .description(book.getDescription())
                    .viewCount(book.getViewCount())
                    .likeCount(book.getLikeCount())
                    .commentCount(book.getCommentCount())
                    .isLikedByMe(isLikedByMe)
                    .build());
        }

        return BookListResponseDto.builder()
                .items(items)
                .totalCount(bookPage.getTotalElements())
                .page(page)
                .hasNext(bookPage.hasNext())
                .build();
    }

    /**
     * 책 상세 조회
     * - 비로그인(memberId=null) 시 isLikedByMe, isBookmarkedByMe = false
     */
    @Override
    @Transactional
    public BookDetailDto getBook(Long bookId, Long memberId) throws Exception {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        String coverImageUrl = bookImageRepository
                .findByBookAndImageTypeAndDeletedAtIsNull(book, BookImage.ImageType.COVER)
                .map(BookImage::getFileUrl)
                .orElse(null);

        boolean isLikedByMe = false;
        boolean isBookmarkedByMe = false;

        if (memberId != null) {
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                isLikedByMe = bookLikeRepository.existsByMemberAndBook(member, book);
                isBookmarkedByMe = bookmarkRepository.existsByMemberAndBook(member, book);
            }
        }

        List<String> tags = bookTagRepository.findByBook(book).stream()
                .map(BookTag::getTagName)
                .collect(Collectors.toList());

        return BookDetailDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getMember().getNickname())
                .authorId(book.getMember().getId())
                .bookType(book.getBookType() != null ? book.getBookType().name() : null)
                .coverImageUrl(coverImageUrl)
                .description(book.getDescription())
                .pageCount(book.getPageCount())
                .viewCount(book.getViewCount())
                .likeCount(book.getLikeCount())
                .commentCount(book.getCommentCount())
                .isLikedByMe(isLikedByMe)
                .isBookmarkedByMe(isBookmarkedByMe)
                .tags(tags)
                .createdAt(book.getCreatedAt())
                .build();
    }

    /**
     * 책 읽기 시작 시 조회수 증가
     */
    @Override
    @Transactional
    public Integer increaseViewCount(Long bookId) throws Exception {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        book.setViewCount(book.getViewCount() + 1);
        book.setWeekViewCount(book.getWeekViewCount() + 1);

        return book.getViewCount();
    }

    /**
     * 책 본문(페이지) 조회
     */
    @Override
    public BookContentsResponseDto getContents(Long bookId) throws Exception {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        List<BookPage> pages = bookPageRepository.findByBookOrderByPageNoAsc(book);

        List<BookPageDto> items = new ArrayList<>();
        for (BookPage page : pages) {
            items.add(BookPageDto.builder()
                    .id(page.getId())
                    .pageNo(page.getPageNo())
                    .title(page.getTitle())
                    .contentType(page.getContentType())
                    .contentTextKo(page.getContentTextKo())
                    .contentTextEn(page.getContentTextEn())
                    .imageUrl(page.getImageUrl())
                    .build());
        }

        return BookContentsResponseDto.builder()
                .items(items)
                .build();
    }

    /**
     * 함께 읽기 좋은 작품 추천 (협업 필터링)
     * - 이 책을 읽은 사람들이 함께 읽은 다른 책 중 많이 겹치는 것 우선
     * - 협업 필터링 결과가 size보다 부족하면 같은 bookType 좋아요 순으로 채움
     */
    @Override
    public BookRecommendResponseDto getRecommendations(Long bookId, int size) throws Exception {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        // 1. 협업 필터링으로 추천
        List<Book> recommended = myReadingRepository.findCollaborativeRecommendations(
                book, PageRequest.of(0, size));

        // 2. 부족하면 같은 bookType 좋아요 순으로 채움
        if (recommended.size() < size) {
            List<Long> excludeIds = new ArrayList<>();
            excludeIds.add(bookId);
            recommended.forEach(b -> excludeIds.add(b.getId()));

            int remaining = size - recommended.size();
            List<Book> fallback = bookRepository.findRecommendations(
                    book.getBookType(), bookId, PageRequest.of(0, size));

            for (Book fb : fallback) {
                if (!excludeIds.contains(fb.getId())) {
                    recommended.add(fb);
                    excludeIds.add(fb.getId());
                    if (recommended.size() >= size) break;
                }
            }
        }

        List<BookRecommendItemDto> items = new ArrayList<>();
        for (Book rec : recommended) {
            String coverImageUrl = bookImageRepository
                    .findByBookAndImageTypeAndDeletedAtIsNull(rec, BookImage.ImageType.COVER)
                    .map(BookImage::getFileUrl)
                    .orElse(null);

            items.add(BookRecommendItemDto.builder()
                    .id(rec.getId())
                    .title(rec.getTitle())
                    .author(rec.getMember().getNickname())
                    .bookType(rec.getBookType() != null ? rec.getBookType().name() : null)
                    .coverImageUrl(coverImageUrl)
                    .description(rec.getDescription())
                    .viewCount(rec.getViewCount())
                    .likeCount(rec.getLikeCount())
                    .commentCount(rec.getCommentCount())
                    .build());
        }

        return BookRecommendResponseDto.builder()
                .items(items)
                .build();
    }
    
    @Override
    public BookListResponseDto getMyBooks(Long memberId) throws Exception {
        if (memberId == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }

        List<Book> myBooks = bookRepository.findByMember_IdAndStatus(memberId, BookStatus.PUBLISHED);

        List<BookListItemDto> items = new ArrayList<>();

        for (Book book : myBooks) {
            String coverImageUrl = bookImageRepository
                    .findByBookAndImageTypeAndDeletedAtIsNull(book, BookImage.ImageType.COVER)
                    .map(BookImage::getFileUrl)
                    .orElse(null);

            items.add(BookListItemDto.builder()
                    .id(book.getId())
                    .authorId(book.getMember().getId())
                    .title(book.getTitle())
                    .author(book.getMember().getNickname())
                    .bookType(book.getBookType() != null ? book.getBookType().name() : null)
                    .coverImageUrl(coverImageUrl)
                    .description(book.getDescription())
                    .viewCount(book.getViewCount())
                    .likeCount(book.getLikeCount())
                    .commentCount(book.getCommentCount())
                    .isLikedByMe(false)
                    .build());
        }

        return BookListResponseDto.builder()
                .items(items)
                .totalCount((long) items.size())
                .page(1)
                .hasNext(false)
                .build();
    }
}