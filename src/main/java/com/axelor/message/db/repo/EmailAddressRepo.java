package com.axelor.message.db.repo;

import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.exception.MessageExceptionMessage;
import java.util.regex.Pattern;

public class EmailAddressRepo extends EmailAddressRepository {
  private static final String EMAIL_PATTERN =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
  private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

  @Override
  public EmailAddress save(EmailAddress entity) {
    String address = entity.getAddress();
    if (address == null || !pattern.matcher(address).matches()) {
      throw new IllegalArgumentException(
          String.format(I18n.get(MessageExceptionMessage.INVALID_EMAIL_ADDRESS_FORMAT), address));
    }
    return super.save(entity);
  }
}
