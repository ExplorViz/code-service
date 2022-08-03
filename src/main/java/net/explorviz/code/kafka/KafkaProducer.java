package net.explorviz.code.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.avro.SpanStructure;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emits {@link SpanStructure} to Kafka.
 */
@ApplicationScoped
public class KafkaProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

  @Inject
  @Channel("explorviz-spans-structure")
  /* default */ Emitter<SpanStructure> spanStructureEmitter; // NOCS

  /**
   * Dispatch method that is called to emit a {@link SpanStructure} to Kafka.
   *
   * @param spanStructure {@link SpanStructure} element that is send to ExplorViz analysis.
   */
  public void dispatch(final SpanStructure spanStructure) {
    this.spanStructureEmitter.send(spanStructure);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Sent new span structure {}", spanStructure);
    }

  }


}
