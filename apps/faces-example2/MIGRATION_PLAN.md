# Migration Plan: struts-faces-example2 to Spring Boot

This document provides a comprehensive guide for migrating the `struts-faces-example2` application from Struts 1.x with JSF integration to Spring Boot. It serves as a reference throughout the migration process and can be used by developers unfamiliar with the existing codebase.

## 1. Overview

### 1.1 Current Application Description

The `struts-faces-example2` application is a demonstration web application that showcases the integration of Apache Struts 1.x with JavaServer Faces (JSF). It implements a user registration and mail subscription management system with the following features:

- User authentication (logon/logoff)
- User registration with profile management
- Mail subscription management (IMAP/POP3)
- In-memory database persistence backed by XML

The application uses a dual-servlet architecture where both the Struts `ActionServlet` and JSF `FacesServlet` work together, with Apache Tiles providing page composition and layout management.

### 1.2 Migration Goals

The primary goals of this migration are:

- Replace the legacy Struts 1.x framework with Spring Boot's modern MVC architecture
- Eliminate the dual-servlet complexity by consolidating request handling under Spring's `DispatcherServlet`
- Modernize the view layer by replacing JSF/Tiles with Thymeleaf (or optionally retaining JSF with Spring integration)
- Replace XML-based configuration with annotation-driven configuration and Spring Boot auto-configuration
- Update form validation from Struts Validator to JSR-380 Bean Validation
- Convert the WAR-based deployment to Spring Boot's executable JAR model
- Maintain all existing business functionality throughout the migration

### 1.3 High-Level Migration Approach

The recommended approach is an incremental migration strategy:

1. Set up a parallel Spring Boot project structure
2. Migrate the data layer and domain model first
3. Convert Struts Actions to Spring MVC Controllers one at a time
4. Replace JSF managed beans with Spring components
5. Migrate views from JSP/JSF to Thymeleaf templates
6. Replace Tiles layouts with Thymeleaf layout dialect
7. Update validation to use Bean Validation annotations
8. Test each component thoroughly before proceeding to the next

## 2. Current Architecture

### 2.1 Dual-Servlet Architecture

The application employs an unusual dual-servlet architecture configured in `WEB-INF/web.xml`:

**FacesServlet (JSF)**
- Servlet class: `javax.faces.webapp.FacesServlet`
- URL pattern: `*.faces`
- Load order: 1 (loads first)
- Purpose: Handles JSF lifecycle and component rendering

**ActionServlet (Struts)**
- Servlet class: `org.apache.struts.action.ActionServlet`
- URL pattern: `*.do`
- Load order: 2
- Configuration: `/WEB-INF/struts-config.xml`
- Purpose: Routes requests to Struts Actions

The MyFaces implementation is initialized via `StartupServletContextListener` which bootstraps the JSF runtime before either servlet begins processing requests.

### 2.2 Framework Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| Front Controller | Struts ActionServlet + JSF FacesServlet | Request routing and lifecycle management |
| Request Processor | FacesTilesRequestProcessor | Custom processor integrating Struts, JSF, and Tiles |
| View Technology | JSP with JSF components | Page rendering |
| Layout Management | Apache Tiles 1.x | Page composition and templates |
| Form Handling | Struts ActionForm / DynaValidatorForm | Request parameter binding |
| Validation | Struts Validator (Commons Validator) | Declarative form validation |
| Managed Beans | JSF Managed Beans | View backing beans |
| Persistence | MemoryDatabasePlugIn | In-memory XML-backed storage |

### 2.3 Key Configuration Files

**`WEB-INF/web.xml`**

Defines the servlet container configuration including:
- MyFaces startup listener
- FacesServlet and ActionServlet declarations
- URL pattern mappings (`.faces` and `.do`)
- Tag library descriptors (struts-html, struts-bean, struts-logic, struts-tiles)
- Welcome file configuration

**`WEB-INF/struts-config.xml`**

The central Struts configuration containing:
- Form bean definitions (logonForm, registrationForm, subscriptionForm)
- Global forwards mapping logical names to Tiles definitions
- Action mappings connecting URL paths to Action classes
- Controller configuration specifying `FacesTilesRequestProcessor`
- Message resources reference
- Plugin declarations (TilesPlugin, MemoryDatabasePlugIn, ValidatorPlugIn)

**`WEB-INF/faces-config.xml`**

JSF configuration defining managed beans:
- `loggedOff` (LoggedOff.class) - Backing bean for logged-off menu
- `loggedOn` (LoggedOn.class) - Backing bean for logged-on menu
- `registrationBacking` (RegistrationBacking.class) - Backing bean for registration page

**`WEB-INF/tiles-defs.xml`**

Tiles layout definitions:
- `.base` - Base layout template with header, footer, menu, and body regions
- `.loggedoff` - Layout for unauthenticated users
- `.loggedon` - Layout for authenticated users
- Page definitions extending these layouts (`.logon`, `.mainMenu`, `.register`, `.registration`, `.subscription`, `.welcome`)

**`WEB-INF/validation.xml`**

Struts Validator rules for:
- `logonForm` - Username and password length constraints
- `registrationForm` - Required fields and email format validation

### 2.4 Current Dependencies

From `pom.xml`, the application depends on:

| Dependency | Purpose |
|------------|---------|
| struts-faces | Struts-JSF integration library |
| struts-tiles | Apache Tiles 1.x support |
| myfaces-impl | MyFaces JSF implementation |
| myfaces-jsf-api | JSF API |
| myfaces-extensions | MyFaces extensions |
| jstl | JavaServer Pages Standard Tag Library |
| servlet-api | Servlet API |
| taglibs:standard | JSTL reference implementation |
| commons-codec | Required by MyFaces |
| commons-el | Expression Language support |

### 2.5 Special Components

**FacesTilesRequestProcessor**

Configured in `struts-config.xml` as the custom request processor:
```xml
<controller>
  <set-property property="inputForward" value="true"/>
  <set-property property="processorClass"
          value="org.apache.struts.faces.application.FacesTilesRequestProcessor"/>
</controller>
```

This processor bridges Struts request handling with JSF lifecycle and Tiles rendering. It intercepts forwards to check if they reference Tiles definitions and renders them appropriately within the JSF context.

**MemoryDatabasePlugIn**

A Struts PlugIn that initializes an in-memory user database from `/WEB-INF/database.xml`:
- Implements the `PlugIn` interface with `init()` and `destroy()` lifecycle methods
- Creates a `MemoryUserDatabase` instance and stores it in the servlet context
- Handles both file-based and WAR-based deployments
- Caches server type options for subscription forms

**JSF Backing Beans**

Three JSF managed beans provide view logic:

- `LoggedOff` - Handles navigation for unauthenticated users (register, logon actions)
- `LoggedOn` - Handles navigation for authenticated users (edit registration, logoff actions)
- `RegistrationBacking` - Manages subscription CRUD operations from the registration page

These beans use `FacesContext.getExternalContext().dispatch()` to forward to Struts actions, demonstrating the tight coupling between JSF and Struts in this application.

### 2.6 Action Classes

| Action Class | Path | Purpose |
|--------------|------|---------|
| LogonAction | /logon | Validates credentials and establishes user session |
| LogoffAction | /logoff | Invalidates session and logs user out |
| EditRegistrationAction | /editRegistration | Prepares registration form for create/edit |
| SaveRegistrationAction | /saveRegistration | Persists user registration changes |
| EditSubscriptionAction | /editSubscription | Prepares subscription form for create/edit/delete |
| SaveSubscriptionAction | /saveSubscription | Persists subscription changes |

### 2.7 Form Beans

| Form Bean | Type | Fields |
|-----------|------|--------|
| logonForm | DynaValidatorForm | username, password |
| registrationForm | RegistrationForm (ValidatorForm) | action, fromAddress, fullName, password, password2, replyToAddress, username |
| subscriptionForm | SubscriptionForm (ActionForm) | action, autoConnect, host, password, type, username |

## 3. Migration Strategy

### 3.1 Replace Struts Actions with Spring MVC Controllers

Each Struts Action class should be converted to a Spring `@Controller` class. The `execute()` method pattern maps naturally to `@RequestMapping` handler methods.

**Example: LogonAction to LogonController**

Current Struts Action:
```java
public final class LogonAction extends Action {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        // Extract credentials from form
        String username = (String) PropertyUtils.getSimpleProperty(form, "username");
        String password = (String) PropertyUtils.getSimpleProperty(form, "password");
        
        // Validate and authenticate
        User user = database.findUser(username);
        if (user == null || !user.getPassword().equals(password)) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.password.mismatch"));
            saveErrors(request, errors);
            return mapping.getInputForward();
        }
        
        // Store user in session
        request.getSession().setAttribute(Constants.USER_KEY, user);
        return mapping.findForward("success");
    }
}
```

Target Spring Controller:
```java
@Controller
public class LogonController {
    
    @Autowired
    private UserDatabase userDatabase;
    
    @GetMapping("/logon")
    public String showLogonForm(Model model) {
        model.addAttribute("logonForm", new LogonForm());
        return "logon";
    }
    
    @PostMapping("/logon")
    public String processLogon(@Valid @ModelAttribute LogonForm form,
                               BindingResult result,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "logon";
        }
        
        User user = userDatabase.findUser(form.getUsername());
        if (user == null || !user.getPassword().equals(form.getPassword())) {
            result.rejectValue(null, "error.password.mismatch");
            return "logon";
        }
        
        session.setAttribute("user", user);
        return "redirect:/mainMenu";
    }
}
```

**Key Conversion Patterns:**

| Struts Pattern | Spring MVC Equivalent |
|----------------|----------------------|
| `Action.execute()` | `@RequestMapping` handler method |
| `ActionMapping.findForward("name")` | Return view name or `redirect:` prefix |
| `ActionMapping.getInputForward()` | Return form view name |
| `saveErrors(request, errors)` | `BindingResult` with `rejectValue()` |
| `ActionForm` parameter binding | `@ModelAttribute` with POJO |
| `servlet.getServletContext().getAttribute()` | `@Autowired` dependency injection |

**Migration Order:**

1. LogoffAction (simplest - no form, just session invalidation)
2. LogonAction (form handling with validation)
3. EditRegistrationAction (form preparation)
4. SaveRegistrationAction (complex validation and persistence)
5. EditSubscriptionAction (form preparation with parameters)
6. SaveSubscriptionAction (CRUD operations)

The `FacesTilesRequestProcessor` will be completely eliminated as Spring's `DispatcherServlet` handles all request processing natively.

### 3.2 Migrate JSF Integration

Two options exist for handling the JSF components:

**Option A: Keep JSF with Spring Boot Integration**

Use the JoinFaces project to integrate JSF with Spring Boot:

```xml
<dependency>
    <groupId>org.joinfaces</groupId>
    <artifactId>joinfaces-spring-boot-starter</artifactId>
    <version>5.x.x</version>
</dependency>
```

Pros:
- Preserves existing JSF components and pages
- Minimal changes to view layer
- JSF managed beans can be converted to Spring `@Component` beans

Cons:
- Maintains complexity of dual-framework architecture
- JSF is considered legacy technology
- Larger dependency footprint
- Continued maintenance burden

**Option B: Replace JSF with Thymeleaf (Recommended)**

Convert JSF pages to Thymeleaf templates:

Pros:
- Modern, actively maintained template engine
- Native Spring Boot integration
- Simpler architecture with single view technology
- Better performance and smaller footprint
- Natural HTML templates that work in browsers

Cons:
- Requires rewriting all view templates
- Loss of JSF component model
- Learning curve for Thymeleaf syntax

**Recommendation:** Option B is recommended for new development. The JSF integration in this application is relatively simple, with backing beans primarily performing navigation. Converting to Thymeleaf will result in a cleaner, more maintainable architecture.

**JSF Backing Bean Migration:**

The JSF backing beans (`LoggedOff`, `LoggedOn`, `RegistrationBacking`) primarily handle navigation by dispatching to Struts actions. In Spring MVC, this navigation logic moves into the controllers themselves or becomes simple link generation in templates.

Current JSF backing bean:
```java
public class LoggedOff {
    public String logon() {
        FacesContext context = FacesContext.getCurrentInstance();
        forward(context, "/editLogon.do");
        return null;
    }
}
```

In Thymeleaf, this becomes a simple link:
```html
<a th:href="@{/logon}">Log On</a>
```

### 3.3 Replace Tiles with Thymeleaf Layouts

Apache Tiles provides page composition through XML-defined layouts. Thymeleaf's Layout Dialect offers equivalent functionality with a more intuitive approach.

**Add Layout Dialect Dependency:**
```xml
<dependency>
    <groupId>nz.net.ultraq.thymeleaf</groupId>
    <artifactId>thymeleaf-layout-dialect</artifactId>
</dependency>
```

**Current Tiles Configuration:**

Base layout (`tiles-defs.xml`):
```xml
<definition name=".base" page="/layout.faces">
  <put name="header" value="/header.jsp"/>
  <put name="footer" value="/footer.jsp"/>
  <put name="menu"   value="/blank.jsp"/>
  <put name="body"   value="/blank.jsp"/>
</definition>

<definition name=".logon" extends=".loggedoff">
  <put name="body" value="/logon.jsp"/>
</definition>
```

**Equivalent Thymeleaf Layout:**

Base layout (`templates/layout/base.html`):
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <title layout:title-pattern="$CONTENT_TITLE - $LAYOUT_TITLE">Application</title>
    <link th:href="@{/css/stylesheet.css}" rel="stylesheet"/>
</head>
<body>
    <div th:replace="~{fragments/header :: header}"></div>
    <div layout:fragment="menu"></div>
    <div layout:fragment="content"></div>
    <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```

Page using layout (`templates/logon.html`):
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title>Log On</title>
</head>
<body>
    <div layout:fragment="menu">
        <div th:replace="~{fragments/loggedoff-menu :: menu}"></div>
    </div>
    <div layout:fragment="content">
        <!-- Logon form content -->
    </div>
</body>
</html>
```

**Layout Mapping:**

| Tiles Definition | Thymeleaf Equivalent |
|------------------|---------------------|
| `.base` | `templates/layout/base.html` |
| `.loggedoff` | `templates/layout/loggedoff.html` (extends base) |
| `.loggedon` | `templates/layout/loggedon.html` (extends base) |
| `<put name="X">` | `layout:fragment="X"` |
| `extends=".parent"` | `layout:decorate="~{layout/parent}"` |

### 3.4 Convert Form Beans to Spring Form Objects

Struts form beans extend `ActionForm` or `ValidatorForm` and include framework-specific methods. Spring uses plain POJOs with Bean Validation annotations.

**Current RegistrationForm:**
```java
public final class RegistrationForm extends ValidatorForm {
    private String action = "Create";
    private String fromAddress = null;
    private String fullName = null;
    private String password = null;
    private String password2 = null;
    private String replyToAddress = null;
    private String username = null;
    
    // Getters and setters...
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.action = "Create";
        // Reset all fields...
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        // Cross-field validation for password match
        if (!password.equals(password2)) {
            errors.add("password2", new ActionMessage("error.password.match"));
        }
        return errors;
    }
}
```

**Target Spring Form Object:**
```java
public class RegistrationForm {
    
    private String action = "Create";
    
    @NotBlank(message = "{prompt.fromAddress}")
    @Email(message = "{error.email.invalid}")
    private String fromAddress;
    
    @NotBlank(message = "{prompt.fullName}")
    private String fullName;
    
    private String password;
    
    private String password2;
    
    @Email(message = "{error.email.invalid}")
    private String replyToAddress;
    
    @NotBlank(message = "{prompt.username}")
    private String username;
    
    // Getters and setters only - no framework methods
}
```

**Cross-Field Validation:**

For password matching, create a custom validator:

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
public @interface PasswordMatch {
    String message() default "{error.password.match}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegistrationForm> {
    @Override
    public boolean isValid(RegistrationForm form, ConstraintValidatorContext context) {
        if (form.getPassword() == null || form.getPassword2() == null) {
            return true;
        }
        return form.getPassword().equals(form.getPassword2());
    }
}
```

Apply to the form class:
```java
@PasswordMatch
public class RegistrationForm {
    // ...
}
```

**Form Bean Mapping:**

| Struts Form | Spring Form | Notes |
|-------------|-------------|-------|
| logonForm (DynaValidatorForm) | LogonForm (POJO) | Convert dynamic properties to typed fields |
| registrationForm (ValidatorForm) | RegistrationForm (POJO) | Add @PasswordMatch for cross-field validation |
| subscriptionForm (ActionForm) | SubscriptionForm (POJO) | Move validate() logic to annotations |

### 3.5 Replace Struts Validator with Bean Validation

The Struts Validator uses XML configuration (`validation.xml`) with Commons Validator. Spring Boot uses JSR-380 Bean Validation (Hibernate Validator).

**Add Validation Starter:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Current Struts Validation (`validation.xml`):**
```xml
<form name="logonForm">
    <field property="username" depends="minlength,maxlength">
        <arg0 key="prompt.username"/>
        <arg1 key="${var:minlength}" name="minlength" resource="false"/>
        <arg2 key="${var:maxlength}" name="maxlength" resource="false"/>
        <var>
            <var-name>maxlength</var-name>
            <var-value>16</var-value>
        </var>
        <var>
            <var-name>minlength</var-name>
            <var-value>3</var-value>
        </var>
    </field>
</form>
```

**Equivalent Bean Validation:**
```java
public class LogonForm {
    
    @NotBlank(message = "{prompt.username}")
    @Size(min = 3, max = 16, message = "{errors.range}")
    private String username;
    
    @NotBlank(message = "{prompt.password}")
    @Size(min = 3, max = 16, message = "{errors.range}")
    private String password;
    
    // Getters and setters
}
```

**Validation Rule Mapping:**

| Struts Validator | Bean Validation | Notes |
|------------------|-----------------|-------|
| required | @NotBlank / @NotNull | Use @NotBlank for strings |
| minlength | @Size(min=X) | Combined with maxlength |
| maxlength | @Size(max=X) | Combined with minlength |
| email | @Email | Built-in annotation |
| mask (regex) | @Pattern | Regular expression validation |

**Controller Integration:**
```java
@PostMapping("/logon")
public String processLogon(@Valid @ModelAttribute LogonForm form,
                           BindingResult result) {
    if (result.hasErrors()) {
        return "logon";
    }
    // Process valid form
}
```

### 3.6 Migrate Configuration

#### 3.6.1 Replace web.xml with Spring Boot Configuration

Spring Boot's embedded servlet container eliminates the need for `web.xml`. Configuration moves to `application.properties` or `application.yml` and Java configuration classes.

**Current web.xml elements and their Spring Boot equivalents:**

| web.xml Element | Spring Boot Equivalent |
|-----------------|----------------------|
| `<servlet>` declarations | Auto-configured DispatcherServlet |
| `<servlet-mapping>` | `server.servlet.context-path` in properties |
| `<listener>` | `@Component` implementing appropriate interface |
| `<welcome-file-list>` | Controller mapping for "/" |
| `<taglib>` | Not needed - Thymeleaf auto-configured |

**application.properties:**
```properties
# Server configuration
server.port=8080
server.servlet.context-path=/struts-faces-example2

# Thymeleaf configuration
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Message source (replaces ApplicationResources.properties)
spring.messages.basename=messages
spring.messages.encoding=UTF-8
```

#### 3.6.2 Convert struts-config.xml Action Mappings

Action mappings in `struts-config.xml` become `@RequestMapping` annotations on controller methods.

**Current struts-config.xml:**
```xml
<action path="/logon"
        type="org.apache.struts.webapp.example2.LogonAction"
        name="logonForm"
        scope="request"
        input="logon">
  <exception key="expired.password"
             type="org.apache.struts.webapp.example2.ExpiredPasswordException"
             path="/changePassword.faces"/>
</action>
```

**Spring MVC Equivalent:**
```java
@Controller
public class LogonController {
    
    @GetMapping("/logon")
    public String showForm(Model model) {
        model.addAttribute("logonForm", new LogonForm());
        return "logon";
    }
    
    @PostMapping("/logon")
    public String processLogon(@Valid @ModelAttribute LogonForm form,
                               BindingResult result,
                               HttpSession session) {
        if (result.hasErrors()) {
            return "logon";  // input forward equivalent
        }
        // ... authentication logic
        return "redirect:/mainMenu";  // success forward equivalent
    }
    
    @ExceptionHandler(ExpiredPasswordException.class)
    public String handleExpiredPassword() {
        return "changePassword";
    }
}
```

**Global Forwards:**

Global forwards become either:
- View resolver mappings (for view names)
- Redirect URLs in controllers
- Constants in a shared class

```java
public class ViewNames {
    public static final String WELCOME = "welcome";
    public static final String LOGON = "logon";
    public static final String MAIN_MENU = "mainMenu";
    public static final String REGISTRATION = "registration";
    public static final String SUBSCRIPTION = "subscription";
}
```

#### 3.6.3 Migrate JSF Managed Beans to Spring Components

JSF managed beans defined in `faces-config.xml` become Spring `@Component` or `@Controller` beans.

**Current faces-config.xml:**
```xml
<managed-bean>
  <managed-bean-name>loggedOff</managed-bean-name>
  <managed-bean-class>org.apache.struts.webapp.example2.LoggedOff</managed-bean-class>
  <managed-bean-scope>request</managed-bean-scope>
</managed-bean>
```

Since these beans primarily handle navigation, their functionality is absorbed into controllers or eliminated entirely when using Thymeleaf links.

#### 3.6.4 Replace ApplicationResources.properties

Struts message resources become Spring's `MessageSource`:

**Current location:** `org/apache/struts/webapp/example2/ApplicationResources.properties`

**New location:** `src/main/resources/messages.properties`

The property keys remain the same, but the lookup mechanism changes:

In Thymeleaf templates:
```html
<label th:text="#{prompt.username}">Username</label>
<span th:if="${#fields.hasErrors('username')}" 
      th:errors="*{username}">Error</span>
```

In Java code:
```java
@Autowired
private MessageSource messageSource;

String message = messageSource.getMessage("error.password.mismatch", null, locale);
```

### 3.7 Update Build Configuration

#### 3.7.1 Replace WAR Packaging with Spring Boot JAR

**Current pom.xml:**
```xml
<packaging>war</packaging>

<dependencies>
    <dependency>
        <groupId>org.apache.struts</groupId>
        <artifactId>struts-faces</artifactId>
    </dependency>
    <!-- ... other Struts/JSF dependencies -->
</dependencies>
```

**Target pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.apache.struts.webapp</groupId>
    <artifactId>example2-spring-boot</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Thymeleaf Layout Dialect -->
        <dependency>
            <groupId>nz.net.ultraq.thymeleaf</groupId>
            <artifactId>thymeleaf-layout-dialect</artifactId>
        </dependency>
        
        <!-- Development Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 3.7.2 Dependency Mapping

| Current Dependency | Spring Boot Replacement |
|--------------------|------------------------|
| struts-faces | Removed (Spring MVC) |
| struts-tiles | thymeleaf-layout-dialect |
| myfaces-impl | Removed (Thymeleaf) |
| myfaces-jsf-api | Removed (Thymeleaf) |
| myfaces-extensions | Removed |
| jstl | Removed (Thymeleaf) |
| servlet-api | Provided by spring-boot-starter-web |
| taglibs:standard | Removed (Thymeleaf) |
| commons-codec | Transitive if needed |
| commons-el | Removed |

### 3.8 Database Plugin Migration

The `MemoryDatabasePlugIn` initializes the in-memory database during servlet startup. In Spring Boot, this becomes a `@Configuration` class with `@Bean` methods.

**Current MemoryDatabasePlugIn:**
```java
public final class MemoryDatabasePlugIn implements PlugIn {
    private String pathname = "/WEB-INF/database.xml";
    
    public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
        database = new MemoryUserDatabase();
        database.setPathname(calculatePath());
        database.open();
        servlet.getServletContext().setAttribute(Constants.DATABASE_KEY, database);
        setupCache(servlet, config);
    }
    
    public void destroy() {
        database.close();
        servlet.getServletContext().removeAttribute(Constants.DATABASE_KEY);
    }
}
```

**Spring Boot Configuration:**
```java
@Configuration
public class DatabaseConfiguration {
    
    @Value("${app.database.path:classpath:database.xml}")
    private Resource databasePath;
    
    @Bean
    public UserDatabase userDatabase() throws Exception {
        MemoryUserDatabase database = new MemoryUserDatabase();
        database.setPathname(databasePath.getFile().getAbsolutePath());
        database.open();
        return database;
    }
    
    @Bean
    public List<LabelValueBean> serverTypes() {
        return List.of(
            new LabelValueBean("IMAP Protocol", "imap"),
            new LabelValueBean("POP3 Protocol", "pop3")
        );
    }
    
    @PreDestroy
    public void cleanup() {
        // Database cleanup handled by Spring's lifecycle
    }
}
```

The `UserDatabase` interface and `MemoryUserDatabase` implementation can be retained with minimal changes, as they are not Struts-specific.

## 4. Implementation Approach

### 4.1 Recommended Migration Strategy

An incremental migration approach is recommended to minimize risk and allow for continuous testing:

**Phase 1: Project Setup (1-2 days)**
- Create new Spring Boot project structure alongside existing code
- Set up build configuration with Spring Boot parent POM
- Configure Thymeleaf and validation dependencies
- Create application.properties with basic settings
- Implement Spring Boot main application class
- **Testing**: Verify application context loads successfully with `@SpringBootTest`

**Phase 2: Data Layer Migration (1-2 days)**
- Copy domain classes (User, Subscription, UserDatabase interface)
- Migrate MemoryUserDatabase and MemoryDatabasePlugIn to Spring configuration
- Create Spring beans for database access
- **Testing**: 
  - Unit tests for all domain class methods
  - Integration tests for database initialization and CRUD operations
  - Verify data persistence across application restarts
  - **Acceptance Criteria**: All existing user/subscription operations work identically to original

**Phase 3: Controller Migration (3-5 days)**
- Start with LogoffAction (simplest)
- Progress to LogonAction (form handling)
- Migrate registration actions
- Migrate subscription actions
- Create form objects with Bean Validation
- **Testing**:
  - Unit tests for each controller method using MockMvc
  - Test valid and invalid form submissions
  - Test authentication and session management
  - Test error handling and validation messages
  - **Acceptance Criteria**: Each controller passes all unit tests before proceeding to next

**Phase 4: View Migration (3-5 days)**
- Create Thymeleaf base layout
- Convert header, footer, and menu fragments
- Migrate each page template
- Implement error display and validation messages
- **Testing**:
  - Visual comparison testing against original application
  - Test all form submissions render correctly
  - Test error message display
  - Test layout consistency across all pages
  - **Acceptance Criteria**: Visual parity with original application

**Phase 5: Integration and Testing (2-3 days)**
- End-to-end testing of all features
- Performance comparison
- Security review
- Documentation updates
- **Testing**:
  - Complete user journey tests (registration, login, subscription management, logout)
  - Cross-browser testing
  - Load testing comparison with original application
  - **Acceptance Criteria**: All user flows complete successfully with equivalent behavior

### 4.2 Proof of Concept Recommendation

Before full migration, implement a proof of concept with:

1. **LogoffAction → LogoffController**: Simplest action with no form handling
2. **LogonAction → LogonController**: Demonstrates form binding and validation
3. **Welcome and Logon pages**: Shows Thymeleaf layout integration

This POC validates the migration approach and identifies any unexpected issues before committing to full migration.

### 4.3 Parallel Running Strategy

During migration, both applications can run simultaneously:
- Original application on port 8080
- Spring Boot application on port 8081
- Compare behavior and output for each migrated feature

## 5. Testing Strategy

A robust testing strategy is essential for ensuring the migrated application maintains functional parity with the original. This section outlines the testing approach for each phase of the migration.

### 5.1 Testing Framework and Tools

The following testing tools should be used throughout the migration:

| Tool | Purpose |
|------|---------|
| JUnit 5 | Unit testing framework |
| Spring Boot Test | Integration testing with Spring context |
| MockMvc | Controller testing without full server |
| Mockito | Mocking dependencies |
| AssertJ | Fluent assertions |
| Selenium/Playwright | End-to-end browser testing |
| JMeter/Gatling | Performance and load testing |
| Testcontainers | Integration testing with containers (if needed) |

**Test Dependencies (pom.xml):**
```xml
<dependencies>
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 5.2 Unit Testing Requirements

Unit tests should be written for every migrated component. Each test class should follow the naming convention `*Test.java` and be placed in the corresponding test package.

**Controller Unit Tests:**

Each controller method requires tests for:
- Happy path (valid input, successful operation)
- Validation failures (invalid form data)
- Business logic errors (e.g., invalid credentials)
- Edge cases (null values, empty strings, boundary conditions)

Example test structure for LogonController:
```java
@WebMvcTest(LogonController.class)
class LogonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDatabase userDatabase;

    @Test
    void showLogonForm_ShouldReturnLogonView() throws Exception {
        mockMvc.perform(get("/editLogon"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeExists("logonForm"));
    }

    @Test
    void processLogon_WithValidCredentials_ShouldRedirectToMainMenu() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        when(userDatabase.findUser("testuser")).thenReturn(user);

        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"));
    }

    @Test
    void processLogon_WithInvalidCredentials_ShouldReturnLogonWithError() throws Exception {
        when(userDatabase.findUser("testuser")).thenReturn(null);

        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "wrongpassword"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().hasErrors());
    }

    @Test
    void processLogon_WithBlankUsername_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "")
                .param("password", "password"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }
}
```

**Form Validation Tests:**

Test all Bean Validation constraints:
```java
class RegistrationFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("testuser");
        form.setFullName("Test User");
        form.setFromAddress("test@example.com");
        form.setPassword("password");
        form.setPassword2("password");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("");
        // ... set other required fields

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void invalidEmail_ShouldHaveViolation() {
        RegistrationForm form = new RegistrationForm();
        form.setFromAddress("not-an-email");
        // ... set other required fields

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void passwordMismatch_ShouldHaveViolation() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("password1");
        form.setPassword2("password2");
        // ... set other required fields

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isNotEmpty();
    }
}
```

**Data Layer Tests:**

Test database operations:
```java
@SpringBootTest
class MemoryUserDatabaseTest {

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void findUser_WithExistingUser_ShouldReturnUser() {
        User user = userDatabase.findUser("user");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
    }

    @Test
    void findUser_WithNonExistingUser_ShouldReturnNull() {
        User user = userDatabase.findUser("nonexistent");
        assertThat(user).isNull();
    }

    @Test
    void createUser_ShouldPersistUser() {
        User user = userDatabase.createUser("newuser");
        assertThat(user).isNotNull();
        assertThat(userDatabase.findUser("newuser")).isNotNull();
    }
}
```

### 5.3 Integration Testing Requirements

Integration tests verify that components work together correctly within the Spring context.

**Application Context Test:**
```java
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void allControllersAreLoaded() {
        assertThat(context.getBean(LogonController.class)).isNotNull();
        assertThat(context.getBean(RegistrationController.class)).isNotNull();
        assertThat(context.getBean(SubscriptionController.class)).isNotNull();
    }

    @Test
    void databaseIsInitialized() {
        UserDatabase database = context.getBean(UserDatabase.class);
        assertThat(database).isNotNull();
        // Verify initial data is loaded
        assertThat(database.findUser("user")).isNotNull();
    }
}
```

**Full Request Integration Tests:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogonIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void logonFlow_ShouldWorkEndToEnd() {
        // Get logon page
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/editLogon", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Username");

        // Submit valid credentials
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "user");
        params.add("password", "pass");
        
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/logon", params, String.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }
}
```

### 5.4 End-to-End Testing Requirements

End-to-end tests verify complete user journeys through the application using browser automation.

**Test Scenarios:**

| Scenario | Steps | Expected Result |
|----------|-------|-----------------|
| User Registration | Navigate to registration, fill form, submit | User created and logged in |
| User Login | Navigate to login, enter credentials, submit | Redirected to main menu |
| User Logout | Click logout from main menu | Session ended, redirected to welcome |
| Add Subscription | Login, navigate to registration, add subscription | Subscription saved |
| Edit Subscription | Login, navigate to subscription, modify, save | Changes persisted |
| Delete Subscription | Login, navigate to subscription, delete | Subscription removed |
| Invalid Login | Enter wrong credentials | Error message displayed |
| Validation Errors | Submit form with invalid data | Field errors displayed |

**Selenium Test Example:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserJourneyE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void completeUserJourney_RegisterLoginManageSubscriptionsLogout() {
        String baseUrl = "http://localhost:" + port;

        // 1. Navigate to welcome page
        driver.get(baseUrl + "/welcome");
        assertThat(driver.getTitle()).contains("Welcome");

        // 2. Register new user
        driver.findElement(By.linkText("Register")).click();
        driver.findElement(By.name("username")).sendKeys("newuser");
        driver.findElement(By.name("password")).sendKeys("password");
        driver.findElement(By.name("password2")).sendKeys("password");
        driver.findElement(By.name("fullName")).sendKeys("New User");
        driver.findElement(By.name("fromAddress")).sendKeys("new@example.com");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 3. Verify logged in
        assertThat(driver.getCurrentUrl()).contains("mainMenu");

        // 4. Add subscription
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("host")).sendKeys("mail.example.com");
        driver.findElement(By.name("username")).sendKeys("mailuser");
        driver.findElement(By.name("password")).sendKeys("mailpass");
        driver.findElement(By.name("type")).sendKeys("imap");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 5. Verify subscription added
        assertThat(driver.getPageSource()).contains("mail.example.com");

        // 6. Logout
        driver.findElement(By.linkText("Log off")).click();
        assertThat(driver.getCurrentUrl()).contains("welcome");
    }
}
```

### 5.5 Regression Testing

Regression testing ensures the migrated application behaves identically to the original.

**Parallel Comparison Testing:**

Run both applications simultaneously and compare responses:

1. **Response Comparison**: For each endpoint, compare HTTP status codes, headers, and response structure
2. **Form Behavior**: Submit identical form data to both applications and compare results
3. **Session Handling**: Verify session creation, maintenance, and destruction behave identically
4. **Error Messages**: Compare validation error messages for identical invalid inputs
5. **Navigation Flows**: Verify all navigation paths produce equivalent results

**Regression Test Checklist:**

| Feature | Original Behavior | Migrated Behavior | Status |
|---------|-------------------|-------------------|--------|
| Login with valid credentials | Redirect to main menu | | |
| Login with invalid credentials | Show error message | | |
| Login with blank username | Show validation error | | |
| Login with short password | Show length error | | |
| Registration with new user | Create user, auto-login | | |
| Registration with existing username | Show uniqueness error | | |
| Registration with mismatched passwords | Show match error | | |
| Edit user profile | Update and save | | |
| Add subscription | Create subscription | | |
| Edit subscription | Update subscription | | |
| Delete subscription | Remove subscription | | |
| Logout | Invalidate session | | |
| Session timeout | Redirect to login | | |

### 5.6 Performance Testing

Compare performance metrics between original and migrated applications.

**Metrics to Measure:**

- Response time for each endpoint
- Throughput (requests per second)
- Memory usage under load
- CPU utilization
- Startup time

**JMeter Test Plan Structure:**

1. **Thread Group**: Simulate concurrent users (10, 50, 100)
2. **HTTP Requests**: Cover all major endpoints
3. **Assertions**: Verify response codes and content
4. **Listeners**: Collect response times and throughput

**Acceptance Criteria:**

- Response times should not exceed 2x the original application
- No memory leaks under sustained load
- Application should handle at least equivalent concurrent users

### 5.7 Test Coverage Requirements

Maintain minimum test coverage thresholds:

| Component | Minimum Coverage |
|-----------|------------------|
| Controllers | 90% |
| Form Objects | 100% |
| Services/Repositories | 85% |
| Configuration Classes | 80% |
| Overall | 85% |

**Coverage Tools:**

- JaCoCo for code coverage measurement
- SonarQube for quality gates (optional)

**Maven Configuration:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.85</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 5.8 Acceptance Criteria Summary

Before each phase can be considered complete, the following acceptance criteria must be met:

**Phase 1 (Project Setup):**
- Application context loads without errors
- All Spring Boot auto-configuration is working
- Application starts and responds to health check

**Phase 2 (Data Layer):**
- All domain class unit tests pass
- Database initialization works correctly
- CRUD operations function identically to original
- Data persists correctly

**Phase 3 (Controllers):**
- All controller unit tests pass (90%+ coverage)
- Form validation works correctly
- Error handling matches original behavior
- Session management works correctly

**Phase 4 (Views):**
- All pages render correctly
- Forms submit and display errors properly
- Layout is consistent across all pages
- Visual appearance matches original (within reason)

**Phase 5 (Integration):**
- All end-to-end tests pass
- Performance is acceptable (within 2x of original)
- No regressions in functionality
- All user journeys complete successfully

## 6. File Mapping

### 5.1 Configuration Files

| Current File | Target Location | Notes |
|--------------|-----------------|-------|
| `WEB-INF/web.xml` | Eliminated | Spring Boot auto-configuration |
| `WEB-INF/struts-config.xml` | Controller annotations | Action mappings become @RequestMapping |
| `WEB-INF/faces-config.xml` | Eliminated | Managed beans become @Component |
| `WEB-INF/tiles-defs.xml` | `templates/layout/*.html` | Thymeleaf layout templates |
| `WEB-INF/validation.xml` | Form class annotations | Bean Validation annotations |
| `WEB-INF/database.xml` | `resources/database.xml` | Moved to classpath |
| `ApplicationResources.properties` | `resources/messages.properties` | Spring MessageSource |

### 5.2 Java Classes

| Current Class | Target Class | Notes |
|---------------|--------------|-------|
| LogonAction | LogonController | @Controller with @RequestMapping |
| LogoffAction | LogoffController | @Controller with @RequestMapping |
| EditRegistrationAction | RegistrationController | Combined with SaveRegistrationAction |
| SaveRegistrationAction | RegistrationController | Combined with EditRegistrationAction |
| EditSubscriptionAction | SubscriptionController | Combined with SaveSubscriptionAction |
| SaveSubscriptionAction | SubscriptionController | Combined with EditSubscriptionAction |
| RegistrationForm | RegistrationForm | POJO with Bean Validation |
| SubscriptionForm | SubscriptionForm | POJO with Bean Validation |
| LogonForm (dynamic) | LogonForm | New POJO class |
| LoggedOff | Eliminated | Navigation in templates |
| LoggedOn | Eliminated | Navigation in templates |
| RegistrationBacking | Eliminated | Logic moves to controller |
| MemoryDatabasePlugIn | DatabaseConfiguration | @Configuration class |
| Constants | Constants | Unchanged |
| User | User | Unchanged |
| Subscription | Subscription | Unchanged |
| UserDatabase | UserDatabase | Unchanged |
| MemoryUserDatabase | MemoryUserDatabase | Minor updates |

### 5.3 View Files

| Current File | Target File | Notes |
|--------------|-------------|-------|
| `layout.jsp` | `templates/layout/base.html` | Thymeleaf layout |
| `header.jsp` | `templates/fragments/header.html` | Thymeleaf fragment |
| `footer.jsp` | `templates/fragments/footer.html` | Thymeleaf fragment |
| `loggedoff.jsp` | `templates/fragments/loggedoff-menu.html` | Thymeleaf fragment |
| `loggedon.jsp` | `templates/fragments/loggedon-menu.html` | Thymeleaf fragment |
| `logon.jsp` | `templates/logon.html` | Thymeleaf template |
| `mainMenu.jsp` | `templates/mainMenu.html` | Thymeleaf template |
| `registration.jsp` | `templates/registration.html` | Thymeleaf template |
| `subscription.jsp` | `templates/subscription.html` | Thymeleaf template |
| `welcome.jsp` | `templates/welcome.html` | Thymeleaf template |
| `changePassword.jsp` | `templates/changePassword.html` | Thymeleaf template |
| `index.jsp` | Controller redirect | Redirect to welcome |
| `blank.jsp` | Eliminated | Not needed |

### 5.4 Static Resources

| Current Location | Target Location |
|------------------|-----------------|
| `stylesheet.css` | `static/css/stylesheet.css` |
| `struts-power.gif` | `static/images/struts-power.gif` |

### 5.5 Project Structure Comparison

**Current Structure:**
```
apps/faces-example2/
├── pom.xml
└── src/main/
    ├── java/org/apache/struts/webapp/example2/
    │   ├── *Action.java
    │   ├── *Form.java
    │   ├── *.java (backing beans, domain)
    │   └── memory/
    │       └── MemoryDatabasePlugIn.java
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml
        │   ├── struts-config.xml
        │   ├── faces-config.xml
        │   ├── tiles-defs.xml
        │   ├── validation.xml
        │   └── database.xml
        ├── *.jsp
        └── stylesheet.css
```

**Target Structure:**
```
example2-spring-boot/
├── pom.xml
└── src/main/
    ├── java/org/apache/struts/webapp/example2/
    │   ├── Application.java
    │   ├── config/
    │   │   └── DatabaseConfiguration.java
    │   ├── controller/
    │   │   ├── LogonController.java
    │   │   ├── RegistrationController.java
    │   │   └── SubscriptionController.java
    │   ├── form/
    │   │   ├── LogonForm.java
    │   │   ├── RegistrationForm.java
    │   │   └── SubscriptionForm.java
    │   ├── domain/
    │   │   ├── User.java
    │   │   ├── Subscription.java
    │   │   └── UserDatabase.java
    │   └── repository/
    │       └── MemoryUserDatabase.java
    └── resources/
        ├── application.properties
        ├── messages.properties
        ├── database.xml
        ├── static/
        │   ├── css/stylesheet.css
        │   └── images/struts-power.gif
        └── templates/
            ├── layout/
            │   └── base.html
            ├── fragments/
            │   ├── header.html
            │   ├── footer.html
            │   ├── loggedoff-menu.html
            │   └── loggedon-menu.html
            ├── logon.html
            ├── mainMenu.html
            ├── registration.html
            ├── subscription.html
            └── welcome.html
```

## 6. Considerations and Risks

### 6.1 Architectural Significance

This migration represents a significant architectural change:

- **Framework replacement**: Moving from Struts 1.x (released 2000, EOL) to Spring Boot (actively maintained)
- **View technology change**: JSF to Thymeleaf requires complete view rewrite
- **Configuration paradigm shift**: XML-based to annotation-driven configuration
- **Deployment model change**: WAR to executable JAR

The effort should not be underestimated, even for this relatively small application.

### 6.2 Dual-Servlet Complexity

The original application's dual-servlet architecture (FacesServlet + ActionServlet) is unusual and adds complexity:

- JSF backing beans dispatch to Struts actions
- FacesTilesRequestProcessor bridges three frameworks
- Request lifecycle spans both JSF and Struts processing

This tight coupling means the migration cannot be done piecemeal at the servlet level. The recommended approach is to migrate completely to Spring MVC rather than attempting to maintain partial JSF integration.

### 6.3 JSF Retention Evaluation

Before proceeding, evaluate whether JSF features are truly needed:

**JSF features used in this application:**
- Managed beans for navigation (can be replaced with links)
- FacesContext for request/session access (Spring provides equivalents)
- Component binding (minimal usage)

**Recommendation:** The JSF usage in this application is minimal and primarily for navigation. Full replacement with Thymeleaf is recommended over maintaining JSF integration.

### 6.4 Testing Strategy

Comprehensive testing is critical:

- **Unit tests**: Test each controller method in isolation
- **Integration tests**: Test Spring context loading and bean wiring
- **End-to-end tests**: Test complete user flows
- **Regression tests**: Compare behavior with original application

### 6.5 Potential Risks

| Risk | Mitigation |
|------|------------|
| Behavioral differences | Parallel running and comparison testing |
| Missing edge cases | Thorough review of original Action code |
| Session handling changes | Explicit testing of session-based features |
| Validation message differences | Side-by-side comparison of error messages |
| URL pattern changes | Maintain same URL structure where possible |
| Performance regression | Load testing before and after |

### 6.6 Skills Required

The migration team should have experience with:
- Spring Boot and Spring MVC
- Thymeleaf template engine
- Bean Validation (JSR-380)
- Maven build configuration
- Understanding of Struts 1.x (for reading existing code)

### 6.7 Estimated Effort

For a developer familiar with both Struts and Spring:
- **Minimum**: 2-3 weeks for basic migration
- **Recommended**: 4-6 weeks including thorough testing
- **With documentation and cleanup**: 6-8 weeks

This estimate assumes the migration is the primary focus and not interrupted by other work.

## Appendix A: Quick Reference

### Struts to Spring MVC Cheat Sheet

| Struts Concept | Spring MVC Equivalent |
|----------------|----------------------|
| ActionServlet | DispatcherServlet (auto-configured) |
| Action | @Controller |
| ActionMapping | @RequestMapping |
| ActionForward | String return value / ModelAndView |
| ActionForm | @ModelAttribute POJO |
| ActionErrors | BindingResult |
| ActionMessage | FieldError / ObjectError |
| struts-config.xml | @Configuration + annotations |
| tiles-defs.xml | Thymeleaf layouts |
| validation.xml | Bean Validation annotations |
| PlugIn | @Configuration + @Bean |
| MessageResources | MessageSource |

### Common Annotation Mappings

```java
// Request mapping
@GetMapping("/path")      // Struts: <action path="/path" forward="..."/>
@PostMapping("/path")     // Struts: <action path="/path" type="..."/>

// Form handling
@ModelAttribute           // Struts: ActionForm parameter
@Valid                    // Struts: validate="true"
BindingResult            // Struts: ActionErrors

// Validation
@NotBlank                // Struts: depends="required"
@Size(min=X, max=Y)      // Struts: depends="minlength,maxlength"
@Email                   // Struts: depends="email"
@Pattern(regexp="...")   // Struts: depends="mask"

// Exception handling
@ExceptionHandler        // Struts: <exception> element
```

## Appendix B: Sample Migrated Controller

```java
package org.apache.struts.webapp.example2.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.apache.struts.webapp.example2.form.LogonForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LogonController {
    
    private final UserDatabase userDatabase;
    
    public LogonController(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }
    
    @GetMapping("/editLogon")
    public String showLogonForm(Model model) {
        model.addAttribute("logonForm", new LogonForm());
        return "logon";
    }
    
    @PostMapping("/logon")
    public String processLogon(@Valid @ModelAttribute LogonForm form,
                               BindingResult result,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "logon";
        }
        
        User user = userDatabase.findUser(form.getUsername());
        
        if (user == null || !user.getPassword().equals(form.getPassword())) {
            result.reject("error.password.mismatch");
            return "logon";
        }
        
        session.setAttribute("user", user);
        return "redirect:/mainMenu";
    }
    
    @GetMapping("/logoff")
    public String logoff(HttpSession session) {
        session.invalidate();
        return "redirect:/welcome";
    }
}
```

## Appendix C: Sample Thymeleaf Template

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title th:text="#{logon.title}">Log On</title>
</head>
<body>
    <div layout:fragment="menu">
        <div th:replace="~{fragments/loggedoff-menu :: menu}"></div>
    </div>
    
    <div layout:fragment="content">
        <h2 th:text="#{logon.title}">Log On</h2>
        
        <div th:if="${#fields.hasGlobalErrors()}" class="error">
            <p th:each="err : ${#fields.globalErrors()}" th:text="${err}">Error</p>
        </div>
        
        <form th:action="@{/logon}" th:object="${logonForm}" method="post">
            <table>
                <tr>
                    <td th:text="#{prompt.username}">Username:</td>
                    <td>
                        <input type="text" th:field="*{username}"/>
                        <span th:if="${#fields.hasErrors('username')}" 
                              th:errors="*{username}" class="error"></span>
                    </td>
                </tr>
                <tr>
                    <td th:text="#{prompt.password}">Password:</td>
                    <td>
                        <input type="password" th:field="*{password}"/>
                        <span th:if="${#fields.hasErrors('password')}" 
                              th:errors="*{password}" class="error"></span>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <button type="submit" th:text="#{button.submit}">Submit</button>
                        <button type="reset" th:text="#{button.reset}">Reset</button>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</body>
</html>
```
