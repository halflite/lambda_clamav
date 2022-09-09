package net.halflite.scan.config;

import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import dagger.Module;
import dagger.Provides;
import fi.solita.clamav.ClamAVClient;

@Module
public class AppModule {

  @Provides
  @Singleton
  public Config providesConfig() {
    return ConfigProvider.getConfig();
  }

  @Provides
  @Singleton
  @Named("source.bucket")
  public String providesSourceBucket(Config config) {
    return config.getValue("source.bucket", String.class);
  }

  @Provides
  @Singleton
  public AmazonS3 providesAmazonS3Client() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  @Provides
  @Singleton
  public ClamAVClient providesClamAvClient(Config config) {
    String host = config.getValue("clamav.host", String.class);
    Integer port = config.getValue("clamav.port", Integer.class);
    return new ClamAVClient(host, port);
  }

}
