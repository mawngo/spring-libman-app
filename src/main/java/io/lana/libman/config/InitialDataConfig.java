package io.lana.libman.config;

import com.github.javafaker.Faker;
import io.lana.libman.core.book.Book;
import io.lana.libman.core.book.BookBorrow;
import io.lana.libman.core.book.BookInfo;
import io.lana.libman.core.book.Income;
import io.lana.libman.core.book.repo.BookBorrowRepo;
import io.lana.libman.core.book.repo.BookInfoRepo;
import io.lana.libman.core.book.repo.BookRepo;
import io.lana.libman.core.book.repo.IncomeRepo;
import io.lana.libman.core.reader.Reader;
import io.lana.libman.core.reader.ReaderRepo;
import io.lana.libman.core.tag.*;
import io.lana.libman.core.tag.repo.*;
import io.lana.libman.core.user.User;
import io.lana.libman.core.user.UserRepo;
import io.lana.libman.core.user.role.Permission;
import io.lana.libman.core.user.role.PermissionRepo;
import io.lana.libman.core.user.role.Role;
import io.lana.libman.core.user.role.RoleRepo;
import io.lana.libman.support.data.Gender;
import io.lana.libman.support.data.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.lana.libman.core.user.role.Authorities.User.SYSTEM;

@Component
@RequiredArgsConstructor
@Slf4j
class InitialDataConfig implements ApplicationRunner {
    private final Faker faker = Faker.instance();

    private final UserRepo userRepo;

    private final PermissionRepo permissionRepo;

    private final RoleRepo roleRepo;

    private final AuthorRepo authorRepo;

    private final PublisherRepo publisherRepo;

    private final GenreRepo genreRepo;

    private final SeriesRepo seriesRepo;

    private final ShelfRepo shelfRepo;

    private final BookInfoRepo bookInfoRepo;

    private final BookRepo bookRepo;

    private final ReaderRepo readerRepo;

    private final IncomeRepo incomeRepo;

    private final BookBorrowRepo bookBorrowRepo;

    private final PasswordEncoder passwordEncoder;

    private final ConfigFacade config;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Generating roles and permissions");
        initPermission();
        log.info("Generating users and readers");
        initUser();
        log.info("Generating shelf and tags");
        initShelf();

        // fake data
        if (config.isFakeDataEnabled()) {
            initTag();
            log.info("Generating books");
            initBook();
            log.info("Generating borrow tickets");
            initBookBorrow();
            log.info("Generating favorites");
            initFavorites();
        }
        log.info("Generation completed");
    }


    @Transactional
    void initPermission() {
        if (permissionRepo.count() == 0) {
            permissionRepo.saveAll(Permission.builtIns());
            permissionRepo.saveAll(Permission.forWriteOnly(Author.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forWriteOnly(Genre.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forWriteOnly(Publisher.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forWriteOnly(Series.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forWriteOnly(Shelf.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forWriteOnly(BookInfo.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forWriteOnly(Book.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forReadWrite(BookBorrow.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forReadWrite(User.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forReadWrite(Permission.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forReadWrite(Role.class.getSimpleName()));
            permissionRepo.saveAll(Permission.forReadWrite(Reader.class.getSimpleName()));
        }

        if (roleRepo.count() == 0) {
            final var librarian = Role.librarian();
            final var librarianPerms = librarian.getPermissions();
            librarianPerms.addAll(Permission.forWriteOnly(Author.class.getSimpleName()));
            librarianPerms.addAll(Permission.forWriteOnly(Genre.class.getSimpleName()));
            librarianPerms.addAll(Permission.forWriteOnly(Publisher.class.getSimpleName()));
            librarianPerms.addAll(Permission.forWriteOnly(Series.class.getSimpleName()));
            librarianPerms.addAll(Permission.forWriteOnly(Shelf.class.getSimpleName()));
            librarianPerms.addAll(Permission.forWriteOnly(BookInfo.class.getSimpleName()));
            librarianPerms.addAll(Permission.forWriteOnly(Book.class.getSimpleName()));
            librarianPerms.addAll(Permission.forReadWrite(BookBorrow.class.getSimpleName()));
            librarianPerms.addAll(Permission.forReadWrite(Reader.class.getSimpleName()));
            roleRepo.saveAll(List.of(Role.admin(), Role.force(), librarian));

            final Set<Permission> amPerms = new HashSet<>();
            amPerms.addAll(Permission.forReadWrite(Reader.class.getSimpleName()));
            amPerms.addAll(Permission.forReadWrite(User.class.getSimpleName()));
            amPerms.addAll(Permission.forReadWrite(Role.class.getSimpleName()));
            amPerms.addAll(Permission.forReadWrite(Permission.class.getSimpleName()));
            roleRepo.save(Role.ofName("AM", amPerms));
        }
    }

    @Transactional
    void initUser() {
        if (userRepo.count() > 0) return;
        final var password = passwordEncoder.encode("123456");

        userRepo.saveAll(List.of(
                User.newInstance("SYSTEM", "SYSTEM", passwordEncoder.encode(IdUtils.newTimeSortableId()), Collections.emptyList(), SYSTEM),
                User.newInstance("admin@admin", "admin", password, Collections.singleton(Role.admin()), SYSTEM),
                User.newInstance("librarian@librarian", "librarian", password, Collections.singleton(Role.librarian()), SYSTEM)
        ));

        if (!config.isFakeDataEnabled()) return;

        final var vnFaker = Faker.instance(new Locale("vi", "VN"));
        Stream.generate(() -> faker.internet().emailAddress())
                .distinct()
                .limit(100)
                .forEach(email -> {
                    final var user = new User();
                    user.setCreatedBy(SYSTEM);
                    user.setPassword(password);
                    user.setUsername(email);
                    user.setEmail(email);
                    user.setFirstName(faker.name().firstName());
                    user.setLastName(faker.name().lastName());
                    user.setPhone(vnFaker.phoneNumber().phoneNumber());
                    if (faker.bool().bool()) {
                        user.setAddress(faker.address().fullAddress());
                        user.setGender(faker.options().option(Gender.values()));
                        user.setDateOfBirth(faker.date().birthday(10, 45).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    }

                    if (faker.number().numberBetween(0, 10) < 2) {
                        user.setRoles(Set.of(Role.librarian()));
                        userRepo.save(user);
                        return;
                    }
                    final var reader = new Reader();
                    reader.setAccount(user);
                    reader.setBorrowLimit(faker.number().numberBetween(3, 12));
                    reader.setCreatedBy(SYSTEM);
                    readerRepo.save(reader);
                });

        // Create default reader
        final var reader = new Reader();
        reader.setAccount(User.newInstance("reader@reader", "reader", password, Collections.emptyList(), SYSTEM));
        reader.setBorrowLimit(faker.number().numberBetween(8, 16));
        reader.setCreatedBy(SYSTEM);
        readerRepo.save(reader);
    }

    @Transactional
    void initTag() {
        if (authorRepo.count() == 0) {
            Stream.generate(() -> faker.book().author())
                    .distinct()
                    .limit(20)
                    .forEach(name -> {
                        final var author = new Author();
                        author.setName(name);
                        author.setCreatedBy(SYSTEM);
                        author.setAbout(faker.superhero().descriptor());
                        author.setDateOfBirth(faker.date().birthday(20, 65).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                        if (faker.bool().bool()) {
                            author.setDateOfDeath(faker.date().birthday(0, 10)
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                        }
                        authorRepo.save(author);
                    });
        }

        if (genreRepo.count() == 0) {
            Stream.generate(() -> faker.book().genre())
                    .distinct()
                    .limit(10)
                    .forEach(name -> {
                        final var genres = new Genre();
                        genres.setName(name);
                        genres.setAbout(faker.weather().description());
                        genres.setCreatedBy(SYSTEM);
                        genreRepo.save(genres);
                    });
        }

        if (publisherRepo.count() == 0) {
            Stream.generate(() -> faker.book().publisher())
                    .distinct()
                    .limit(8)
                    .forEach(name -> {
                        final var publisher = new Publisher();
                        publisher.setName(name);
                        publisher.setAbout(faker.weather().description());
                        publisher.setCreatedBy(SYSTEM);
                        publisherRepo.save(publisher);
                    });
        }

        if (seriesRepo.count() == 0) {
            Stream.generate(() -> faker.company().catchPhrase())
                    .distinct()
                    .limit(10)
                    .forEach(name -> {
                        final var series = new Series();
                        series.setName(name);
                        series.setAbout(faker.howIMetYourMother().catchPhrase());
                        series.setCreatedBy(SYSTEM);
                        seriesRepo.save(series);
                    });
        }
    }

    @Transactional
    void initShelf() {
        if (shelfRepo.count() > 0) return;
        shelfRepo.save(Shelf.storage());

        if (!config.isFakeDataEnabled()) return;
        List.of("Historical", "Science", "Manga", "Travel", "Self-help / Personal").forEach(name -> {
            final var shelf = Shelf.ofName(name);
            shelf.setCreatedBy(SYSTEM);
            shelfRepo.save(shelf);
        });
    }

    @Transactional
    void initBook() {
        if (bookInfoRepo.count() > 0) return;
        final var authors = authorRepo.findAll();
        final var series = seriesRepo.findAll();
        final var publishers = publisherRepo.findAll();
        final var genres = genreRepo.findAll();
        final var shelf = shelfRepo.findAll();

        Stream.generate(() -> faker.book().title())
                .distinct()
                .limit(100)
                .forEach(name -> {
                    final var info = new BookInfo();
                    info.setTitle(name);
                    info.setAbout(faker.backToTheFuture().quote());
                    info.setPublisher(faker.options().nextElement(publishers));
                    info.setCreatedBy(SYSTEM);
                    info.setBorrowCost(faker.number().randomDouble(1, 0, 2));
                    if (faker.random().nextInt(1, 10) > 1) {
                        info.setAuthor(faker.options().nextElement(authors));
                    }
                    if (faker.bool().bool()) {
                        info.setSeries(faker.options().nextElement(series));
                    }

                    if (faker.bool().bool()) {
                        info.setYear(faker.number().numberBetween(1960, 2018));
                    }

                    // mark associated genres
                    final var numberOfGenres = faker.random().nextInt(0, 8);
                    for (int i = 0; i < numberOfGenres; i++) {
                        info.getGenres().add(faker.options().nextElement(genres));
                    }
                    bookInfoRepo.save(info);

                    // generate associated book
                    final var numberOfBooks = faker.random().nextInt(1, 5);
                    for (int i = 0; i < numberOfBooks; i++) {
                        final var book = new Book();
                        book.setInfo(info);
                        book.setCreatedBy(SYSTEM);
                        book.setShelf(faker.options().nextElement(shelf));
                        book.setNote(faker.howIMetYourMother().quote());
                        bookRepo.save(book);
                    }
                });
    }

    @Transactional
    void initBookBorrow() {
        if (bookBorrowRepo.count() > 0) return;
        final var books = bookRepo.findAll();
        final var readers = readerRepo.findAll();
        final var users = userRepo.findAll().stream()
                .filter(User::isInternal)
                .collect(Collectors.toList());

        Stream.generate(() -> faker.options().nextElement(readers))
                .distinct()
                .limit(50)
                .forEach(reader -> {
                    final var numberOfTickets = faker.number().numberBetween(1, 10);
                    for (int i = 0; i < numberOfTickets; i++) {
                        var income = incomeRepo.save(new Income());
                        final var returnDate = faker.date().past(62, 0, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        income.setReturnDate(returnDate);
                        income.setCreatedBy(SYSTEM);

                        var borrowLimit = faker.number().numberBetween(1, 8);
                        for (int j = 0; j < borrowLimit; j++) {
                            final var borrow = new BookBorrow();
                            final var borrowDate = faker.date().past(92, (int) ChronoUnit.DAYS.between(returnDate.minusDays(1), LocalDate.now()), TimeUnit.DAYS)
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            borrow.setCreatedBy(faker.options().nextElement(users).getUsername());
                            borrow.setReader(faker.options().nextElement(readers));
                            borrow.setBorrowDate(borrowDate);
                            borrow.setCreatedAt(borrowDate.atStartOfDay().toInstant(ZoneOffset.UTC));
                            borrow.setUpdatedAt(borrowDate.atStartOfDay().toInstant(ZoneOffset.UTC));
                            borrow.setDueDate(faker.date().past(60, 0, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            borrow.setReturned(true);
                            borrow.setReturnDate(returnDate);
                            final var book = faker.options().nextElement(books);
                            borrow.setBook(book);
                            borrow.setBorrowCost(book.getBorrowCost());
                            borrow.setOverDueAdditionalCost(Math.max(book.getBorrowCost(), config.getOverDueDefaultCost()));
                            if (borrow.getTotalCost() > 0d) {
                                if (borrow.getReturnDate().isAfter(income.getReturnDate())) income.setReturnDate(borrow.getReturnDate());
                                borrow.setIncome(income.add(borrow));
                            }
                            if (faker.bool().bool()) borrow.setNote(faker.howIMetYourMother().quote());
                            bookBorrowRepo.save(borrow);
                        }
                        incomeRepo.save(income);
                    }
                });


        // generate borrowing ticket
        final List<Book> borrowedBook = new ArrayList<>();
        Stream.generate(() -> faker.options().nextElement(readers))
                .distinct()
                .limit(25)
                .filter(x -> borrowedBook.size() < books.size())
                .forEach(reader -> {
                    var borrowLimit = faker.number().numberBetween(1, reader.getBorrowLimit());
                    final var numberOfTickets = faker.number().numberBetween(1, borrowLimit);

                    for (int i = 0; i < numberOfTickets; i++) {
                        final var borrowDate = faker.date().past(15, 0, TimeUnit.DAYS)
                                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        final var bound = (int) ChronoUnit.DAYS.between(borrowDate, LocalDate.now());
                        final var dueDate = bound <= 0 ? LocalDate.now() : faker.number().numberBetween(0, 10) < 2
                                ? faker.date().past(bound, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                : faker.date().future(10, 1, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        final var note = faker.howIMetYourMother().quote();
                        final var numberOfBorrow = faker.number().numberBetween(1, borrowLimit);
                        for (int j = 0; j < numberOfBorrow; j++) {
                            final var borrow = new BookBorrow();
                            borrow.setCreatedBy(faker.options().nextElement(users).getUsername());
                            borrow.setReader(reader);
                            borrow.setBorrowDate(borrowDate);
                            borrow.setCreatedAt(borrowDate.atStartOfDay().toInstant(ZoneOffset.UTC));
                            borrow.setUpdatedAt(borrowDate.atStartOfDay().toInstant(ZoneOffset.UTC));
                            borrow.setDueDate(dueDate);
                            borrow.setNote(note);

                            final var book = faker.options().nextElement(books);
                            book.setStatus(Book.Status.BORROWED);
                            books.remove(book);
                            borrowedBook.add(book);
                            borrow.setBook(book);
                            borrow.setBorrowCost(book.getBorrowCost());
                            borrow.setOverDueAdditionalCost(Math.max(book.getBorrowCost(), config.getOverDueDefaultCost()));
                            bookBorrowRepo.save(borrow);
                            borrowLimit--;
                            if (borrowLimit <= 0) break;
                        }
                        if (borrowLimit <= 0) break;
                    }
                });
        bookRepo.saveAll(borrowedBook);
    }

    @Transactional
    void initFavorites() {
        final var readers = readerRepo.findAll();
        final var books = bookInfoRepo.findAll().toArray(new BookInfo[0]);
        for (final var reader : readers) {
            final var favoritesCount = faker.number().numberBetween(0, 10);
            if (favoritesCount <= 0) continue;

            final Set<BookInfo> favorites = new HashSet<>();
            for (int i = 0; i < favoritesCount; i++) {
                favorites.add(faker.options().nextElement(books));
            }
            reader.setFavorites(favorites);
            readerRepo.save(reader);
        }
    }
}
