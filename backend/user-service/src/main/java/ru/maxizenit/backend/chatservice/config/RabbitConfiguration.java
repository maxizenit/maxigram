package ru.maxizenit.backend.chatservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.maxigram.backend.maxigramcommons.rabbit.MaxigramSimpleMessageConverter;
import ru.maxigram.backend.maxigramcommons.rabbit.RabbitParams;

@Configuration
public class RabbitConfiguration {

  @Bean
  public SimpleMessageConverter simpleMessageConverter() {
    return new MaxigramSimpleMessageConverter();
  }

  @Bean
  public Exchange exchange() {
    return new DirectExchange(RabbitParams.EXCHANGE);
  }

  @Bean
  public Queue similarityCalculationRequestQueue() {
    return new Queue(RabbitParams.SIMILARITY_CALCULATION_REQUEST);
  }

  @Bean
  public Queue similarityCalculationResultQueue() {
    return new Queue(RabbitParams.SIMILARITY_CALCULATION_RESULT);
  }

  @Bean
  public Queue userActionsFromFeedServiceRequestQueue() {
    return new Queue(RabbitParams.USER_ACTIONS_FROM_FEED_SERVICE_REQUEST);
  }

  @Bean
  public Queue userActionsFromFeedServiceResultQueue() {
    return new Queue(RabbitParams.USER_ACTIONS_FROM_FEED_SERVICE_RESULT);
  }

  @Bean
  public Binding bindingSimilarityCalculationRequestQueue(Exchange exchange) {
    return BindingBuilder.bind(similarityCalculationRequestQueue())
        .to(exchange)
        .with(similarityCalculationRequestQueue().getName())
        .noargs();
  }

  @Bean
  public Binding bindingSimilarityCalculationResultQueue(Exchange exchange) {
    return BindingBuilder.bind(similarityCalculationResultQueue())
        .to(exchange)
        .with(similarityCalculationResultQueue().getName())
        .noargs();
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
