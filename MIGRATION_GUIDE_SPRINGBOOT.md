# Struts 1 to Spring Boot Migration Guide

This document provides a comprehensive migration plan for converting the Apache Struts 1 nested tag library and supporting infrastructure to Spring Boot with Thymeleaf.

---

## Table of Contents

1. [Inventory of Components to Migrate](#section-1-inventory-of-components-to-migrate)
2. [Tag-by-Tag Migration Mapping](#section-2-tag-by-tag-migration-mapping)
3. [Relative Property Navigation Migration](#section-3-relative-property-navigation-migration)
4. [Step-by-Step Migration Procedure](#section-4-step-by-step-migration-procedure)
5. [Risk Areas and Edge Cases](#section-5-risk-areas-and-edge-cases)

---

## Section 1: Inventory of Components to Migrate

### 1.1 Nested Tag Classes

All nested tag classes reside under `taglib/src/main/java/org/apache/struts/taglib/nested/` and its sub-packages.

#### Core Nested Tags (root package)

| Class | Struts Parent Class | Interface(s) | Purpose |
|---|---|---|---|
| `NestedPropertyTag` | `BodyTagSupport` | `NestedNameSupport` | The `nested:nest` tag -- defines a new nesting level for child tags |
| `NestedRootTag` | `BodyTagSupport` | `NestedNameSupport` | The `nested:root` tag -- starts a nested hierarchy without a form |
| `NestedWriteNestingTag` | `BodyTagSupport` | *(none)* | The `nested:writeNesting` tag -- writes or exposes the current nesting level |
| `NestedReference` | `Serializable` | *(none)* | Holds the current bean name and nested property path in request scope |

#### Bean Sub-package (`nested/bean/`)

| Class | Struts Parent Class | Interface(s) | Purpose |
|---|---|---|---|
| `NestedDefineTag` | `DefineTag` | `NestedNameSupport` | Nested extension of `<bean:define>` |
| `NestedDefineTei` | `IterateTei` | *(none)* | TEI class for `NestedDefineTag` |
| `NestedMessageTag` | `MessageTag` | `NestedNameSupport` | Nested extension of `<bean:message>` |
| `NestedSizeTag` | `SizeTag` | `NestedNameSupport` | Nested extension of `<bean:size>` |
| `NestedWriteTag` | `WriteTag` | `NestedNameSupport` | Nested extension of `<bean:write>` |

#### HTML Sub-package (`nested/html/`)

| Class | Struts Parent Class | Interface(s) | Purpose |
|---|---|---|---|
| `NestedCheckboxTag` | `CheckboxTag` | `NestedNameSupport` | Nested extension of `<html:checkbox>` |
| `NestedErrorsTag` | `ErrorsTag` | `NestedPropertySupport` | Nested extension of `<html:errors>` |
| `NestedFileTag` | `FileTag` | `NestedNameSupport` | Nested extension of `<html:file>` |
| `NestedFormTag` | `FormTag` | `NestedNameSupport` | Nested extension of `<html:form>` |
| `NestedHiddenTag` | `HiddenTag` | `NestedNameSupport` | Nested extension of `<html:hidden>` |
| `NestedImageTag` | `ImageTag` | `NestedPropertySupport` | Nested extension of `<html:image>` |
| `NestedImgTag` | `ImgTag` | `NestedNameSupport` | Nested extension of `<html:img>` |
| `NestedLinkTag` | `LinkTag` | `NestedNameSupport` | Nested extension of `<html:link>` |
| `NestedMessagesTag` | `MessagesTag` | `NestedPropertySupport` | Nested extension of `<html:messages>` |
| `NestedMultiboxTag` | `MultiboxTag` | `NestedNameSupport` | Nested extension of `<html:multibox>` |
| `NestedOptionsCollectionTag` | `OptionsCollectionTag` | `NestedNameSupport` | Nested extension of `<html:optionsCollection>` |
| `NestedOptionsTag` | `OptionsTag` | `NestedNameSupport` | Nested extension of `<html:options>` |
| `NestedPasswordTag` | `PasswordTag` | `NestedNameSupport` | Nested extension of `<html:password>` |
| `NestedRadioTag` | `RadioTag` | `NestedNameSupport` | Nested extension of `<html:radio>` |
| `NestedSelectTag` | `SelectTag` | `NestedNameSupport` | Nested extension of `<html:select>` |
| `NestedSubmitTag` | `SubmitTag` | `NestedPropertySupport` | Nested extension of `<html:submit>` |
| `NestedTextTag` | `TextTag` | `NestedNameSupport` | Nested extension of `<html:text>` |
| `NestedTextareaTag` | `TextareaTag` | `NestedNameSupport` | Nested extension of `<html:textarea>` |

#### Logic Sub-package (`nested/logic/`)

| Class | Struts Parent Class | Interface(s) | Purpose |
|---|---|---|---|
| `NestedEmptyTag` | `EmptyTag` | `NestedNameSupport` | Nested extension of `<logic:empty>` |
| `NestedEqualTag` | `EqualTag` | `NestedNameSupport` | Nested extension of `<logic:equal>` |
| `NestedGreaterEqualTag` | `GreaterEqualTag` | `NestedNameSupport` | Nested extension of `<logic:greaterEqual>` |
| `NestedGreaterThanTag` | `GreaterThanTag` | `NestedNameSupport` | Nested extension of `<logic:greaterThan>` |
| `NestedIterateTag` | `IterateTag` | `NestedNameSupport` | Nested extension of `<logic:iterate>` |
| `NestedIterateTei` | `IterateTei` | *(none)* | TEI class for `NestedIterateTag` |
| `NestedLessEqualTag` | `LessEqualTag` | `NestedNameSupport` | Nested extension of `<logic:lessEqual>` |
| `NestedLessThanTag` | `LessThanTag` | `NestedNameSupport` | Nested extension of `<logic:lessThan>` |
| `NestedMatchTag` | `MatchTag` | `NestedNameSupport` | Nested extension of `<logic:match>` |
| `NestedMessagesPresentTag` | `MessagesPresentTag` | `NestedNameSupport` | Nested extension of `<logic:messagesPresent>` |
| `NestedMessagesNotPresentTag` | `MessagesNotPresentTag` | `NestedNameSupport` | Nested extension of `<logic:messagesNotPresent>` |
| `NestedNotEmptyTag` | `NotEmptyTag` | `NestedNameSupport` | Nested extension of `<logic:notEmpty>` |
| `NestedNotEqualTag` | `NotEqualTag` | `NestedNameSupport` | Nested extension of `<logic:notEqual>` |
| `NestedNotMatchTag` | `NotMatchTag` | `NestedNameSupport` | Nested extension of `<logic:notMatch>` |
| `NestedNotPresentTag` | `NotPresentTag` | `NestedNameSupport` | Nested extension of `<logic:notPresent>` |
| `NestedPresentTag` | `PresentTag` | `NestedNameSupport` | Nested extension of `<logic:present>` |

### 1.2 TLD Tag Definitions

The TLD file at `taglib/src/main/resources/META-INF/tld/struts-nested.tld` defines the following tags:

| TLD Tag Name | Tag Class | Key Attributes |
|---|---|---|
| `nest` | `NestedPropertyTag` | `property` |
| `writeNesting` | `NestedWriteNestingTag` | `property`, `id`, `filter` |
| `root` | `NestedRootTag` | `name` |
| `define` | `NestedDefineTag` | `id` (required), `name`, `property`, `scope`, `toScope`, `type`, `value` |
| `message` | `NestedMessageTag` | `arg0`-`arg4`, `bundle`, `key`, `locale`, `name`, `property`, `scope` |
| `size` | `NestedSizeTag` | `collection`, `id` (required), `name`, `property`, `scope` |
| `write` | `NestedWriteTag` | `bundle`, `filter`, `format`, `formatKey`, `ignore`, `locale`, `name`, `property`, `scope` |
| `checkbox` | `NestedCheckboxTag` | `property` (required), `accesskey`, `alt`, `disabled`, `name`, `style`, `styleClass`, `styleId`, `tabindex`, `title`, `value`, + event handlers |
| `errors` | `NestedErrorsTag` | `bundle`, `footer`, `header`, `locale`, `name`, `prefix`, `property`, `suffix` |
| `file` | `NestedFileTag` | `property` (required), `accept`, `maxlength`, `name`, `size`, + standard HTML attributes |
| `form` | `NestedFormTag` | `action` (required), `enctype`, `focus`, `method`, `name`, `onsubmit`, `style`, `styleClass`, `styleId`, `target` |
| `hidden` | `NestedHiddenTag` | `property` (required), `alt`, `name`, `styleClass`, `styleId`, `value`, `write` |
| `image` | `NestedImageTag` | `property`, `src`, `srcKey`, `alt`, `border`, + standard HTML attributes |
| `img` | `NestedImgTag` | `src`, `srcKey`, `alt`, `border`, `height`, `width`, `name`, `property`, + standard HTML attributes |
| `link` | `NestedLinkTag` | `action`, `forward`, `href`, `page`, `name`, `property`, `scope`, + standard HTML attributes |
| `messages` | `NestedMessagesTag` | `id` (required), `bundle`, `locale`, `name`, `property`, `header`, `footer`, `message` |
| `multibox` | `NestedMultiboxTag` | `property` (required), `name`, `value`, + standard HTML attributes |
| `options` | `NestedOptionsTag` | `collection`, `filter`, `labelName`, `labelProperty`, `name`, `property`, `style`, `styleClass` |
| `optionsCollection` | `NestedOptionsCollectionTag` | `filter`, `label`, `name`, `property` (required), `style`, `styleClass`, `value` |
| `password` | `NestedPasswordTag` | `property` (required), `maxlength`, `name`, `redisplay`, `size`, + standard HTML attributes |
| `radio` | `NestedRadioTag` | `property` (required), `name`, `value` (required), + standard HTML attributes |
| `select` | `NestedSelectTag` | `property` (required), `multiple`, `name`, `size`, + standard HTML attributes |
| `submit` | `NestedSubmitTag` | `property`, `value`, + standard HTML attributes |
| `text` | `NestedTextTag` | `property` (required), `maxlength`, `name`, `size`, + standard HTML attributes |
| `textarea` | `NestedTextareaTag` | `property` (required), `cols`, `name`, `rows`, + standard HTML attributes |
| `empty` | `NestedEmptyTag` | `name`, `property`, `scope` |
| `notEmpty` | `NestedNotEmptyTag` | `name`, `property`, `scope` |
| `equal` | `NestedEqualTag` | `cookie`, `header`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `notEqual` | `NestedNotEqualTag` | `cookie`, `header`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `greaterEqual` | `NestedGreaterEqualTag` | `cookie`, `header`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `greaterThan` | `NestedGreaterThanTag` | `cookie`, `header`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `lessEqual` | `NestedLessEqualTag` | `cookie`, `header`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `lessThan` | `NestedLessThanTag` | `cookie`, `header`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `match` | `NestedMatchTag` | `cookie`, `header`, `location`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `notMatch` | `NestedNotMatchTag` | `cookie`, `header`, `location`, `name`, `parameter`, `property`, `scope`, `value` (required) |
| `present` | `NestedPresentTag` | `cookie`, `header`, `name`, `parameter`, `property`, `role`, `scope`, `user` |
| `notPresent` | `NestedNotPresentTag` | `cookie`, `header`, `name`, `parameter`, `property`, `role`, `scope`, `user` |
| `messagesPresent` | `NestedMessagesPresentTag` | `name`, `property`, `message` |
| `messagesNotPresent` | `NestedMessagesNotPresentTag` | `name`, `property`, `message` |
| `iterate` | `NestedIterateTag` | `collection`, `id`, `indexId`, `length`, `name`, `offset`, `property`, `scope`, `type` |

### 1.3 Core Interfaces

Located in `taglib/src/main/java/org/apache/struts/taglib/nested/`:

| Interface | Extends | Methods | Purpose |
|---|---|---|---|
| `NestedTagSupport` | *(none)* | *(marker interface -- no methods)* | Base marker interface for all nested tags |
| `NestedPropertySupport` | `NestedTagSupport` | `getProperty()`, `setProperty(String)` | Tags that have a `property` attribute to be adjusted for nesting |
| `NestedNameSupport` | `NestedPropertySupport` | `getName()`, `setName(String)` | Tags that also have a `name` attribute (bean name) |
| `NestedParentSupport` | `NestedNameSupport` | `getNestedProperty()` | Tags that act as nesting parents (e.g., `NestedPropertyTag`, `NestedIterateTag`) |

**Interface hierarchy:**
```
NestedTagSupport (marker)
  └── NestedPropertySupport (property get/set)
        └── NestedNameSupport (name get/set)
              └── NestedParentSupport (getNestedProperty)
```

### 1.4 Helper Class: `NestedPropertyHelper.java`

Located at `taglib/src/main/java/org/apache/struts/taglib/nested/NestedPropertyHelper.java`.

| Static Method | Signature | Purpose |
|---|---|---|
| `getCurrentProperty` | `(HttpServletRequest) -> String` | Retrieves the current nesting property path from the `NestedReference` stored in the request |
| `getCurrentName` | `(HttpServletRequest, NestedNameSupport) -> String` | Retrieves the current bean name from the request; falls back to searching parent `FormTag` ancestry |
| `getAdjustedProperty` | `(HttpServletRequest, String) -> String` | Combines the current nesting context with a child property using `calculateRelativeProperty()` |
| `setProperty` | `(HttpServletRequest, String) -> void` | Stores a property path into the request-scoped `NestedReference` |
| `setName` | `(HttpServletRequest, String) -> void` | Stores a bean name into the request-scoped `NestedReference` |
| `deleteReference` | `(HttpServletRequest) -> void` | Removes the `NestedReference` from the request |
| `setNestedProperties` | `(HttpServletRequest, NestedPropertySupport) -> void` | Master method: adjusts a tag's `property` (and optionally `name`) based on the current nesting context |
| `referenceInstance` | `(HttpServletRequest) -> NestedReference` | (private) Gets or creates the `NestedReference` in the request |
| `calculateRelativeProperty` | `(String property, String parent) -> String` | (private) Core algorithm for resolving relative property paths with `./`, `this/`, `../`, `/` syntax |

### 1.5 Key Nesting Tags

#### `NestedPropertyTag` (`nested:nest`)

**File:** `taglib/src/main/java/org/apache/struts/taglib/nested/NestedPropertyTag.java`

- Extends `BodyTagSupport`, implements `NestedNameSupport`
- On `doStartTag()`: saves the current nesting state, then pushes a new nesting level by calling `NestedPropertyHelper.getAdjustedProperty()` and `setProperty()`
- On `doEndTag()`: restores the original nesting state from saved values
- Enables grouping child tags under a common property prefix

#### `NestedRootTag` (`nested:root`)

**File:** `taglib/src/main/java/org/apache/struts/taglib/nested/NestedRootTag.java`

- Extends `BodyTagSupport`, implements `NestedNameSupport`
- On `doStartTag()`: saves current state, then sets the nesting property to `""` (root) and the bean name to the `name` attribute
- On `doEndTag()`: restores the previous nesting state
- Allows nesting without requiring a `<html:form>` tag

### 1.6 Action Classes

Located under `core/src/main/java/org/apache/struts/action/`:

| Class | Extends | Key Methods | Purpose |
|---|---|---|---|
| `Action` | *(none)* | `execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)`, `saveErrors()`, `saveMessages()`, `addErrors()`, `addMessages()`, `isCancelled()`, `isTokenValid()`, `generateToken()`, `saveToken()`, `resetToken()`, `getLocale()`, `setLocale()`, `getResources()` | Base class for all user-written action handlers |
| `ActionForm` | *(none)* | `reset(ActionMapping, HttpServletRequest)`, `validate(ActionMapping, HttpServletRequest)` | Abstract base for form data beans; populated from request params, validated before Action execution |
| `ActionMapping` | `ActionConfig` | `findForward(String)`, `findRequiredForward(String)`, `findForwards()`, `getInputForward()` | Runtime representation of one `<action>` element from `struts-config.xml` |
| `ActionForward` | `ForwardConfig` | constructors with `name`, `path`, `redirect`, `module` | Describes a destination (JSP path or redirect URL) returned from `Action.execute()` |
| `DynaActionForm` | `ActionForm` | dynamic property map via `get(name)` / `set(name, value)` | Form bean with properties defined in XML rather than Java code |
| `RequestProcessor` | *(none)* | `process()`, `processActionCreate()`, `processActionForm()`, `processPopulate()`, `processValidate()`, `processActionPerform()`, `processForwardConfig()`, `processException()`, `processPreprocess()`, `processRoles()`, `processLocale()`, `processContent()`, `processNoCache()`, `processMapping()`, `processMultipart()`, `processPath()`, `processForward()`, `processInclude()`, `processCachedMessages()` | Orchestrates the full request lifecycle pipeline |
| `ActionServlet` | `HttpServlet` | `init()`, `process()`, `destroy()` | Front controller; bootstraps modules, parses `struts-config.xml`, delegates to `RequestProcessor` |
| `ExceptionHandler` | *(none)* | `execute(Exception, ExceptionConfig, ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)` | Default handler for exceptions thrown by Action classes |

### 1.7 struts-config.xml Definitions

The following `struts-config.xml` files exist in the `apps/` subdirectories:

| Application | File Path |
|---|---|
| mailreader | `apps/mailreader/src/main/webapp/WEB-INF/struts-config.xml` |
| cookbook | `apps/cookbook/src/main/webapp/WEB-INF/struts-config.xml` |
| blank | `apps/blank/src/main/webapp/WEB-INF/struts-config.xml` |
| examples | `apps/examples/src/main/webapp/WEB-INF/struts-config.xml` (plus sub-modules: `validator/`, `upload/`, `dispatch/`, `exercise/`) |
| faces-example1 | `apps/faces-example1/src/main/webapp/WEB-INF/struts-config.xml` |
| faces-example2 | `apps/faces-example2/src/main/webapp/WEB-INF/struts-config.xml` |
| el-example | `apps/el-example/src/main/webapp/WEB-INF/struts-config.xml` |
| scripting-mailreader | `apps/scripting-mailreader/src/main/webapp/WEB-INF/struts-config.xml` |

**Typical `struts-config.xml` structure** (from `mailreader`):

- **`<form-beans>`**: Defines form beans using `DynaValidatorForm` with `<form-property>` elements specifying `name`, `type`, and optional `initial`/`reset` values. Supports `extends` for bean inheritance.
- **`<global-forwards>`**: Named global destinations (e.g., `Logoff` -> `/Logoff.do`, `Logon` -> `/Logon.do`, `Failure` -> `/Error.do`).
- **`<action-mappings>`**: Maps URL paths to Action classes with attributes for `type` (Action class), `name` (form bean), `scope` (request/session), `validate` (true/false), `input` (error page forward), `cancellable`, `parameter`, `forward` (direct forward). Supports `extends` and wildcards (`/*`, `/Save*`).
- **`<controller>`**: Module-level controller settings (e.g., `pagePattern`, `inputForward`).
- **`<message-resources>`**: Defines message resource bundles.
- **`<plug-in>`**: Plugin lifecycle hooks (e.g., `ValidatorPlugIn`, `MemoryDatabasePlugIn`, `DigestingPlugIn`, `ModuleConfigVerifier`).

### 1.8 Validation Configuration

**Validation XML files** exist in the following apps:

| Application | File Path |
|---|---|
| mailreader | `apps/mailreader/src/main/webapp/WEB-INF/validation.xml` |
| cookbook | `apps/cookbook/src/main/webapp/WEB-INF/validation.xml` |
| blank | `apps/blank/src/main/webapp/WEB-INF/validation.xml` |
| examples (validator) | `apps/examples/src/main/webapp/WEB-INF/validator/validation.xml` |
| examples (upload) | `apps/examples/src/main/webapp/WEB-INF/upload/validation.xml` |
| faces-example1 | `apps/faces-example1/src/main/webapp/WEB-INF/validation.xml` |
| faces-example2 | `apps/faces-example2/src/main/webapp/WEB-INF/validation.xml` |
| scripting-mailreader | `apps/scripting-mailreader/src/main/webapp/WEB-INF/validation.xml` |

**`ValidatorForm.java`** (`core/src/main/java/org/apache/struts/validator/ValidatorForm.java`):
- Extends `ActionForm`, implements `Serializable`
- Overrides `validate()` to delegate to the Apache Commons Validator framework
- Uses `Resources.initValidator()` to create a `Validator` instance configured from `validation.xml`
- Supports multi-page forms via a `page` property
- The validation key defaults to the form bean's `attribute` name from the mapping

---

## Section 2: Tag-by-Tag Migration Mapping

### 2.1 Root / Container Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:form action="/path">` | `<form th:action="@{/path}" th:object="${formBean}" method="post">` | Spring MVC binds form to `@ModelAttribute`; `th:object` establishes the binding context |
| `<nested:root name="beanName">` | `<div th:object="${beanName}">` | Apply `th:object` to any wrapper element to establish a binding root without a form |

### 2.2 Nesting / Parent Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:nest property="child">` | `<div th:object="${formBean.child}">` or use `*{child.subProp}` | Thymeleaf has no implicit nesting stack; use `th:object` on a wrapper element, or use fully-qualified selection expressions |
| `<nested:iterate property="items" id="item">` | `<div th:each="item : *{items}">` or `<div th:each="item : ${formBean.items}">` | `th:each` iterates over collections; within the loop, reference `item.property` directly |

### 2.3 Logic / Conditional Tags

| Struts Nested Tag | Thymeleaf Equivalent | Example |
|---|---|---|
| `<nested:empty property="prop">` | `<div th:if="${#lists.isEmpty(obj.prop)}">` or `<div th:if="*{prop} == null">` | Check for null or empty collection |
| `<nested:notEmpty property="prop">` | `<div th:if="${!#lists.isEmpty(obj.prop)}">` or `<div th:if="*{prop} != null">` | Inverse of empty |
| `<nested:equal property="prop" value="X">` | `<div th:if="*{prop} == 'X'">` | SpEL equality check |
| `<nested:notEqual property="prop" value="X">` | `<div th:if="*{prop} != 'X'">` | SpEL inequality check |
| `<nested:greaterEqual property="prop" value="5">` | `<div th:if="*{prop} >= 5">` | SpEL numeric comparison |
| `<nested:greaterThan property="prop" value="5">` | `<div th:if="*{prop} > 5">` | SpEL numeric comparison |
| `<nested:lessEqual property="prop" value="5">` | `<div th:if="*{prop} <= 5">` | SpEL numeric comparison |
| `<nested:lessThan property="prop" value="5">` | `<div th:if="*{prop} < 5">` | SpEL numeric comparison |
| `<nested:match property="prop" value="substr">` | `<div th:if="${#strings.contains(*{prop}, 'substr')}">` | String contains check |
| `<nested:notMatch property="prop" value="substr">` | `<div th:if="${!#strings.contains(*{prop}, 'substr')}">` | Inverse of match |
| `<nested:present property="prop">` | `<div th:if="*{prop} != null">` | Null check |
| `<nested:notPresent property="prop">` | `<div th:if="*{prop} == null">` | Null check inverse |
| `<nested:messagesPresent>` | `<div th:if="${#fields.hasErrors('*')}">` | Check for validation errors |
| `<nested:messagesNotPresent>` | `<div th:if="${!#fields.hasErrors('*')}">` | Check for no validation errors |

### 2.4 Form Input Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:text property="name"/>` | `<input type="text" th:field="*{name}"/>` | `th:field` generates `id`, `name`, and `value` attributes |
| `<nested:hidden property="id"/>` | `<input type="hidden" th:field="*{id}"/>` | Same pattern as text |
| `<nested:checkbox property="active"/>` | `<input type="checkbox" th:field="*{active}"/>` | Spring handles boolean binding |
| `<nested:password property="pass"/>` | `<input type="password" th:field="*{pass}"/>` | Same pattern as text |
| `<nested:radio property="type" value="A"/>` | `<input type="radio" th:field="*{type}" value="A"/>` | `th:field` handles checked state |
| `<nested:textarea property="desc"/>` | `<textarea th:field="*{desc}"></textarea>` | `th:field` sets name and value |
| `<nested:multibox property="selected" value="opt1"/>` | `<input type="checkbox" th:field="*{selected}" value="opt1"/>` | Multi-value checkbox; Spring binds to array/List |
| `<nested:file property="upload"/>` | `<input type="file" th:field="*{upload}"/>` | Use `MultipartFile` in the form bean |
| `<nested:submit value="Save"/>` | `<button type="submit">Save</button>` or `<input type="submit" value="Save"/>` | No special Thymeleaf attribute needed |
| `<nested:image property="img" src="/icon.gif"/>` | `<input type="image" th:src="@{/icon.gif}"/>` | Use `th:src` for URL resolution |

### 2.5 Select / Options Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:select property="country">` | `<select th:field="*{country}">` | Wraps `<option>` elements |
| `<nested:options property="countryList" labelProperty="name"/>` | `<option th:each="c : *{countryList}" th:value="${c.value}" th:text="${c.name}">` | Iterate and render options explicitly |
| `<nested:optionsCollection property="items" label="label" value="value"/>` | `<option th:each="item : *{items}" th:value="${item.value}" th:text="${item.label}">` | Same pattern as above |

### 2.6 Output / Display Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:write property="name"/>` | `<span th:text="*{name}"></span>` | Output text; use `th:utext` for unescaped HTML |
| `<nested:write property="amount" format="#,##0.00"/>` | `<span th:text="${#numbers.formatDecimal(obj.amount, 1, 2)}"></span>` | Use Thymeleaf number utilities |
| `<nested:define id="var" property="prop"/>` | `<th:block th:with="var=*{prop}">` | Define a local variable |
| `<nested:message key="msg.key"/>` | `<span th:text="#{msg.key}"></span>` | Message i18n lookup |
| `<nested:message key="msg.key" arg0="${val}"/>` | `<span th:text="#{msg.key(${val})}"></span>` | Parameterized message |
| `<nested:size id="count" property="list"/>` | `<th:block th:with="count=${#lists.size(obj.list)}">` | Collection size |
| `<nested:writeNesting/>` | *(no equivalent)* | Diagnostic; not needed in Thymeleaf |

### 2.7 Navigation / Link Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:link action="/edit" property="id" paramId="id" paramProperty="id">` | `<a th:href="@{/edit(id=*{id})}">` | URL expression with parameters |
| `<nested:link forward="success">` | `<a th:href="@{/target-path}">` | Replace forward names with explicit URLs |
| `<nested:img src="/images/logo.png"/>` | `<img th:src="@{/images/logo.png}"/>` | URL resolution |

### 2.8 Error / Message Tags

| Struts Nested Tag | Thymeleaf Equivalent | Notes |
|---|---|---|
| `<nested:errors property="name"/>` | `<ul th:if="${#fields.hasErrors('name')}"><li th:each="err : ${#fields.errors('name')}" th:text="${err}"></li></ul>` | Field-specific errors |
| `<nested:errors/>` | `<ul th:if="${#fields.hasAnyErrors()}"><li th:each="err : ${#fields.allErrors()}" th:text="${err}"></li></ul>` | All errors |
| `<nested:messages id="msg" property="prop">` | `<div th:if="${#fields.hasErrors('prop')}"><p th:each="err : ${#fields.errors('prop')}" th:text="${err}"></p></div>` | Iterate over messages for a property |

---

## Section 3: Relative Property Navigation Migration

### 3.1 How `calculateRelativeProperty()` Works

The core of the nested tag library's property resolution is the `calculateRelativeProperty()` method in `NestedPropertyHelper.java` (lines 232-300). This method takes two parameters:

- `property` -- the relative property expression provided by the child tag
- `parent` -- the current dot-notated nesting context (e.g., `"order.customer"`)

It supports four navigation patterns:

#### Pattern 1: `./` or `this/` -- Reference Parent's Property

```java
if ("./".equals(property) || "this/".equals(property)) {
    return parent;
}
```

Returns the parent property as-is. Used to reference the current nesting level directly, typically for indexed properties.

**Example:**
- Parent context: `order.items[0]`
- Property: `./`
- Result: `order.items[0]`

#### Pattern 2: `/` prefix -- Absolute (Return to Root)

```java
if (stepping.startsWith("/")) {
    return property;  // return from root
}
```

When the stepping portion starts with `/`, the method ignores the parent context entirely and returns just the property name. This provides an absolute path from the root bean.

**Example:**
- Parent context: `order.customer.address`
- Property: `/globalFlag`
- Result: `globalFlag`

#### Pattern 3: `../` -- Navigate Up One Level

The method tokenizes the parent by `.` and the stepping by `/`. Each `../` removes one level from the parent context.

**Example:**
- Parent context: `order.customer.address` (3 tokens: order, customer, address)
- Property: `../name` (1 step up, property = `name`)
- Result: `order.customer.name` (removed 1 token, appended `name`)

**Example with multiple levels:**
- Parent context: `order.customer.address` (3 tokens)
- Property: `../../orderDate` (2 steps up, property = `orderDate`)
- Result: `order.orderDate` (removed 2 tokens, appended `orderDate`)

#### Pattern 4: `../` exceeding depth -- Falls Back to Root

When the number of `../` steps equals or exceeds the number of parent tokens, the method returns the property from root.

**Example:**
- Parent context: `order.customer` (2 tokens)
- Property: `../../globalProp` (2 steps up >= 2 tokens)
- Result: `globalProp` (returns from root)

### 3.2 Thymeleaf Does Not Have a Direct Equivalent

Thymeleaf has **no implicit nesting stack** and **no relative property navigation syntax**. All property paths must be either:

1. **Selection expressions** (`*{prop}`) relative to the nearest `th:object`
2. **Variable expressions** (`${bean.full.path}`) with explicit full paths

### 3.3 Migration Examples: Before and After

#### Example A: `./` (this) reference

**Struts (JSP):**
```jsp
<nested:form action="/updateOrder">
  <nested:iterate property="items">
    <nested:write property="./"/>  <!-- writes the item itself -->
    <nested:text property="quantity"/>
  </nested:iterate>
</nested:form>
```

**Thymeleaf (HTML):**
```html
<form th:action="@{/updateOrder}" th:object="${orderForm}" method="post">
  <div th:each="item, iterStat : *{items}">
    <span th:text="${item}"></span>
    <input type="text" th:field="*{items[__${iterStat.index}__].quantity}"/>
  </div>
</form>
```

#### Example B: `/` (absolute root) reference

**Struts (JSP):**
```jsp
<nested:form action="/editCustomer">
  <nested:nest property="address">
    <nested:text property="street"/>
    <nested:text property="city"/>
    <nested:write property="/customerName"/>  <!-- jumps to root -->
  </nested:nest>
</nested:form>
```

**Thymeleaf (HTML):**
```html
<form th:action="@{/editCustomer}" th:object="${customerForm}" method="post">
  <div>
    <input type="text" th:field="*{address.street}"/>
    <input type="text" th:field="*{address.city}"/>
    <span th:text="*{customerName}"></span>  <!-- explicit full path from root -->
  </div>
</form>
```

#### Example C: `../` (navigate up) reference

**Struts (JSP):**
```jsp
<nested:form action="/editOrder">
  <nested:nest property="customer">
    <nested:nest property="address">
      <nested:text property="street"/>
      <nested:write property="../name"/>  <!-- up one level to customer.name -->
    </nested:nest>
  </nested:nest>
</nested:form>
```

**Thymeleaf (HTML):**
```html
<form th:action="@{/editOrder}" th:object="${orderForm}" method="post">
  <div>
    <input type="text" th:field="*{customer.address.street}"/>
    <span th:text="*{customer.name}"></span>  <!-- explicit full path -->
  </div>
</form>
```

#### Example D: `../../` (navigate up two levels) reference

**Struts (JSP):**
```jsp
<nested:form action="/editOrder">
  <nested:nest property="customer">
    <nested:nest property="address">
      <nested:text property="city"/>
      <nested:write property="../../orderDate"/>  <!-- up two levels to root -->
    </nested:nest>
  </nested:nest>
</nested:form>
```

**Thymeleaf (HTML):**
```html
<form th:action="@{/editOrder}" th:object="${orderForm}" method="post">
  <div>
    <input type="text" th:field="*{customer.address.city}"/>
    <span th:text="*{orderDate}"></span>  <!-- already at root level -->
  </div>
</form>
```

### 3.4 Conversion Strategy

1. **Flatten all nesting**: Remove all `<nested:nest>` wrappers and convert child property references to their fully-qualified dot-notated paths.
2. **Resolve `./` and `this/`**: Replace with the full parent context path.
3. **Resolve `../`**: Count the steps and compute the resulting prefix manually, then use the explicit path.
4. **Resolve `/`**: Strip the leading `/` and use the property as a root-level reference via `*{property}`.
5. **For `<nested:iterate>`**: Convert to `th:each` with explicit indexed field binding using `*{collection[__${iterStat.index}__].field}`.

---

## Section 4: Step-by-Step Migration Procedure

### Phase 1: Project Setup

**Goal:** Create a new Spring Boot project structure.

1. **Create a new module** (or standalone project) with the following dependencies in `pom.xml` (or `build.gradle`):

   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.2.x</version>
   </parent>

   <dependencies>
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
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-test</artifactId>
           <scope>test</scope>
       </dependency>
   </dependencies>
   ```

2. **Configure `application.properties`:**

   ```properties
   # Thymeleaf view resolver
   spring.thymeleaf.prefix=classpath:/templates/
   spring.thymeleaf.suffix=.html
   spring.thymeleaf.mode=HTML
   spring.thymeleaf.encoding=UTF-8
   spring.thymeleaf.cache=false

   # Server settings
   server.port=8080
   server.servlet.context-path=/

   # Message sources (replaces Struts MessageResources)
   spring.messages.basename=messages
   spring.messages.encoding=UTF-8
   ```

3. **Create the directory structure:**
   ```
   src/
     main/
       java/
         com/example/app/
           Application.java          # @SpringBootApplication
           config/                    # @Configuration classes
           controller/                # @Controller classes
           model/                     # Form POJOs (replacing ActionForm)
           service/                   # Business logic (from Action classes)
       resources/
         templates/                   # Thymeleaf .html templates
         static/                      # CSS, JS, images
         messages.properties          # i18n messages
         application.properties
   ```

### Phase 2: Form Beans to POJOs

**Goal:** Convert every `ActionForm` / `DynaActionForm` / `DynaValidatorForm` to a plain POJO with JSR 380 (Bean Validation) annotations.

#### 2.1 Conversion Rules

For each `<form-bean>` in `struts-config.xml`:

| Struts Pattern | Spring Boot Equivalent |
|---|---|
| `<form-bean name="MyForm" type="com.example.MyForm">` (extends `ActionForm`) | Create a POJO class `MyForm` with fields, getters, setters |
| `<form-bean type="org.apache.struts.validator.DynaValidatorForm">` with `<form-property>` elements | Create a concrete POJO with a field for each `<form-property>` |
| `<form-bean extends="BaseForm">` | Use Java class inheritance or composition |
| `ActionForm.reset(ActionMapping, HttpServletRequest)` | Remove entirely; Spring reinitializes form beans per request by default |
| `ActionForm.validate(ActionMapping, HttpServletRequest)` | Replace with JSR 380 annotations on fields |

#### 2.2 Validation Mapping

| Struts Validator Type (`validation.xml`) | JSR 380 Annotation | Example |
|---|---|---|
| `required` | `@NotBlank` (String) / `@NotNull` (Object) | `@NotBlank(message = "{error.required}")` |
| `minlength` | `@Size(min = N)` | `@Size(min = 3)` |
| `maxlength` | `@Size(max = N)` | `@Size(max = 50)` |
| `mask` (regex) | `@Pattern(regexp = "...")` | `@Pattern(regexp = "^[a-zA-Z]+$")` |
| `byte`, `short`, `integer`, `long`, `float`, `double` | Use the appropriate Java type | Field type enforces this |
| `date` | `@DateTimeFormat(pattern = "...")` | `@DateTimeFormat(pattern = "MM/dd/yyyy")` |
| `email` | `@Email` | `@Email(message = "{error.email}")` |
| `url` | `@URL` (Hibernate Validator) | `@URL` |
| `range` | `@Min` + `@Max` or `@Range` (Hibernate Validator) | `@Min(1) @Max(100)` |
| `intRange` | `@Min` + `@Max` | `@Min(1) @Max(100)` |
| `floatRange` | `@DecimalMin` + `@DecimalMax` | `@DecimalMin("0.0") @DecimalMax("99.9")` |
| Nested object validation | `@Valid` | `@Valid private Address address;` |

#### 2.3 Example Conversion

**Struts `struts-config.xml`:**
```xml
<form-bean name="RegistrationForm"
           type="org.apache.struts.validator.DynaValidatorForm">
    <form-property name="username" type="java.lang.String"/>
    <form-property name="password" type="java.lang.String"/>
    <form-property name="fullName" type="java.lang.String"/>
    <form-property name="fromAddress" type="java.lang.String"/>
</form-bean>
```

**Struts `validation.xml`:**
```xml
<form name="RegistrationForm">
    <field property="username" depends="required,minlength,maxlength">
        <arg key="prompt.username" position="0"/>
        <var><var-name>minlength</var-name><var-value>3</var-value></var>
        <var><var-name>maxlength</var-name><var-value>16</var-value></var>
    </field>
    <field property="password" depends="required,minlength,maxlength">
        <arg key="prompt.password" position="0"/>
        <var><var-name>minlength</var-name><var-value>3</var-value></var>
        <var><var-name>maxlength</var-name><var-value>16</var-value></var>
    </field>
    <field property="fromAddress" depends="required,email"/>
</form>
```

**Spring Boot POJO:**
```java
public class RegistrationForm {

    @NotBlank(message = "{error.username.required}")
    @Size(min = 3, max = 16, message = "{error.username.size}")
    private String username;

    @NotBlank(message = "{error.password.required}")
    @Size(min = 3, max = 16, message = "{error.password.size}")
    private String password;

    private String fullName;

    @NotBlank(message = "{error.fromAddress.required}")
    @Email(message = "{error.fromAddress.email}")
    private String fromAddress;

    // Getters and setters...
}
```

### Phase 3: Actions to Controllers

**Goal:** Convert each `<action>` mapping to a `@Controller` method.

#### 3.1 Conversion Rules

| Struts Pattern | Spring Boot Equivalent |
|---|---|
| `<action path="/SubmitLogon" type="...LogonAction" name="LogonForm" scope="request" validate="true" input="Logon">` | `@PostMapping("/SubmitLogon") public String submitLogon(@Valid @ModelAttribute("LogonForm") LogonForm form, BindingResult result)` |
| `Action.execute(mapping, form, request, response)` returns `ActionForward` | Controller method returns `String` (view name) or `"redirect:/path"` |
| `mapping.findForward("Success")` | Return `"viewName"` (the path from the forward definition) |
| `mapping.findForward("Failure")` returning a redirect forward | Return `"redirect:/path"` |
| `ActionForm form` parameter | `@ModelAttribute FormPojo form` parameter |
| `ActionMapping mapping` parameter | `@RequestMapping` annotation attributes |
| `request.getAttribute(...)` / `request.setAttribute(...)` | `Model.addAttribute(...)` / `@ModelAttribute` |
| `request.getParameter(...)` | `@RequestParam` annotation |
| `request.getSession().getAttribute(...)` | `@SessionAttributes` annotation on controller or `HttpSession` parameter |
| `saveErrors(request, errors)` | `BindingResult` (auto-populated by `@Valid`) or `RedirectAttributes.addFlashAttribute()` |
| `saveMessages(request, messages)` | `RedirectAttributes.addFlashAttribute("messages", ...)` |
| `isCancelled(request)` | Check for a cancel parameter: `@RequestParam(required=false) String cancel` |

#### 3.2 Example Conversion

**Struts Action:**
```java
public class LogonAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        DynaActionForm logonForm = (DynaActionForm) form;
        String username = (String) logonForm.get("username");
        String password = (String) logonForm.get("password");

        // Business logic...
        if (authenticated) {
            return mapping.findForward("Success");  // -> /MainMenu.do
        } else {
            ActionErrors errors = new ActionErrors();
            errors.add("password", new ActionMessage("error.password.mismatch"));
            saveErrors(request, errors);
            return mapping.findForward("Logon");  // -> /Logon.jsp
        }
    }
}
```

**Spring Boot Controller:**
```java
@Controller
public class LogonController {

    @GetMapping("/Logon")
    public String showLogon(Model model) {
        model.addAttribute("logonForm", new LogonForm());
        return "Logon";  // -> templates/Logon.html
    }

    @PostMapping("/SubmitLogon")
    public String submitLogon(@Valid @ModelAttribute("logonForm") LogonForm form,
                              BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return "Logon";
        }

        // Business logic...
        if (authenticated) {
            session.setAttribute("user", user);
            return "redirect:/MainMenu";
        } else {
            result.rejectValue("password", "error.password.mismatch");
            return "Logon";
        }
    }
}
```

### Phase 4: JSP to Thymeleaf Templates

**Goal:** Convert each JSP file to a Thymeleaf `.html` template.

#### 4.1 Conversion Steps

1. **Create `.html` file** in `src/main/resources/templates/` for each `.jsp` file.

2. **Add Thymeleaf namespace** to the root element:
   ```html
   <html xmlns:th="http://www.thymeleaf.org">
   ```

3. **Convert tag library declarations:**
   - Remove `<%@ taglib prefix="nested" uri="..." %>` and all other Struts taglib declarations
   - Remove `<%@ page ... %>` directives

4. **Convert all `nested:*` tags** using the mapping table in Section 2.

5. **Convert other Struts tags:**

   | Struts Tag | Thymeleaf Equivalent |
   |---|---|
   | `<html:form action="/path">` | `<form th:action="@{/path}" th:object="${form}" method="post">` |
   | `<html:text property="name"/>` | `<input type="text" th:field="*{name}"/>` |
   | `<html:errors/>` | `<div th:if="${#fields.hasAnyErrors()}"><p th:each="e : ${#fields.allErrors()}" th:text="${e}"/></div>` |
   | `<html:link action="/path">` | `<a th:href="@{/path}">` |
   | `<html:submit/>` | `<button type="submit">` |
   | `<bean:write name="bean" property="prop"/>` | `<span th:text="${bean.prop}"/>` |
   | `<bean:message key="key"/>` | `<span th:text="#{key}"/>` |
   | `<logic:iterate name="bean" property="list" id="item">` | `<div th:each="item : ${bean.list}">` |
   | `<logic:present name="bean">` | `<div th:if="${bean != null}">` |
   | `<logic:equal name="bean" property="prop" value="X">` | `<div th:if="${bean.prop == 'X'}">` |

6. **Convert JSTL tags:**

   | JSTL Tag | Thymeleaf Equivalent |
   |---|---|
   | `<c:if test="${condition}">` | `<div th:if="${condition}">` |
   | `<c:choose>/<c:when>/<c:otherwise>` | `th:switch` / `th:case` |
   | `<c:forEach items="${list}" var="item">` | `<div th:each="item : ${list}">` |
   | `<c:out value="${expr}"/>` | `<span th:text="${expr}"/>` |
   | `<c:set var="x" value="${expr}"/>` | `<th:block th:with="x=${expr}">` |
   | `<fmt:message key="key"/>` | `<span th:text="#{key}"/>` |
   | `<fmt:formatDate value="${date}" pattern="..."/>` | `<span th:text="${#dates.format(date, '...')}"/>` |

#### 4.2 Example Conversion

**Struts JSP:**
```jsp
<%@ taglib prefix="nested" uri="http://struts.apache.org/tags-nested" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<nested:form action="/saveOrder">
    <nested:text property="orderNumber"/>
    <nested:nest property="customer">
        <nested:text property="name"/>
        <nested:nest property="address">
            <nested:text property="street"/>
            <nested:text property="city"/>
        </nested:nest>
    </nested:nest>
    <nested:iterate property="items" id="item">
        <nested:text property="productName"/>
        <nested:text property="quantity"/>
    </nested:iterate>
    <nested:submit value="Save"/>
</nested:form>
```

**Thymeleaf HTML:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<form th:action="@{/saveOrder}" th:object="${orderForm}" method="post">
    <input type="text" th:field="*{orderNumber}"/>

    <input type="text" th:field="*{customer.name}"/>
    <input type="text" th:field="*{customer.address.street}"/>
    <input type="text" th:field="*{customer.address.city}"/>

    <div th:each="item, iterStat : *{items}">
        <input type="text" th:field="*{items[__${iterStat.index}__].productName}"/>
        <input type="text" th:field="*{items[__${iterStat.index}__].quantity}"/>
    </div>

    <button type="submit">Save</button>
</form>
</body>
</html>
```

### Phase 5: Configuration Migration

**Goal:** Replace all Struts XML configuration with Spring equivalents.

#### 5.1 Global Forwards to View Controllers

**Struts:**
```xml
<global-forwards>
    <forward name="Logoff" path="/Logoff.do"/>
    <forward name="Logon" path="/Logon.do"/>
    <forward name="Failure" path="/Error.do"/>
</global-forwards>
```

**Spring Boot:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Simple forwards that don't need controller logic
        registry.addViewController("/").setViewName("Welcome");
        registry.addViewController("/Error").setViewName("error");
    }
}
```

For forwards that route to action URLs (e.g., `/Logoff.do`), create appropriate `@Controller` methods instead.

#### 5.2 Global Exceptions to @ControllerAdvice

**Struts:**
```xml
<global-exceptions>
    <exception key="expired.password"
               type="com.example.ExpiredPasswordException"
               path="/ChangePassword.do"/>
</global-exceptions>
```

**Spring Boot:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpiredPasswordException.class)
    public String handleExpiredPassword(ExpiredPasswordException ex,
                                         RedirectAttributes attrs) {
        attrs.addFlashAttribute("error", ex.getMessage());
        return "redirect:/ChangePassword";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }
}
```

#### 5.3 Struts Plugins to Spring Configuration

| Struts Plugin | Spring Boot Equivalent |
|---|---|
| `ValidatorPlugIn` | Auto-configured by `spring-boot-starter-validation`; no explicit setup needed |
| `ModuleConfigVerifier` | Not needed; Spring validates configuration at startup |
| Custom PlugIn with `<set-property>` | `@Configuration` class with `@Bean` methods and `@Value` / `@ConfigurationProperties` |
| PlugIn `init()`/`destroy()` | `@PostConstruct` / `@PreDestroy` on `@Configuration` beans |
| `DigestingPlugIn` (XML data loading) | `@Bean` method that parses XML and returns the data object |

**Example:**
```java
@Configuration
public class DatabaseConfig {

    @Value("${database.pathname:/WEB-INF/database.xml}")
    private String pathname;

    @Bean
    @PostConstruct
    public void initDatabase() {
        // Load and initialize database from pathname
    }
}
```

#### 5.4 RequestProcessor Pipeline to Spring Interceptors

The Struts `RequestProcessor.process()` method executes these steps in order:

| RequestProcessor Step | Spring Boot Equivalent |
|---|---|
| `processMultipart()` | Auto-handled by Spring's `MultipartResolver` (configured via `spring.servlet.multipart.*`) |
| `processPath()` | Handled by `DispatcherServlet` URL mapping |
| `processLocale()` | `LocaleResolver` bean + `LocaleChangeInterceptor` |
| `processContent()` | `spring.mvc.contentnegotiation.*` properties or `ContentNegotiationConfigurer` |
| `processNoCache()` | `WebContentInterceptor` with cache settings or `@CacheControl` annotations |
| `processPreprocess()` | Custom `HandlerInterceptor.preHandle()` |
| `processCachedMessages()` | Flash attributes (auto-managed by Spring MVC) |
| `processMapping()` | `@RequestMapping` annotations (auto-resolved by `DispatcherServlet`) |
| `processRoles()` | Spring Security's `@PreAuthorize`, `@Secured`, or `HttpSecurity` configuration |
| `processActionForm()` | `@ModelAttribute` parameter binding |
| `processPopulate()` | Automatic via Spring MVC data binding (`WebDataBinder`) |
| `processValidate()` | `@Valid` annotation + `BindingResult` parameter |
| `processForward()` / `processInclude()` | `ViewResolver` chain or `forward:` / `redirect:` prefixes |
| `processActionCreate()` | Spring manages controller instances via `@Controller` (singleton by default) |
| `processActionPerform()` | Invocation of `@RequestMapping` handler method |
| `processException()` | `@ExceptionHandler` methods or `HandlerExceptionResolver` beans |
| `processForwardConfig()` | Return value from controller method interpreted by `ViewResolver` |

**Example `HandlerInterceptor` (replacing `processPreprocess()`):**
```java
@Component
public class PreprocessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // Custom preprocessing logic
        return true;  // continue processing
    }
}

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private PreprocessInterceptor preprocessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(preprocessInterceptor);
    }
}
```

#### 5.5 Message Resources

**Struts:**
```xml
<message-resources parameter="org.apache.struts.apps.mailreader.resources.ApplicationResources"/>
<message-resources parameter="org.apache.struts.apps.mailreader.resources.AlternateApplicationResources"
                   key="alternate"/>
```

**Spring Boot:**
```properties
# application.properties
spring.messages.basename=messages,messages-alternate
spring.messages.encoding=UTF-8
```

Place `messages.properties` (and locale variants like `messages_fr.properties`) in `src/main/resources/`.

### Phase 6: Testing and Validation

**Goal:** Verify each migrated component matches original Struts behavior.

#### 6.1 Integration Testing with MockMvc

```java
@SpringBootTest
@AutoConfigureMockMvc
public class LogonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testShowLogonForm() throws Exception {
        mockMvc.perform(get("/Logon"))
               .andExpect(status().isOk())
               .andExpect(view().name("Logon"))
               .andExpect(model().attributeExists("logonForm"));
    }

    @Test
    public void testSubmitLogonValidationFailure() throws Exception {
        mockMvc.perform(post("/SubmitLogon")
                   .param("username", "")
                   .param("password", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("Logon"))
               .andExpect(model().hasErrors());
    }

    @Test
    public void testSubmitLogonSuccess() throws Exception {
        mockMvc.perform(post("/SubmitLogon")
                   .param("username", "testuser")
                   .param("password", "testpass"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/MainMenu"));
    }
}
```

#### 6.2 Nested Object Binding Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class NestedFormBindingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testNestedObjectBinding() throws Exception {
        mockMvc.perform(post("/saveOrder")
                   .param("orderNumber", "ORD-001")
                   .param("customer.name", "John Doe")
                   .param("customer.address.street", "123 Main St")
                   .param("customer.address.city", "Springfield")
                   .param("items[0].productName", "Widget")
                   .param("items[0].quantity", "5"))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testNestedValidation() throws Exception {
        mockMvc.perform(post("/saveOrder")
                   .param("orderNumber", "")        // required field empty
                   .param("customer.name", ""))       // nested required field empty
               .andExpect(status().isOk())
               .andExpect(model().attributeHasFieldErrors("orderForm",
                   "orderNumber", "customer.name"));
    }
}
```

#### 6.3 Validation Verification Checklist

For each migrated form:

- [ ] All `required` validations trigger `@NotBlank`/`@NotNull` errors
- [ ] `minlength`/`maxlength` constraints match `@Size` parameters
- [ ] Regex/mask patterns match `@Pattern` expressions
- [ ] Nested object validation fires with `@Valid` annotation
- [ ] Error messages display correctly in Thymeleaf templates
- [ ] Form resubmission preserves previously entered values
- [ ] Redirect-after-POST pattern prevents double submission

---

## Section 5: Risk Areas and Edge Cases

### 5.1 DynaActionForm Has No Direct Spring Equivalent

**Problem:** `DynaActionForm` and `DynaValidatorForm` define form properties dynamically in `struts-config.xml` rather than in Java code. Spring MVC has no equivalent mechanism for dynamic form binding.

**Resolution:**
- **Preferred:** Convert each `DynaActionForm` to a concrete POJO class with typed fields. This is the recommended approach for type safety and IDE support.
- **Alternative:** Use `Map<String, Object>`-based binding for forms that genuinely need dynamic properties:
  ```java
  public class DynamicForm {
      private Map<String, Object> properties = new HashMap<>();
      // getters/setters for the map
  }
  ```
  In Thymeleaf: `th:field="*{properties['fieldName']}"`. Note that this loses type safety and validation support.

**Affected files in this codebase:**
- `apps/mailreader/` uses `DynaValidatorForm` for `BaseForm`, `LogonForm`, `RegistrationForm`, `SubscriptionForm`
- All dynamic form-property definitions must be converted to concrete Java fields

### 5.2 RequestProcessor Pipeline Decomposition

**Problem:** The Struts `RequestProcessor` is a monolithic class with 20+ `process*` methods that form a sequential pipeline. There is no single Spring equivalent.

**RequestProcessor methods requiring careful decomposition:**

| Method | Spring Equivalent | Risk |
|---|---|---|
| `processPreprocess()` | `HandlerInterceptor.preHandle()` | Custom preprocessing logic must be carefully ported |
| `processRoles()` | Spring Security | Requires Spring Security dependency and configuration |
| `processPopulate()` | Automatic data binding | Custom `BeanUtils` population logic may differ |
| `processValidate()` | `@Valid` + `BindingResult` | Multi-page form validation (`page` attribute) requires manual handling |
| `processException()` | `@ExceptionHandler` | Exception-to-forward mapping must be converted to exception-to-view mapping |
| `processActionPerform()` | Controller method invocation | Thread-safety model differs (Struts Actions are singletons; Spring controllers are also singletons but use method-scoped parameters) |
| `processForwardConfig()` | View resolution | Module-relative vs. context-relative path logic must be replicated if multi-module |

**Key risk:** Applications with **custom `RequestProcessor` subclasses** will require the most careful analysis, as their overridden methods must be mapped to the correct Spring extension points.

### 5.3 Relative Property Navigation Requires Manual Refactoring

**Problem:** The `../`, `/`, `./`, and `this/` property navigation syntax in `NestedPropertyHelper.calculateRelativeProperty()` has **no automated conversion path** to Thymeleaf.

**Impact:**
- Every JSP that uses relative property navigation must be manually analyzed
- The developer must trace the nesting context to determine the fully-qualified property path
- Deeply nested pages with many `../` references are especially error-prone

**Mitigation:**
1. Create a helper script that parses JSP files and identifies all `nested:*` tags with `../`, `/`, `./`, or `this/` in their `property` attributes
2. For each occurrence, document the enclosing `nested:nest` / `nested:iterate` hierarchy and compute the resolved absolute path
3. Replace with explicit paths in the Thymeleaf template
4. Write integration tests that submit forms with nested data and verify binding works correctly

### 5.4 Tiles Layouts Should Migrate to Thymeleaf Layout Dialect

**Problem:** Applications using Apache Tiles for page layout composition need a Thymeleaf equivalent.

**Resolution:**
- Add the **Thymeleaf Layout Dialect** dependency:
  ```xml
  <dependency>
      <groupId>nz.net.ultraq.thymeleaf</groupId>
      <artifactId>thymeleaf-layout-dialect</artifactId>
  </dependency>
  ```
- Convert Tiles definitions to Thymeleaf layout templates:

  | Tiles Concept | Thymeleaf Layout Dialect |
  |---|---|
  | `tiles-defs.xml` definitions | Layout template files with `layout:decorate` |
  | `<tiles:insertAttribute name="header"/>` | `<div layout:fragment="header">` |
  | `<tiles:put name="body" value="/content.jsp"/>` | `<div layout:fragment="body">` in content template |
  | `<tiles:insertDefinition name="default"/>` | `<html layout:decorate="~{layouts/default}">` |

### 5.5 Multi-Module Struts Applications

**Problem:** Struts 1 supports multiple modules, each with its own `struts-config.xml`, `RequestProcessor`, and namespace. Spring Boot does not have a direct equivalent.

**Resolution:**
- If modules are independent, consider separate Spring Boot applications behind a gateway
- If modules share a deployment, use `@RequestMapping` path prefixes on controllers to replicate module namespaces
- Module-specific message resources become separate `MessageSource` beans or bundled into a single `messages.properties` with prefixed keys

### 5.6 ActionServlet Initialization and PlugIn Lifecycle

**Problem:** Struts `PlugIn` classes have `init(ActionServlet, ModuleConfig)` and `destroy()` lifecycle methods that run during servlet initialization.

**Resolution:**
- Convert each `PlugIn` to a Spring `@Configuration` class or `@Component`
- Use `@PostConstruct` for `init()` logic and `@PreDestroy` for `destroy()` logic
- If the plugin needs access to the `ServletContext`, inject it via `@Autowired`
- If the plugin modifies configuration at startup, use `@Bean` methods to produce the configured objects

### 5.7 Client-Side Validation (JavascriptValidatorTag)

**Problem:** Struts provides `<html:javascript>` / `JavascriptValidatorTag` to emit client-side validation scripts. There is no Thymeleaf equivalent.

**Resolution:**
- Use HTML5 form validation attributes (`required`, `pattern`, `minlength`, `maxlength`, `min`, `max`) generated alongside `th:field`
- Or use a JavaScript validation library (e.g., jQuery Validation, Parsley.js, or a custom script) to replicate the behavior
- Server-side validation via `@Valid` remains the authoritative check

### 5.8 Token-Based Double-Submit Protection

**Problem:** Struts provides `Action.generateToken()`, `isTokenValid()`, `saveToken()`, and `resetToken()` for preventing duplicate form submissions.

**Resolution:**
- Spring Security provides CSRF protection by default (`CsrfFilter`), which covers the most common case
- For additional double-submit protection, use the **Post-Redirect-Get** (PRG) pattern consistently
- If exact token semantics are needed, implement a custom `HandlerInterceptor` that generates and validates tokens

### 5.9 Multipart File Upload

**Problem:** Struts uses `MultipartRequestHandler` and `MultipartRequestWrapper` for file uploads.

**Resolution:**
- Spring Boot auto-configures multipart support via `spring.servlet.multipart.*` properties
- Use `MultipartFile` as a field type in form POJOs or as a `@RequestParam` in controller methods
- Configure limits in `application.properties`:
  ```properties
  spring.servlet.multipart.max-file-size=10MB
  spring.servlet.multipart.max-request-size=25MB
  ```

---

## Appendix: Quick Reference Cheat Sheet

| Struts 1 Concept | Spring Boot Equivalent |
|---|---|
| `struts-config.xml` | `@Configuration` classes + annotations |
| `ActionServlet` | `DispatcherServlet` (auto-configured) |
| `RequestProcessor` | `DispatcherServlet` + `HandlerInterceptor` + `HandlerExceptionResolver` |
| `Action` | `@Controller` class |
| `Action.execute()` | `@RequestMapping` method |
| `ActionForm` | POJO with `@ModelAttribute` |
| `DynaActionForm` | Concrete POJO |
| `ActionMapping` | `@RequestMapping` annotation |
| `ActionForward` | Return `String` (view name or `"redirect:..."`) |
| `ActionErrors` / `ActionMessages` | `BindingResult` / `RedirectAttributes` flash |
| `validation.xml` | JSR 380 annotations (`@NotBlank`, `@Size`, etc.) |
| `MessageResources` | `MessageSource` / `messages.properties` |
| `<nested:*>` tags | Thymeleaf `th:*` attributes |
| `<html:form>` | `<form th:action th:object>` |
| `<logic:iterate>` | `th:each` |
| `<bean:write>` | `th:text` |
| `<bean:message>` | `#{...}` message expression |
| Tiles layouts | Thymeleaf Layout Dialect |
| PlugIn | `@Configuration` + `@PostConstruct` |
| Global forwards | `WebMvcConfigurer.addViewControllers()` |
| Global exceptions | `@ControllerAdvice` + `@ExceptionHandler` |
| Struts modules | `@RequestMapping` path prefixes |
