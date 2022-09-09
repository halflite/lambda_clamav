package net.halflite.scan.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import fi.solita.clamav.ClamAVClient;

@Singleton
public class ScanService {
  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(ScanService.class);
  /** ZIP拡張子 */
  private static final String EXT_TYPE = "ZIP";

  private final AmazonS3 s3Client;
  private final ClamAVClient clamAVClient;
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
        .map(FilenameUtils::getExtension)
        .filter(EXT_TYPE::equalsIgnoreCase)
        .orElseThrow(() -> new RuntimeException("unknown extention."));

    this.scan(sourceKey);
  }

  protected void scan(String sourceKey) {
    GetObjectRequest req = new GetObjectRequest(this.sourceBucket, sourceKey);
    S3Object s3Object = this.s3Client.getObject(req);
    try(InputStream in = s3Object.getObjectContent()) {
      byte[] bytes = IOUtils.toByteArray(in);
      if (!ClamAVClient.isCleanReply(this.clamAVClient.scan(bytes)))
      {
          throw new RuntimeException(String.format("Caution: %s", sourceKey));
      }
    } catch (IOException e) {
      LOG.warn("error", e);
      throw new RuntimeException(e);
    }
 }

  @Inject
  public ScanService(AmazonS3 s3Client,
      ClamAVClient clamAVClient,
      @Named("source.bucket") String sourceBucket) {
    this.s3Client = s3Client;
    this.clamAVClient = clamAVClient;
    this.sourceBucket = sourceBucket;
  }
}
