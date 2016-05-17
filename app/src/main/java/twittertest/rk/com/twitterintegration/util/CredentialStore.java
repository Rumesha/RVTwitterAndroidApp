package twittertest.rk.com.twitterintegration.util;

/**
 * Created by ASER ASPIRE on 5/16/2016.
 */
public interface CredentialStore {

  String[] read();
  void write(String[] response);
  void clearCredentials();
}
