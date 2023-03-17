package com.axelor.message.service;

import com.axelor.app.AppSettings;
import com.axelor.utils.service.AppSettingsServiceImpl;

public class AppSettingsMessageServiceImpl extends AppSettingsServiceImpl implements
    AppSettingsMessageService {

  public String sendingblueUrlSendsms() {
    return AppSettings.get().get("bondici.sendinblue.url.sendsms");
  }
}
