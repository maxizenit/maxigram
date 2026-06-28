package org.maxizenit.maxigram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MaxigramApplication

fun main(args: Array<String>) {
    runApplication<MaxigramApplication>(*args)
}
