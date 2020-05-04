package uk.gov.ons.ctp.response.collection.exercise.lib.rabbit;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/** A class to encapsulate listening for Rabbit messages for use by unit and/or integration tests */
@Slf4j
public class SimpleMessageListener extends SimpleMessageBase {

  private SimpleMessageListenerContainer container;

  /**
   * Constructor for a SimpleMessageListener - expects the rabbit connection details
   *
   * @param host the rabbit host
   * @param port the port the rabbit host is listening on
   * @param username the rabbit user to connect with
   * @param password the rabbit password to connect with
   */
  public SimpleMessageListener(String host, int port, String username, String password) {
    super(host, port, username, password);
  }

  /**
   * Constructor that accepts a Rabbitmq configuration object
   *
   * @param rabbitmq a Rabbitmq configuration object populated by Spring or other means
   */
  public SimpleMessageListener(Rabbitmq rabbitmq) {
    super(rabbitmq);
  }

  /**
   * Listen to all messages on an exchange
   *
   * @param type the type of the exchange
   * @param exchangeName the name of the exchange
   * @return a blocking queue where incoming messages will be posted
   */
  public BlockingQueue<String> listen(ExchangeType type, String exchangeName) {
    return listen(type, exchangeName, null);
  }

  /**
   * Listen to messages on a particular routing key on an exchange
   *
   * @param routingKey the routing key to listen for messages on
   * @param type the type of the exchange
   * @param exchangeName the name of the exchange
   * @return a blocking queue where incoming messages will be posted
   * @return
   */
  public BlockingQueue<String> listen(ExchangeType type, String exchangeName, String routingKey) {
    RabbitAdmin rabbitAdmin = getRabbitAdmin();
    String queueName = getQueueName(exchangeName);
    Queue queue = new Queue(queueName, false, true, true);
    rabbitAdmin.declareQueue(queue);

    declareExchangeAndBind(rabbitAdmin, queue, type, exchangeName, routingKey);

    final BlockingQueue<String> transfer = new ArrayBlockingQueue<>(100);

    MessageListener messageListener =
        new MessageListener() {
          @Override
          public void onMessage(Message message) {
            String msgStr = new String(message.getBody());

            log.info("onMessage: {}", msgStr);

            transfer.add(msgStr);
          }
        };

    ConnectionFactory connectionFactory = getConnectionFactory();
    this.container = new SimpleMessageListenerContainer(connectionFactory);

    this.container.setMessageListener(messageListener);
    this.container.setQueueNames(queueName);
    this.container.start();

    return transfer;
  }

  /** A method to close down a SimpleMessageListener and release any resources */
  public void close() {
    if (this.container != null) {
      this.container.stop();
    }
  }

  /**
   * Method to generate a queuename, based on the name of the exchange and a UUID
   *
   * @param exchangeName the name of the exchange the queue will be bound to
   * @return the proposed name for the queue
   */
  private String getQueueName(String exchangeName) {
    return String.format("%s.queue.%s", exchangeName, UUID.randomUUID().toString());
  }

  /**
   * A private method to declare a given exchange and bind a supplied queue to it
   *
   * @param admin a RabbitAdmin
   * @param queue a previously declared queue
   * @param type the type of the exchange to bind
   * @param exchangeName the name of the exchange to bind
   * @param routingKey the desired routing key or null if not required
   */
  private void declareExchangeAndBind(
      RabbitAdmin admin, Queue queue, ExchangeType type, String exchangeName, String routingKey) {
    switch (type) {
      case Direct:
        DirectExchange de = new DirectExchange(exchangeName);
        admin.declareExchange(de);
        admin.declareBinding(BindingBuilder.bind(queue).to(de).with(routingKey));
        break;
      case Topic:
        TopicExchange te = new TopicExchange(exchangeName);
        admin.declareExchange(te);
        admin.declareBinding(BindingBuilder.bind(queue).to(te).with(routingKey));
        break;
      case Fanout:
        FanoutExchange fe = new FanoutExchange(exchangeName);
        admin.declareExchange(fe);
        admin.declareBinding(BindingBuilder.bind(queue).to(fe));
        break;
      default:
        throw new RuntimeException(String.format("Unknown exchange type %s", type.name()));
    }
  }
}
