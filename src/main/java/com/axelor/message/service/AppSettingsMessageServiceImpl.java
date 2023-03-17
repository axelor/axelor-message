package com.axelor.message.service;

import com.axelor.app.AppSettings;
import com.axelor.utils.service.AppSettingsServiceImpl;
import com.google.inject.Singleton;
import javax.annotation.concurrent.ThreadSafe;

@Singleton
@ThreadSafe
public class AppSettingsMessageServiceImpl extends AppSettingsServiceImpl
    implements AppSettingsMessageService {

  public String sendingblueUrlSendsms() {
    return AppSettings.get().get("bondici.sendinblue.url.sendsms");
  }
}
