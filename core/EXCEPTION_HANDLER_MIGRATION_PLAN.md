# Exception Handler Migration Plan: Struts 1 to Spring Boot

This document describes the full implementation plan for replacing the Struts 1 exception handling infrastructure with Spring Boot's `@ControllerAdvice` and `@ExceptionHandler` mechanism.

---

## Table of Contents

1. [Overview](#overview)
2. [Struts 1 Exception Handling Architecture (Current State)](#struts-1-exception-handling-architecture-current-state)
3. [Phase 1: Create Spring Boot Exception Infrastructure](#phase-1-create-spring-boot-exception-infrastructure)
4. [Phase 2: Migrate Exception Configuration from XML to Annotations](#phase-2-migrate-exception-configuration-from-xml-to-annotations)
5. [Phase 3: Handle Edge Cases](#phase-3-handle-edge-cases)
6. [Phase 4: Deprecation and Removal](#phase-4-deprecation-and-removal)
7. [Phase 5: Testing Strategy](#phase-5-testing-strategy)
8. [Appendix: Struts-to-Spring Mapping Reference](#appendix-struts-to-spring-mapping-reference)

---

## Overview

Struts 1 uses an XML-configured, class-based exception handling system where `<exception>` elements in `struts-config.xml` map Java exception types to handler classes and error views. Spring Boot replaces this entirely with annotation-driven `@ControllerAdvice` classes containing `@ExceptionHandler` methods, combined with Spring's `MessageSource` for i18n.

### Key Architectural Differences

| Concern | Struts 1 | Spring Boot |
|---------|----------|-------------|
| Exception-to-handler mapping | `<exception>` XML elements in `struts-config.xml` | `@ExceptionHandler` annotations on methods |
| Global handlers | `<global-exceptions>` block in XML | `@ControllerAdvice` class |
| Action-level handlers | `<exception>` nested inside `<action>` in XML | `@ExceptionHandler` methods directly on `@Controller` |
| Handler class | `ExceptionHandler` (instantiated per config) | `@ControllerAdvice` bean (singleton, managed by Spring) |
| Business exception | `ModuleException` (checked, extends `Exception`) | `BusinessException` (unchecked, extends `RuntimeException`) |
| Error messages | `MessageResources` bundles via `ExceptionConfig.bundle` | `MessageSource` injection via `@Autowired` |
| Error storage scope | `ExceptionConfig.scope` ("request" or "session") | Model attributes (request) or `HttpSession` / `RedirectAttributes` (session) |
| Error view resolution | `ExceptionConfig.path` returns an `ActionForward` | Return a view name `String` (resolved by `ViewResolver`) |
| Exception hierarchy walk | `ActionConfig.findException(Class)` manual superclass loop | Spring's built-in most-specific `@ExceptionHandler` resolution |
| Exception attribute | `request.setAttribute(Globals.EXCEPTION_KEY, ex)` | Model attribute added in `@ExceptionHandler` method |
| Error collection | `ActionMessages` under `Globals.ERROR_KEY` | Model attribute (e.g., `"errorMessage"`) or `BindingResult` |

---

## Struts 1 Exception Handling Architecture (Current State)

### Source Files Involved

| File | Role |
|------|------|
| `core/src/main/java/org/apache/struts/action/ExceptionHandler.java` | Default handler: builds `ActionForward`, stores error in request/session, handles committed responses (325 lines) |
| `core/src/main/java/org/apache/struts/config/ExceptionConfig.java` | JavaBean for `<exception>` XML element: holds `type`, `key`, `path`, `handler`, `scope`, `bundle` (426 lines) |
| `core/src/main/java/org/apache/struts/chain/commands/AbstractExceptionHandler.java` | Chain command: looks up `ExceptionConfig` via `ActionConfig.findException()`, delegates to `handle()` (131 lines) |
| `core/src/main/java/org/apache/struts/chain/commands/servlet/ExceptionHandler.java` | Servlet-specific chain command: instantiates the configured handler class and calls `execute()` (69 lines) |
| `core/src/main/java/org/apache/struts/util/ModuleException.java` | Business exception carrying a message key, message args, and optional property name (138 lines) |
| `core/src/main/java/org/apache/struts/config/ActionConfig.java` | Contains `findException(Class)` (lines 1101-1133): walks the exception class hierarchy checking local then global configs |
| `core/src/main/java/org/apache/struts/Globals.java` | Defines `EXCEPTION_KEY` (`"org.apache.struts.action.EXCEPTION"`) and `ERROR_KEY` (`"org.apache.struts.action.ERROR"`) |

### How It Works Today

1. An `Action.execute()` method throws an exception.
2. The `servlet-exception` chain (defined in `chain-config.xml`) catches it.
3. `AbstractExceptionHandler.execute()` looks up the `ExceptionConfig` via `ActionConfig.findException(exceptionClass)`, which walks the class hierarchy (local action-level configs first, then global module-level configs, then superclass, repeat).
4. `servlet.ExceptionHandler.handle()` instantiates the handler class specified by `ExceptionConfig.getHandler()` (default: `org.apache.struts.action.ExceptionHandler`).
5. `ExceptionHandler.execute()`:
   - Determines the forward path from `ExceptionConfig.path` or `ActionMapping.getInputForward()`
   - If the exception is a `ModuleException`, extracts its `ActionMessage` and `property`; otherwise creates an `ActionMessage` from `ExceptionConfig.key`
   - Stores the exception as `request.setAttribute(Globals.EXCEPTION_KEY, ex)`
   - Stores error messages as `ActionMessages` under `Globals.ERROR_KEY` in the configured scope (request or session)
   - If the response is already committed, attempts `RequestDispatcher.include()` or writes directly to the response

---

## Phase 1: Create Spring Boot Exception Infrastructure

### 1.1 Create `BusinessException` Class

**Replaces:** `core/src/main/java/org/apache/struts/util/ModuleException.java`

**New location:** `src/main/java/org/apache/struts/spring/exception/BusinessException.java`

**Design rationale:** `ModuleException` extends checked `Exception`, which is incompatible with Spring's proxy-based AOP and `@ExceptionHandler` pattern. Spring controllers should throw unchecked exceptions. `BusinessException` extends `RuntimeException` and carries the same semantic fields: a message key for i18n resolution, optional message arguments, and an optional property name for field-level error association.

```java
package org.apache.struts.spring.exception;

/**
 * Unchecked business exception that carries an i18n message key and optional
 * field-level property association. Replaces {@code ModuleException} from
 * the Struts 1 framework.
 *
 * <p>Semantics preserved from ModuleException:</p>
 * <ul>
 *   <li>{@code messageKey} — replaces {@code ActionMessage.getKey()}</li>
 *   <li>{@code messageArgs} — replaces {@code ActionMessage} constructor args</li>
 *   <li>{@code property} — replaces {@code ModuleException.getProperty()}</li>
 * </ul>
 */
public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final Object[] messageArgs;
    private final String property;

    /**
     * Construct with a message key only (no arguments, no property).
     *
     * @param messageKey the i18n message resource key
     */
    public BusinessException(String messageKey) {
        this(messageKey, null, null, null);
    }

    /**
     * Construct with a message key and arguments for placeholder substitution.
     *
     * @param messageKey  the i18n message resource key
     * @param messageArgs replacement values for the message template
     */
    public BusinessException(String messageKey, Object... messageArgs) {
        this(messageKey, messageArgs, null, null);
    }

    /**
     * Full constructor.
     *
     * @param messageKey  the i18n message resource key
     * @param messageArgs replacement values for the message template
     * @param property    optional form field name this error relates to
     * @param cause       optional root cause
     */
    public BusinessException(String messageKey, Object[] messageArgs,
                             String property, Throwable cause) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
        this.property = property;
    }

    /** Returns the i18n message resource key. */
    public String getMessageKey() {
        return messageKey;
    }

    /** Returns the message template arguments, or {@code null}. */
    public Object[] getMessageArgs() {
        return messageArgs;
    }

    /**
     * Returns the form property (field name) this error is associated with.
     * Falls back to {@code messageKey} if no explicit property was set,
     * preserving the behavior of {@code ModuleException.getProperty()}.
     */
    public String getProperty() {
        return (property != null) ? property : messageKey;
    }
}
```

**Migration mapping from `ModuleException`:**

| `ModuleException` | `BusinessException` | Notes |
|--------------------|---------------------|-------|
| `new ModuleException(key)` | `new BusinessException(key)` | Direct replacement |
| `new ModuleException(key, val1, val2)` | `new BusinessException(key, val1, val2)` | Varargs replaces overloaded constructors |
| `ex.getActionMessage().getKey()` | `ex.getMessageKey()` | No intermediate `ActionMessage` object |
| `ex.getProperty()` | `ex.getProperty()` | Same semantics; defaults to `messageKey` if null |
| `ex.setProperty(prop)` | Constructor parameter `property` | Immutable in Spring version |
| `throw new ModuleException(key)` (checked) | `throw new BusinessException(key)` (unchecked) | No `throws` clause needed |

---

### 1.2 Create `GlobalExceptionHandler` Class

**Replaces:**
- `core/src/main/java/org/apache/struts/action/ExceptionHandler.java` (the default handler)
- `<global-exceptions>` blocks in `struts-config.xml`

**New location:** `src/main/java/org/apache/struts/spring/exception/GlobalExceptionHandler.java`

**Design rationale:** A single `@ControllerAdvice` class replaces both the `ExceptionHandler` class and all `<global-exceptions>` XML declarations. Spring automatically routes exceptions to the most specific `@ExceptionHandler` method, eliminating the need for `ExceptionConfig` and the `findException()` hierarchy walk.

```java
package org.apache.struts.spring.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Locale;

/**
 * Global exception handler for the Spring Boot application.
 * Replaces Struts 1's {@code ExceptionHandler} class and
 * {@code <global-exceptions>} XML configuration.
 *
 * <p>Struts 1 equivalence:</p>
 * <ul>
 *   <li>{@code ExceptionHandler.execute()} &rarr; individual {@code @ExceptionHandler} methods</li>
 *   <li>{@code ExceptionConfig.key} &rarr; {@code BusinessException.getMessageKey()}</li>
 *   <li>{@code ExceptionConfig.path} &rarr; returned view name string</li>
 *   <li>{@code ExceptionConfig.bundle} &rarr; {@code MessageSource} (injected)</li>
 *   <li>{@code ExceptionConfig.scope} &rarr; model attributes (request scope by default)</li>
 *   <li>{@code request.setAttribute(Globals.EXCEPTION_KEY, ex)} &rarr; {@code model.addAttribute("exception", ex)}</li>
 *   <li>{@code ActionMessages} under {@code Globals.ERROR_KEY} &rarr; {@code model.addAttribute("errorMessage", msg)}</li>
 * </ul>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Replaces Struts MessageResources bundles referenced by
     * ExceptionConfig.bundle. Spring's MessageSource is configured
     * via application.properties: spring.messages.basename=messages
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * Handle BusinessException (replaces ModuleException handling in
     * ExceptionHandler.execute(), lines 143-155).
     *
     * <p>Struts 1 equivalent flow:</p>
     * <ol>
     *   <li>{@code ex instanceof ModuleException} check</li>
     *   <li>Extract {@code ActionMessage} and {@code property}</li>
     *   <li>Store in {@code ActionMessages} under {@code Globals.ERROR_KEY}</li>
     *   <li>Forward to {@code ExceptionConfig.path}</li>
     * </ol>
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException ex, Model model) {
        Locale locale = LocaleContextHolder.getLocale();

        // Resolve the i18n message (replaces MessageResources lookup)
        String errorMessage = messageSource.getMessage(
                ex.getMessageKey(),
                ex.getMessageArgs(),
                ex.getMessageKey(), // fallback to key itself
                locale
        );

        // Replaces: request.setAttribute(Globals.EXCEPTION_KEY, ex)
        model.addAttribute("exception", ex);

        // Replaces: ActionMessages under Globals.ERROR_KEY
        model.addAttribute("errorMessage", errorMessage);

        // Replaces: ModuleException.getProperty() for field-level association
        model.addAttribute("errorProperty", ex.getProperty());

        log.debug("BusinessException handled: key={}, property={}",
                ex.getMessageKey(), ex.getProperty());

        // Replaces: ExceptionConfig.path (the error view)
        return "error/business-error";
    }

    /**
     * Handle IOException (replaces XML declarations like:
     * {@code <exception type="java.io.IOException" key="..." path="..."/>}).
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIOException(IOException ex, Model model) {
        log.error("IOException occurred", ex);

        model.addAttribute("exception", ex);
        model.addAttribute("errorMessage", "An I/O error occurred. Please try again later.");

        return "error/io-error";
    }

    /**
     * Catch-all handler for any unhandled exception.
     *
     * <p>In Struts 1, if no matching {@code <exception>} element was found
     * for a thrown exception (after walking the entire class hierarchy in
     * {@code ActionConfig.findException()}), the exception would propagate
     * to the servlet container. This catch-all provides a controlled
     * fallback instead.</p>
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unhandled exception caught by global handler", ex);

        model.addAttribute("exception", ex);
        model.addAttribute("errorMessage",
                "An unexpected error occurred. Please contact support.");

        return "error/generic-error";
    }
}
```

**MessageSource configuration** (replaces Struts `MessageResources` bundles):

```properties
# application.properties
spring.messages.basename=messages,org.apache.struts.action.LocalStrings
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
```

---

### 1.3 Create `SessionScopeExceptionHandler` (Optional)

**Replaces:** `ExceptionConfig.scope = "session"` handling in `ExceptionHandler.storeException()` (lines 296-309)

In Struts 1, when `ExceptionConfig.scope` is `"session"`, errors are stored via `request.getSession().setAttribute(Globals.ERROR_KEY, errors)`. In Spring Boot, there are two approaches:

#### Option A: Direct `HttpSession` Injection

For cases where errors must persist across multiple requests within the same session:

```java
@ControllerAdvice
public class SessionScopeExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    /**
     * Handle exceptions that require session-scoped error storage.
     * Replaces ExceptionHandler.storeException() with scope="session".
     *
     * Spring injects the HttpSession automatically when declared
     * as a method parameter.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleSessionScopedException(BusinessException ex,
                                                HttpSession session,
                                                Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage(
                ex.getMessageKey(), ex.getMessageArgs(),
                ex.getMessageKey(), locale);

        // Replaces: request.getSession().setAttribute(Globals.ERROR_KEY, errors)
        session.setAttribute("errorMessage", errorMessage);
        session.setAttribute("errorProperty", ex.getProperty());

        model.addAttribute("exception", ex);
        return "error/business-error";
    }
}
```

#### Option B: `RedirectAttributes` for Post-Redirect-Get Pattern

For post-redirect-get (PRG) flows where errors must survive a redirect:

```java
@ExceptionHandler(BusinessException.class)
public String handleWithRedirect(BusinessException ex,
                                  RedirectAttributes redirectAttributes) {
    Locale locale = LocaleContextHolder.getLocale();
    String errorMessage = messageSource.getMessage(
            ex.getMessageKey(), ex.getMessageArgs(),
            ex.getMessageKey(), locale);

    // Flash attributes survive one redirect, then are removed
    // This is the Spring equivalent of session-scoped error storage
    // for single-redirect scenarios
    redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
    redirectAttributes.addFlashAttribute("errorProperty", ex.getProperty());
    redirectAttributes.addFlashAttribute("exception", ex);

    return "redirect:/error";
}
```

**Recommendation:** Use Option B (`RedirectAttributes`) for most cases. Use Option A (direct `HttpSession`) only when errors must persist across multiple requests beyond a single redirect.

---

## Phase 2: Migrate Exception Configuration from XML to Annotations

### 2.1 Audit of All `<exception>` Elements in struts-config.xml Files

The following table documents every `<exception>` element found across all `struts-config.xml` files in the `apps/` directory:

#### Global Exceptions (`<global-exceptions>`)

| Config File | Exception Type | Key | Path | Handler | Scope | Notes |
|-------------|---------------|-----|------|---------|-------|-------|
| `apps/blank/.../struts-config.xml` | `app.ExpiredPasswordException` | `expired.password` | `/changePassword.jsp` | (default) | request | Commented out (sample) |
| `apps/cookbook/.../struts-config.xml` | _(none)_ | — | — | — | — | Empty `<global-exceptions>` block |

#### Action-Level Exceptions (nested inside `<action>`)

| Config File | Action Path | Exception Type | Key | Path | Handler | Scope |
|-------------|-------------|---------------|-----|------|---------|-------|
| `apps/mailreader/.../struts-config.xml` | `/LogonSubmit` | `o.a.s.apps.mailreader.dao.ExpiredPasswordException` | `expired.password` | `/ChangePassword.do` | (default) | request |
| `apps/scripting-mailreader/.../struts-config.xml` | `/LogonSubmit` | `o.a.s.apps.mailreader.dao.ExpiredPasswordException` | `expired.password` | `/ChangePassword.do` | (default) | request |
| `apps/faces-example1/.../struts-config.xml` | `/logon` | `o.a.s.webapp.example.ExpiredPasswordException` | `expired.password` | `/changePassword.faces` | (default) | request |
| `apps/faces-example2/.../struts-config.xml` | `/logon` | `o.a.s.webapp.example2.ExpiredPasswordException` | `expired.password` | `/changePassword.faces` | (default) | request |
| `apps/examples/.../exercise/struts-config.xml` | `/html-cancel` | `o.a.s.action.InvalidCancelException` | `errors.invalidCancel` | `/html-cancel.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/dispatch` | `java.lang.NoSuchMethodException` | `dispatch.NoSuchMethodException` | `/dispatch.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/dispatch` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/dispatch.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/dispatch-noparam` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/dispatch.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/mapping` | `java.lang.NoSuchMethodException` | `dispatch.NoSuchMethodException` | `/mapping.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/mapping` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/mapping.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/mapping-execute` | `java.lang.NoSuchMethodException` | `dispatch.NoSuchMethodException` | `/mapping.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/mapping-execute` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/mapping.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/mapping-noparam` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/mapping.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/lookup` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/lookup.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/lookup-noparam` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/lookup.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/actionDispatcher` | `java.lang.NoSuchMethodException` | `dispatch.NoSuchMethodException` | `/actionDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/actionDispatcher` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/actionDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/actionDispatcher-noparam` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/actionDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventDispatcher` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/eventDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventDispatcher-error` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/eventDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventDispatcher-error` | `java.lang.NoSuchMethodException` | `dispatch.NoSuchMethodException` | `/eventDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventDispatcher-noparam` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/eventDispatcher.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventAction` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/eventAction.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventAction-error` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/eventAction.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventAction-error` | `java.lang.NoSuchMethodException` | `dispatch.NoSuchMethodException` | `/eventAction.jsp` | (default) | request |
| `apps/examples/.../dispatch/struts-config.xml` | `/eventAction-noparam` | `javax.servlet.ServletException` | `dispatch.ServletException` | `/eventAction.jsp` | (default) | request |

**Key observations from the audit:**
- **No custom handler classes**: All `<exception>` elements use the default `org.apache.struts.action.ExceptionHandler`. No custom `ExceptionHandler` subclasses exist in the codebase.
- **All request-scoped**: Every exception declaration uses the default `scope="request"`. No session-scoped exception handling is currently configured.
- **No `bundle` overrides**: All exceptions use the default message resource bundle. No `bundle` attribute is set.
- **Three distinct exception type families**:
  1. `ExpiredPasswordException` variants (business logic)
  2. `InvalidCancelException` (framework)
  3. `NoSuchMethodException` / `ServletException` (dispatch errors)

---

### 2.2 Map XML Exception Declarations to `@ExceptionHandler` Methods

#### Global Exceptions to `@ControllerAdvice`

Since the only global exception (`apps/blank`) is a commented-out sample, no actual global `<exception>` elements need migration. The `GlobalExceptionHandler` class created in Phase 1 serves as the global catch-all.

#### Action-Level Exceptions to `@Controller` Methods

Action-level `<exception>` elements should be migrated to `@ExceptionHandler` methods on the corresponding `@Controller` class. When the same exception type is handled identically across multiple actions (as with `ServletException` in the dispatch examples), the handler can be placed in a module-specific `@ControllerAdvice`.

**Example: Migrating the mailreader `ExpiredPasswordException`**

Struts 1 XML:
```xml
<action path="/LogonSubmit" type="...LogonAction" input="Logon">
    <exception
        key="expired.password"
        type="org.apache.struts.apps.mailreader.dao.ExpiredPasswordException"
        path="/ChangePassword.do"/>
</action>
```

Spring Boot equivalent (on the `@Controller`):
```java
@Controller
public class LogonController {

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/logon")
    public String submitLogon(/* ... */) {
        // ... logon logic that may throw ExpiredPasswordException
    }

    /**
     * Replaces: <exception key="expired.password"
     *            type="...ExpiredPasswordException"
     *            path="/ChangePassword.do"/>
     */
    @ExceptionHandler(ExpiredPasswordException.class)
    public String handleExpiredPassword(ExpiredPasswordException ex, Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage("expired.password", null, locale);

        model.addAttribute("exception", ex);
        model.addAttribute("errorMessage", msg);

        // path="/ChangePassword.do" becomes a redirect or view name
        return "redirect:/changePassword";
    }
}
```

**Example: Migrating dispatch `ServletException` handlers**

Since many dispatch actions share the same exception handling, create a module-scoped `@ControllerAdvice`:

```java
/**
 * Replaces all action-level <exception> elements for the dispatch module.
 * Uses @ControllerAdvice with basePackages to scope to dispatch controllers.
 */
@ControllerAdvice(basePackages = "org.apache.struts.spring.controller.dispatch")
public class DispatchExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(NoSuchMethodException.class)
    public String handleNoSuchMethod(NoSuchMethodException ex, Model model,
                                      HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage(
                "dispatch.NoSuchMethodException", null, locale);

        model.addAttribute("exception", ex);
        model.addAttribute("errorMessage", msg);

        // Determine view based on originating controller context
        return "dispatch/error";
    }

    @ExceptionHandler(ServletException.class)
    public String handleServletException(ServletException ex, Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage(
                "dispatch.ServletException", null, locale);

        model.addAttribute("exception", ex);
        model.addAttribute("errorMessage", msg);

        return "dispatch/error";
    }
}
```

---

### 2.3 Identify Custom `ExceptionHandler` Subclasses

**Finding: No custom subclasses exist.** A search for classes extending `org.apache.struts.action.ExceptionHandler` returned zero results. All `<exception>` elements use the default handler.

**If custom handlers are added in the future**, the migration pattern is:

1. Each custom `ExceptionHandler` subclass becomes a separate `@ControllerAdvice` class.
2. Use `@Order` annotations to control precedence (lower values = higher priority):

```java
@ControllerAdvice
@Order(1)  // Higher priority than GlobalExceptionHandler
public class CustomBusinessExceptionHandler {

    @ExceptionHandler(SpecificBusinessException.class)
    public String handleSpecific(SpecificBusinessException ex, Model model) {
        // Custom handling logic from the ExceptionHandler subclass
        return "error/specific";
    }
}

@ControllerAdvice
@Order(2)  // Lower priority — catch-all
public class GlobalExceptionHandler {
    // ... as defined in Phase 1
}
```

---

## Phase 3: Handle Edge Cases

### 3.1 Committed Response Handling

**Struts 1 behavior:** `ExceptionHandler.handleCommittedResponse()` (lines 197-242) implements complex fallback logic when `response.isCommitted()` returns `true`:

1. Check for `INCLUDE_PATH` property on `ExceptionConfig` and attempt `RequestDispatcher.include()`
2. If include path starts with `/`, dispatch to it
3. If include fails, write exception details directly to the response writer
4. If `SILENT_IF_COMMITTED` property is `"true"`, log a warning and do nothing

**Spring Boot equivalent:** This complexity is **not needed** in Spring Boot. Spring MVC's `DispatcherServlet` handles exceptions before the response is committed to the client. The `@ExceptionHandler` mechanism operates at the controller level, before view rendering begins.

**However**, if the application has streaming endpoints or async processing where the response may be partially committed:

```java
/**
 * Custom HandlerExceptionResolver for committed-response edge cases.
 * Only needed if the application has streaming/async endpoints.
 * Replaces ExceptionHandler.handleCommittedResponse() (lines 197-242).
 */
@Component
public class CommittedResponseExceptionResolver implements HandlerExceptionResolver {

    private static final Logger log =
            LoggerFactory.getLogger(CommittedResponseExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                          HttpServletResponse response,
                                          Object handler, Exception ex) {
        if (response.isCommitted()) {
            log.warn("Response already committed when exception occurred. "
                    + "Attempting to write error information.", ex);
            try {
                response.getWriter().println("<!-- Error: " + ex.getMessage() + " -->");
                response.getWriter().flush();
            } catch (IOException e) {
                log.error("Failed to write error to committed response", e);
            }
            // Return empty ModelAndView to indicate the exception was handled
            return new ModelAndView();
        }
        // Return null to let other resolvers (including @ExceptionHandler) handle it
        return null;
    }
}
```

**Recommendation:** Do not implement `CommittedResponseExceptionResolver` unless testing reveals committed-response issues. The Struts 1 `INCLUDE_PATH` and `SILENT_IF_COMMITTED` properties had no usages in the current codebase.

---

### 3.2 Exception Hierarchy Resolution

**Struts 1 behavior:** `ActionConfig.findException(Class)` (lines 1101-1133) manually walks the exception class superclass chain:

```java
// Struts 1 — manual hierarchy walk
public ExceptionConfig findException(Class type) {
    while (true) {
        String name = type.getName();
        config = findExceptionConfig(name);        // check local (action-level)
        if (config != null) return config;
        config = getModuleConfig().findExceptionConfig(name);  // check global
        if (config != null) return config;
        type = type.getSuperclass();                // walk up to parent
        if (type == null) break;
    }
    return null;
}
```

**Spring Boot equivalent:** Spring's `@ExceptionHandler` mechanism **natively resolves the most specific exception type**. No manual hierarchy walk is needed. When multiple `@ExceptionHandler` methods could match (e.g., one for `BusinessException` and one for `Exception`), Spring selects the most specific match automatically.

**Example of hierarchy resolution:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles BusinessException and all its subclasses
    // Spring will route ExpiredPasswordException here if no more
    // specific handler exists
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, Model model) { ... }

    // More specific: handles only ExpiredPasswordException
    // Spring prefers this over handleBusiness() when an
    // ExpiredPasswordException is thrown
    @ExceptionHandler(ExpiredPasswordException.class)
    public String handleExpiredPassword(ExpiredPasswordException ex, Model model) { ... }

    // Least specific: catches everything else
    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) { ... }
}
```

**Precedence rules:**
1. `@ExceptionHandler` on the `@Controller` class takes priority over `@ControllerAdvice`
2. Among `@ControllerAdvice` classes, `@Order` controls precedence
3. Among methods in the same class, the most specific exception type wins

This replaces the two-level lookup (local → global) and superclass walk in Struts 1 with a cleaner annotation-based model.

---

### 3.3 Error View Rendering

**Migration of Struts JSP error pages to Spring Boot view templates:**

| Struts 1 JSP | Spring Boot Template | Model Attributes Available |
|--------------|---------------------|---------------------------|
| `ExceptionConfig.path` (e.g., `/error.jsp`) | View name returned from `@ExceptionHandler` (e.g., `"error/business-error"`) | `exception`, `errorMessage`, `errorProperty` |
| JSP accessing `${requestScope['org.apache.struts.action.EXCEPTION']}` | Thymeleaf/JSP accessing `${exception}` | Direct model attribute |
| JSP accessing `<html:errors/>` tag | Thymeleaf: `${errorMessage}` or Spring's `<form:errors/>` | Direct model attribute |

**Example Thymeleaf error template** (`src/main/resources/templates/error/business-error.html`):

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Error</title>
</head>
<body>
    <div class="error-container">
        <h1>An Error Occurred</h1>

        <!-- Replaces <html:errors/> tag and Globals.ERROR_KEY lookup -->
        <p th:if="${errorMessage}" th:text="${errorMessage}" class="error-message"></p>

        <!-- Field-level error (replaces ModuleException.getProperty()) -->
        <p th:if="${errorProperty}" class="error-field">
            Related field: <span th:text="${errorProperty}"></span>
        </p>

        <!-- Exception details (replaces Globals.EXCEPTION_KEY) -->
        <details th:if="${exception}">
            <summary>Technical Details</summary>
            <pre th:text="${exception.message}"></pre>
        </details>
    </div>
</body>
</html>
```

**If retaining JSP** (under `src/main/webapp/WEB-INF/views/`):

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>
    <h1>Error</h1>
    <!-- Replaces request.getAttribute(Globals.ERROR_KEY) -->
    <p>${errorMessage}</p>

    <!-- Replaces request.getAttribute(Globals.EXCEPTION_KEY) -->
    <c:if test="${not empty exception}">
        <p>Details: ${exception.message}</p>
    </c:if>
</body>
</html>
```

---

## Phase 4: Deprecation and Removal

Once the Spring Boot exception handling infrastructure is in place and all applications have been migrated, the following Struts 1 files should be deprecated and eventually removed:

### Files to Remove

| File | Lines | Reason |
|------|-------|--------|
| `core/src/main/java/org/apache/struts/action/ExceptionHandler.java` | 325 | Replaced by `GlobalExceptionHandler` `@ControllerAdvice` |
| `core/src/main/java/org/apache/struts/config/ExceptionConfig.java` | 426 | Replaced by `@ExceptionHandler` annotations; no XML config needed |
| `core/src/main/java/org/apache/struts/chain/commands/AbstractExceptionHandler.java` | 131 | Chain command replaced by Spring's `HandlerExceptionResolver` infrastructure |
| `core/src/main/java/org/apache/struts/chain/commands/servlet/ExceptionHandler.java` | 69 | Servlet-specific chain command; no longer needed |
| `core/src/main/java/org/apache/struts/util/ModuleException.java` | 138 | Replaced by `BusinessException` |

### XML Configuration to Remove

All `<global-exceptions>` blocks and `<exception>` elements within `<action>` mappings across:

| Config File | Elements to Remove |
|-------------|-------------------|
| `apps/blank/src/main/webapp/WEB-INF/struts-config.xml` | `<global-exceptions>` block (lines 66-73, commented-out sample) |
| `apps/cookbook/src/main/webapp/WEB-INF/struts-config.xml` | `<global-exceptions>` block (line 98-99, empty) |
| `apps/mailreader/src/main/webapp/WEB-INF/struts-config.xml` | `<exception>` in `/LogonSubmit` action (lines 145-148) |
| `apps/scripting-mailreader/src/main/webapp/WEB-INF/struts-config.xml` | `<exception>` in `/LogonSubmit` action (lines 141-144) |
| `apps/faces-example1/src/main/webapp/WEB-INF/struts-config.xml` | `<exception>` in `/logon` action (lines 128-131) |
| `apps/faces-example2/src/main/webapp/WEB-INF/struts-config.xml` | `<exception>` in `/logon` action (lines 134-137) |
| `apps/examples/src/main/webapp/WEB-INF/exercise/struts-config.xml` | `<exception>` in `/html-cancel` action (lines 53-56) |
| `apps/examples/src/main/webapp/WEB-INF/dispatch/struts-config.xml` | All `<exception>` elements (26 total across 13 actions) |

### Related Code to Update

| File / Area | Change Required |
|-------------|----------------|
| `core/src/main/java/org/apache/struts/Globals.java` | Remove `EXCEPTION_KEY` and `ERROR_KEY` constants (or deprecate) |
| `core/src/main/java/org/apache/struts/config/ActionConfig.java` | Remove `findException(Class)` method (lines 1101-1133), `findExceptionConfig()`, `findExceptionConfigs()`, `addExceptionConfig()`, `removeExceptionConfig()`, and the `exceptions` HashMap |
| `core/src/main/java/org/apache/struts/config/ModuleConfig.java` | Remove `findExceptionConfig()` and related methods |
| `core/src/main/java/org/apache/struts/config/ConfigRuleSet.java` | Remove Digester rules that parse `<exception>` and `<global-exceptions>` elements |
| `core/src/main/resources/org/apache/struts/chain/chain-config.xml` | Remove the `servlet-exception` chain or its `ExceptionHandler` command |
| `core/src/main/resources/org/apache/struts/resources/struts-config_1_4.dtd` | DTD elements for `<exception>` and `<global-exceptions>` become obsolete |

---

## Phase 5: Testing Strategy

### 5.1 Unit Tests for `@ExceptionHandler` Methods

Use `MockMvc` with `@WebMvcTest` to test each handler in isolation:

```java
@WebMvcTest(controllers = TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageSource messageSource;

    /**
     * Test BusinessException handling.
     * Verifies: correct view, model attributes, HTTP status.
     * Replaces testing of ExceptionHandler.execute() with ModuleException.
     */
    @Test
    void handleBusinessException_returnsErrorView() throws Exception {
        when(messageSource.getMessage(eq("expired.password"), any(), any(), any()))
                .thenReturn("Your password has expired");

        mockMvc.perform(post("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/business-error"))
                .andExpect(model().attribute("errorMessage", "Your password has expired"))
                .andExpect(model().attribute("errorProperty", "expired.password"))
                .andExpect(model().attributeExists("exception"));
    }

    /**
     * Test IOException handling.
     */
    @Test
    void handleIOException_returnsIOErrorView() throws Exception {
        mockMvc.perform(get("/test/io-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/io-error"))
                .andExpect(model().attributeExists("exception"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    /**
     * Test catch-all handler.
     */
    @Test
    void handleGenericException_returnsGenericErrorView() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/generic-error"))
                .andExpect(model().attributeExists("exception"));
    }
}
```

### 5.2 Test Exception Hierarchy Resolution

```java
/**
 * Verify that Spring resolves the most specific handler.
 * Replaces testing of ActionConfig.findException() hierarchy walk.
 */
@Test
void subclassOfBusinessException_usesSpecificHandler() throws Exception {
    // ExpiredPasswordException extends BusinessException
    // If a specific @ExceptionHandler(ExpiredPasswordException.class) exists,
    // it should be invoked instead of @ExceptionHandler(BusinessException.class)
    mockMvc.perform(post("/test/expired-password"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("redirect:/changePassword"));
}

@Test
void subclassWithNoSpecificHandler_fallsToParentHandler() throws Exception {
    // CustomBusinessException extends BusinessException
    // No specific handler exists, so BusinessException handler should be used
    mockMvc.perform(post("/test/custom-business-error"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("error/business-error"));
}
```

### 5.3 Test Session-Scoped Error Storage

```java
/**
 * Test session-scoped error storage.
 * Replaces testing of ExceptionHandler.storeException() with scope="session".
 */
@Test
void sessionScopedError_persistsAcrossRequests() throws Exception {
    MvcResult result = mockMvc.perform(post("/test/session-error"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

    // Verify flash attributes survive the redirect
    FlashMap flashMap = result.getFlashMap();
    assertNotNull(flashMap.get("errorMessage"));
    assertNotNull(flashMap.get("errorProperty"));
}
```

### 5.4 Integration Test: Full Request Cycle

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * End-to-end test: throw exception from controller,
     * verify global handler catches it, resolves message,
     * and returns correct view with all model attributes.
     */
    @Test
    void fullCycle_businessExceptionThrownInController() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "testuser")
                        .param("password", "expired"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/business-error"))
                .andExpect(model().attribute("errorMessage", notNullValue()))
                .andExpect(model().attribute("exception",
                        instanceOf(BusinessException.class)));
    }
}
```

### 5.5 Test Checklist

| Test Case | What It Validates | Struts 1 Equivalent |
|-----------|-------------------|---------------------|
| BusinessException returns correct view | `ExceptionConfig.path` mapping | `ExceptionHandler.execute()` forward |
| BusinessException resolves i18n message | `MessageSource` replaces `MessageResources` | `ExceptionConfig.key` lookup |
| BusinessException carries property in model | Field-level error association | `ModuleException.getProperty()` |
| IOException returns 500 with error view | IO error handling | `<exception type="java.io.IOException">` |
| Generic Exception catch-all works | Fallback for unmapped exceptions | Exception propagating past `findException()` |
| Subclass resolves to most specific handler | Exception hierarchy resolution | `ActionConfig.findException(Class)` walk |
| Subclass falls back to parent handler | Hierarchy fallback | Superclass loop in `findException()` |
| Session-scoped errors persist via flash attrs | PRG error pattern | `ExceptionConfig.scope="session"` |
| Model contains `exception` attribute | Exception available in view | `Globals.EXCEPTION_KEY` |
| Model contains `errorMessage` attribute | Error message in view | `Globals.ERROR_KEY` / `ActionMessages` |
| HTTP status codes are correct | 400 for business, 500 for system | Not directly in Struts 1 (always 200) |

---

## Appendix: Struts-to-Spring Mapping Reference

### ExceptionConfig Attributes to Spring Equivalents

| `ExceptionConfig` Attribute | XML Example | Spring Boot Equivalent | Location |
|------------------------------|-------------|----------------------|----------|
| `type` | `type="java.io.IOException"` | `@ExceptionHandler(IOException.class)` | Method annotation parameter |
| `key` | `key="expired.password"` | `messageSource.getMessage("expired.password", ...)` | Inside handler method body |
| `path` | `path="/error.jsp"` | `return "error/business-error"` | Handler method return value |
| `handler` | `handler="com.example.MyHandler"` | Separate `@ControllerAdvice` class with `@Order` | Class-level annotation |
| `scope` | `scope="session"` | `session.setAttribute(...)` or `redirectAttributes.addFlashAttribute(...)` | Handler method parameter injection |
| `bundle` | `bundle="alternate"` | `MessageSource` configured with multiple basenames | `spring.messages.basename` property |

### Globals Constants to Spring Model Attributes

| Struts 1 Constant | Value | Spring Boot Equivalent |
|--------------------|-------|----------------------|
| `Globals.EXCEPTION_KEY` | `"org.apache.struts.action.EXCEPTION"` | `model.addAttribute("exception", ex)` |
| `Globals.ERROR_KEY` | `"org.apache.struts.action.ERROR"` | `model.addAttribute("errorMessage", msg)` |

### Class Replacement Summary

| Struts 1 Class | Spring Boot Replacement |
|----------------|------------------------|
| `org.apache.struts.util.ModuleException` | `org.apache.struts.spring.exception.BusinessException` |
| `org.apache.struts.action.ExceptionHandler` | `org.apache.struts.spring.exception.GlobalExceptionHandler` |
| `org.apache.struts.config.ExceptionConfig` | `@ExceptionHandler` annotation (no config class needed) |
| `org.apache.struts.chain.commands.AbstractExceptionHandler` | Spring's `HandlerExceptionResolver` (built-in) |
| `org.apache.struts.chain.commands.servlet.ExceptionHandler` | Spring's `ExceptionHandlerExceptionResolver` (built-in) |
| `org.apache.struts.action.ActionMessage` | `MessageSource.getMessage()` return value |
| `org.apache.struts.action.ActionMessages` | Model attributes or `BindingResult` |
| `org.apache.struts.util.MessageResources` | `org.springframework.context.MessageSource` |
