# Libman Project

Simple library management web app using [Spring](https://spring.io/projects/spring-boot) and [Unpoly](https://unpoly.com/) +
JSP.

This project is for learning purpose.

## Requirement

- Java 11+
- Maven (for development, optional)
- SDKMan (for java tools management, optional)

## Configuration

See all available configurations in [application.properties](src/main/resources/application.properties).

Config properties can be set directly in:

- Project's ```application.properties```
- ```application.properties``` file in the same folder with your war
- Environment variables

See [Spring external config docs](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/features.html#features.external-config)
for more information.

```properties
# overwrite spring.profiles.active
SPRING_PROFILES_ACTIVE=false
```

### Default data generation

By default, the app will generate some required data if not exists,
including: roles, permissions, default users, and one default shelf.
Default user include:

- **librarian** - account with role librarian, can access almost every thing, except account control and execute
  dangerous action.
- **admin** - account with role admin, can access every thing.
- SYSTEM - no role.

All generated users using the same password: **123456**.

Roles and permissions will be auto generated and setup for each logic.
There are some special permissions that you cannot delete:

- ```ADMIN```: special permission allow you to access everything.
- ```LIBRARIAN```: permission that allow you to access librarian app, without this permission, your account will only be
  a reader account and can not access library manage app, even if you have ```ADMIN``` permission
- ```FORCE```: permission that allow you to execute dangerous action, for example delete an author that affect related
  books

### Dev (default)

This is the default config when you run the app without any config.

The default dev profile does not require any configuration:

- All data include database and uploaded file will be stored in memory, purged after app shutdown.
- Mail will show in console instead of sending to user.
- Fake data will be generated on startup (book, reader, account, borrow ticket....)
- App running on http://localhost:8080
- H2 database console can be accessed via http://localhost:8080/h2-console (user: admin, password: 123456, JDBC URL:
  jdbc:h2:mem:test)

You can disable fake data generation by setting this property. The required default data will still be generated.

```properties
config.data.enable-fake-data=false
```

### Production

To run app in "production", you need to tweak some config properties in ```application.properties```

1. **Disable dev profile**
    ```properties
    spring.profiles.active = default
    ```
   Setting this will automatically disable fake data generation. The required data like role, permission, default users
   will still be generated.

2. **Setup mail**
    ```properties
    # Default config. Change those configs if you not using gmail
    spring.mail.host                                   = smtp.gmail.com
    spring.mail.port                                   = 587
    spring.mail.properties.mail.smtp.auth              = true
    spring.mail.properties.mail.smtp.starttls.enable   = true
    spring.mail.properties.mail.smtp.starttls.required = true

    # Credentials
    spring.mail.password                               = <somepassword>
    spring.mail.username                               = example@gmail.com
    # Set this property to `test` will disable send mail (log mail to console instead)
    config.email.from                                  = example@gmail.com
    ```
3. **Setup Postgres database**
    ```properties
    # Change those configs to match your database. only postgresql supported
    spring.datasource.url      = jdbc:postgresql://localhost:5432/library
    spring.datasource.username = postgres
    spring.datasource.password = 123456
    ```
4. **Setup File Storage**
   ```properties
   # Example config using file as storage.
   config.vfs.base-path = file:///D:/Temp
   ```
   See [Apache Common VFS docs](https://commons.apache.org/proper/commons-vfs/filesystems.html) for available config.

5. **Setup data**

   Remember to change the password of default users: admin, librarian.

## Run Project

```shell
mvn spring-boot:run
```

The command will run project on port 8080.

Note: You can use maven wrapper to run maven command instead if you don't have maven installed:

```shell
# linux
./mvnw spring-boot:run
# window
./mvnw.cmd spring-boot:run
```

## Build project

```shell
mvn package
```

The command will build project into a ```libman-<version>.war``` file inside ```./target``` directory.

You can run the packaged war using bellow command:

```shell
java -jar libman-1.0.1.war
```

Note that the version depend on your output war, and the ```-jar``` flag is required.
