package ru.maxigram.backend.maxigramcommons.rabbit;

import java.util.List;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

public class MaxigramSimpleMessageConverter extends SimpleMessageConverter {

  public MaxigramSimpleMessageConverter() {
    super();
    setAllowedListPatterns(List.of("java.util.*", "ru.maxigram.backend.maxigramcommons.*"));
  }
}
