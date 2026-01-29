package com.axelor.message.db.repo;

import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.utils.helpers.EmailHelper;

public class EmailAddressMessageRepository extends EmailAddressRepository {

  @Override
  public EmailAddress save(EmailAddress entity) {
    String address = entity.getAddress();
    if (!EmailHelper.isValidEmailAddress(address)) {
      throw new IllegalArgumentException(
          String.format(I18n.get(MessageExceptionMessage.INVALID_EMAIL_ADDRESS_FORMAT), address));
    }
    return super.save(entity);
  }
}
