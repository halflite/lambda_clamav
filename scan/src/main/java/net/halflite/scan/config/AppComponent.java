package net.halflite.scan.config;

import javax.inject.Singleton;
import dagger.Component;
import net.halflite.scan.service.ScanService;

/** 
 * Daggerのコンポーネント定義
 * 
 * @author shing
 *
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
  public ScanService scanService();
}
