package jimlind.filmlinkd.listener;

import com.google.cloud.pubsub.v1.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriberListener extends Subscriber.Listener {

  @Override
  public void failed(Subscriber.State from, Throwable failure) {
    log.info("The Pub/Sub subscriber has encountered a fatal error and is shutting down.");
    log.info(failure.toString());
    System.exit(-1);
  }

  @Override
  public void running() {
    log.info("The Pub/Sub subscriber has successfully started and is running.");
  }

  @Override
  public void starting() {
    log.info("The Pub/Sub subscriber is starting.");
  }

  @Override
  public void stopping(Subscriber.State from) {
    log.info("The Pub/Sub subscriber is stopping.");
  }

  @Override
  public void terminated(Subscriber.State from) {
    log.info("The Pub/Sub subscriber is being terminated.");
    System.exit(-1);
  }
}
