package ru.maxigram.backend.chatservice.configuration;

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
    converter.setAllowedListPatterns(
        List.of("ru.maxigram.backend.rabbitmqcommons.*", "java.util.*"));
    return converter;
  }

  @Bean
  public Queue similarityCalculationRequestQueue() {
    return new Queue(RabbitMQParameters.SIMILARITY_CALCULATION_REQUEST);
  }

  @Bean
  public Queue similarityCalculationResultQueue() {
    return new Queue(RabbitMQParameters.SIMILARITY_CALCULATION_RESULT);
  }

  @Bean
  public Exchange exchange() {
    return new DirectExchange(RabbitMQParameters.EXCHANGE);
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
}
