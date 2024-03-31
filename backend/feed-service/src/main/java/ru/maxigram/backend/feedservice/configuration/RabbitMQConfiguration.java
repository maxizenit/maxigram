package ru.maxigram.backend.feedservice.configuration;

import java.util.List;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.maxigram.backend.rabbitmqcommons.RabbitMQParameters;

@Configuration
public class RabbitMQConfiguration {

  @Bean
  public SimpleMessageConverter simpleMessageConverter() {
    SimpleMessageConverter converter = new SimpleMessageConverter();
    converter.setAllowedListPatterns(List.of("ru.maxigram.backend.rabbitmqcommons.*", "java.util.*"));
    return converter;
  }

  @Bean
  public Queue userActionsFromFeedServiceRequestQueue() {
    return new Queue(RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_REQUEST);
  }

  @Bean
  public Queue userActionsFromFeedServiceResultQueue() {
    return new Queue(RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_RESULT);
  }

  @Bean
  public Exchange exchange() {
    return new DirectExchange(RabbitMQParameters.EXCHANGE);
  }

  @Bean
  public Binding bindingUserActionsFromFeedServiceRequestQueue(Exchange exchange) {
    return BindingBuilder.bind(userActionsFromFeedServiceRequestQueue())
        .to(exchange)
        .with(userActionsFromFeedServiceRequestQueue().getName())
        .noargs();
  }

  @Bean
  public Binding bindingUserActionsFromFeedServiceResultQueue(Exchange exchange) {
    return BindingBuilder.bind(userActionsFromFeedServiceResultQueue())
        .to(exchange)
        .with(userActionsFromFeedServiceResultQueue().getName())
        .noargs();
  }
}
