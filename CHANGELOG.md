## 4.0.2 (2026-01-27)

#### Fix

* Import @Inject from jakarta.inject instead of com.google.inject

## 4.0.1 (2026-01-19)

#### Change

* Refactor email validation to use `EmailHelper` from `axelor-utils`

  <details>
  
  Refactored `EmailAddressRepo` to delegate email validation to `EmailHelper.isValidEmailAddress()`
  from `axelor-utils` instead of maintaining a duplicate regex pattern.
  
  This ensures consistent email validation across all modules and centralizes the fix
  for dashes in intermediate domain segments (e.g., user@hdf.my-company.fr).
  
  </details>


## 4.0.0 (2025-10-20)

#### Feature

* Upgrade Axelor Open Platform to 8.0

  <details>
  
  Read AOP migration guide to update your project.
  
  </details>

#### Change

* Upgrade `okhttp` to 5.2.1
* Upgrade `axelor-utils` to 4.0.0
* Change the type of the parameter `isForTemporaryMessage` from `Boolean` to primitive `boolean`

  <details>
  
  The type of the parameter `isForTemporaryMessage` has changed from `Boolean` to primitive `boolean` in the following methods:
  - `MessageService.createMessage(String, long, String, String, EmailAddress, List<EmailAddress>, List<EmailAddress>, List<EmailAddress>, List<EmailAddress>, Set<MetaFile>, String, int, EmailAccount, String, Boolean)`
  - `MessageService.sendMessage(Message, Boolean)`
  - `TemplateMessageService.generateMessage(Model, Template, Boolean)`
  - `TemplateMessageService.generateMessage(Long, String, String, Template, Boolean)`
  
  </details>

* Use React template instead of legacy angular

#### Remove

* Delete the deprecated class `IExceptionMessage`. Use `MessageExceptionMessage` instead.
* Remove unreferenced constants

  <details>
  
  The following constants have been removed from `MessageExceptionMessage`:
  - `MAIL_ACCOUNT_4`
  - `MESSAGE_1`
  - `SET_EMAIL_TEMPLATE_MESSAGE`
  - `SEND_EMAIL_EXCEPTION`
  
  </details>


## 3.3.0 (2025-10-10)

#### Feature

* Upgrade JaCoCo to 0.8.13
* Pre-send action on `MailMessage`

  <details>
  
  Consumers module can now implement `preSendAction` method of `MailMessageAction` to trigger actions on a `MailMessage` record before sending it.
  
  </details>

* Upgrade message module to AOP 7.4

#### Change

* Rename `MailMessageAction` class and its related services

  <details>
  
  The name was confusing as it was actually handling `Message` records and not `MailMessage` records.
  * Legacy `MailMessageAction` has been replaced by `MessageAction`.
  * Legacy `MailMessageActionService` has been replaced by `MessageActionService`.
  * Legacy `MailMessageActionServiceImpl` has been replaced by `MessageActionServiceImpl`.
  
  </details>

* Rename `MailMessageServerStartListener` into `ServerStartListener`

  <details>
  
  * `ServerStartListener` manages different registers: `MessageServerStartListener` and `MailMessageServerStartListener`.
  
  </details>

#### Fix

* In progress messages blocked

  <details>
  
  Allow to resend messages in the "In Progress" status from the message form view.
  
  </details>

* Prevent duplicate email sending

  <details>
  
  Improved the message sending logic to ensure a message is only sent if it's in draft status. This prevents accidental multiple sends when clicking the send button twice.
  
  </details>

* Remove unnecessary save action from validate button
* Duplicate email addresses created when fetching emails
* Pattern validation issue for email address

  <details>
  
  Enforce the regex pattern in the repository to prevent persistence of invalid email addresses when creating it using "Create and Select".
  
  </details>

* Move the action `action-message-method-print-pdf-email` definition to `axelor-message` module

  <details>
  
  The action `action-message-method-print-pdf-email` is now explicitly declared in Axelor Message instead of relying on its definition from the Base module.
  
  </details>

* Encode from name

  <details>
  
  Encode fromName in MessageServiceImpl#sendByEmail to handle non-ASCII characters, preventing display issues in user inboxes.
  
  </details>

* Update schema locations to version 7.3 in XML configuration files

  <details>
  
  Updated XML schema references to the latest version (7.3) across domain, 
  view, and data-import files. Upgraded `axelor-utils` dependency from 
  version 3.2.1 to 3.3.0 in the build configuration for compatibility.
  
  </details>


## 3.2.2 (2024-11-04)

#### Fix

* Replace ForkJoin pool usages by executor services

  <details>
  
  - Replace Fork join usage and use ExecutionService instead
  
  </details>


## 3.2.1 (2024-10-17)

#### Fix

* Update axelor utils version to use aop 7.2.1

  <details>
  
  Update axelor utils version and use aop 7.2.1 fix
  
  </details>


## 3.2.0 (2024-10-08)

#### Feature

* Support Draft message generation on custom models

  <details>
  
  Support costume models while generating a draft message or testing a template costume models are added on test template wizard so its possible to select a json model and the select the record
  
  </details>

#### Change

* Extract all ALL_MODEL_SELECT related code from TemplateService

  <details>
  
  Refactored TemplateService fixing bad practices. Extracted all ALL_MODEL_SELECT related logic into Utils module.
  
  </details>


## 3.1.2 (2024-08-30)

#### Fix

* Update email sending thread with TenantAware

  <details>
  
  Update thread sending email jobs with TenantAware
  
  </details>


## 3.1.1 (2024-06-27)

#### Change

* Change AOP version to 7.1.0

## 3.1.0 (2024-05-31)

#### Feature

* adapt message grid-view

  <details>
  
  adapt message grid-view to better display fields
  
  </details>

#### Change

* Refactoring Template Message Service

  <details>
  
  Refactoring the template message service by removing some duplications, extract methods
  from code to lower complexity of functions with the help of sonarLint, use of DI and Constructor injection
  
  </details>

#### Fix

* Update GenerateMessageWizard from Message module and support template language selection

  <details>
  
  Updating the language axelor base reference from the Message module to an aop reference in GenerateMessageWizard,
  and implementing language selection within the template.
  ```
  
  </details>

* Remove references to AOS models

  <details>
  
  Remove AOS models reference from the message module 
  ```
  
  </details>

* Remove duplicate selections and constants for media type and handle EMAILING in message

  <details>
  
  isolate duplicated media type constants (MAIL,EMAIL,SMS..) from message and template domains
  and adding new constant of type EMAILING
  
  </details>


## 3.0.1 (2024-04-26)

#### Fix

* Remove duplicate selections and constants for media type and handle EMAILING in message

  <details>
  
  isolate duplicated media type constants (MAIL,EMAIL,SMS..) from message and template domains
  and adding new constant of type EMAILING
  
  </details>


## 3.0.0 (2024-04-03)

#### Feature

* Implement MailMessage action register

  <details>
  
  Implement a mail message action registry that can identify classes that implements a generic class of the MailMessageAction 
  and execute those post actions in the generation of the message.
  
  </details>

* Indicate Mail account in template

  <details>
  
  Add MailAccount field to the template, allowing users to select a mail account to use for each message template.
  
  </details>


## 2.0.0 (2024-01-25)

#### Feature

* Add a thread-safe asynchronous MailMessageCreator

  <details>
  
  This change also remove the com.github.groovy-wslite dependency 
  to get rid of the associated vulnerable transitive dependency and replace it with the usage of 
  Jackson library by the new JsonUtils classes.
  The memory leak issue in the sendSMS service is also fixed.
  
  </details>


## 1.2.2 (2023-09-14)

#### Fixed

* Bump Utils module to 1.3.1

## 1.2.1 (2023-09-14)

#### Fixed

* Fix AOP version to 6.1.5

## 1.2.0 (2023-09-08)

#### Fixed

* Fix toMobilePhone field visibility condition

## 1.1.0 (2023-07-03)

#### Changes

* EmailAccount : Sendinblue connector for sending SMS issues

#### Features

* Bump AOP version to 6.1.3
* Update xsd to 6.1

## 1.0.1

* Anomalie #62504: Menu: Plural form missing ('s')
* Anomalie #62744: Fixed a bug that could occur when sending a mail with no content

## 1.0.0

* Initial AOP Addons version
* Evolution #58178: Migrate AOS axelor-message to AOP Addons
* Anomalie #61000: EmailAccount : isValid boolean not updated
* Anomalie #61094: Encrypt SMTP account password field
