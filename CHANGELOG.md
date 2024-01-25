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
