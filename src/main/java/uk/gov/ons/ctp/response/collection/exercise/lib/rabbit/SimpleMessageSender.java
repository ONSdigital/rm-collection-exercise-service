package uk.gov.ons.ctp.response.collection.exercise.lib.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class SimpleMessageSender extends SimpleMessageBase {

  public SimpleMessageSender(String host, int port, String username, String password) {
    super(host, port, username, password);
  }

  /** Constructor for use by unit tests */
  SimpleMessageSender() {
    super();
  }

  /**
   * Constructor that accepts a Rabbitmq configuration object
   *
   * @param rabbitmq a Rabbitmq configuration object populated by Spring or other means
   */
  public SimpleMessageSender(Rabbitmq rabbitmq) {
    super(rabbitmq);
  }

  public void sendMessage(String exchange, String routingKey, String message) {
    RabbitTemplate rabbitTemplate = getRabbitTemplate();

    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }

  public void sendMessage(String exchange, String message) {
    RabbitTemplate rabbitTemplate = getRabbitTemplate();

    rabbitTemplate.convertAndSend(exchange, message);
  }

  /**
   * This method sends a message direct to a Rabbit queue. It's use is really not recommended as all
   * messages should go through an exchange. However some of the RM services are accdientally
   * posting direct to queues so this method is here to allow integration tests to mimic this
   * behaviour.
   *
   * <p>This works because, by default, queues are bound to the default exchange with the name of
   * the queue as the routing key. Reference: https://goo.gl/mBqv13
   *
   * @param queueName the name of the queue to send the message to
   * @param message the message to send
   */
  public void sendMessageToQueue(String queueName, String message) {
    RabbitTemplate rabbitTemplate = getRabbitTemplate();

    rabbitTemplate.convertAndSend(queueName, message);
  }
}
