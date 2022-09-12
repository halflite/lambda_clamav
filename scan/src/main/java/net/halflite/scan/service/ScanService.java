package net.halflite.scan.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Singleton
public class ScanService {
  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(ScanService.class);
  /** ZIP拡張子 */
  private static final String EXT_TYPE = "ZIP";

  private final AmazonS3 s3Client;
  private final String sourceBucket;

  public void execute(S3Event input) {
    S3Entity s3 = input.getRecords().stream()
        .findFirst()
        .map(S3EventNotificationRecord::getS3)
        .orElseThrow(() -> new RuntimeException("object not found."));

    Optional.of(s3)
        .map(S3Entity::getBucket)
        .map(S3BucketEntity::getName)
        .filter(this.sourceBucket::equals)
        .orElseThrow(() -> new RuntimeException("unknown buckcet."));

    String sourceKey = s3.getObject().getUrlDecodedKey();
    LOG.info("source key: {}", sourceKey);
    Optional.of(sourceKey)
        .map(com.google.common.io.Files::getFileExtension)
        .filter(EXT_TYPE::equalsIgnoreCase)
        .orElseThrow(() -> new RuntimeException("unknown extention."));

    this.scan(sourceKey);
  }

  protected void scan(String sourceKey) {
    try {
      GetObjectRequest req = new GetObjectRequest(this.sourceBucket, sourceKey);
      S3Object s3Object = this.s3Client.getObject(req);
      final Path tempFile = Files.createTempFile(null, ".zip");
      try (Closeable closable = () -> Files.deleteIfExists(tempFile);
          InputStream in = s3Object.getObjectContent()) {
        Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        int scanResult = this.executeClamScan(tempFile);
        if (scanResult != 0) {
          throw new RuntimeException(String.format("Caution: %s", sourceKey));
        }
      }
    } catch (IOException | InterruptedException e) {
      LOG.warn("error", e);
      throw new RuntimeException(String.format("Caution: %s", sourceKey), e);
    }
  }

  protected int executeClamScan(Path tempFile) throws IOException, InterruptedException {
    String filepath = tempFile.normalize().toAbsolutePath().toString();
    LOG.info("clamscan　start, file path: {}", filepath);
    ProcessBuilder pb = new ProcessBuilder("/usr/bin/clamscan", filepath);
    Process process = pb.start();
    int scanResult = process.waitFor();
    LOG.info("clamscan result: {}", scanResult);
    process.destroy();
    return scanResult;
  }

  @Inject
  public ScanService(AmazonS3 s3Client,
      @Named("source.bucket") String sourceBucket) {
    this.s3Client = s3Client;
    this.sourceBucket = sourceBucket;
  }
}
