## 3.1.2 (2024-08-30)

#### Fix

* Update email sending thread with TenantAware

  <details>
  
  Update thread sending email jobs with TenantAware
  
  </details>


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
