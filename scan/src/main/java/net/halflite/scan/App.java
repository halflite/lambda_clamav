package net.halflite.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import net.halflite.scan.config.AppComponent;
import net.halflite.scan.config.DaggerAppComponent;
import net.halflite.scan.service.ScanService;

public class App implements RequestHandler<S3Event, Void> {
  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(App.class);
  
  private final ScanService scanService;
  
  @Override
  public Void handleRequest(S3Event input, Context context) {
    try {
      LOG.info("started.");
      this.scanService.execute(input);
      // TODO 何か成功キューを送る
      LOG.info("completed.");
      return null;
    } catch (Exception e) {
      LOG.info("error.", e);
      // TODO 何か失敗キューを送る
      return null;
    }
  }

  public App() {
    AppComponent appComponent = DaggerAppComponent.create();
    this.scanService = appComponent.scanService();
  }
}
