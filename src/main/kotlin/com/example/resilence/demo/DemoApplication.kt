package com.example.resilence.demo

import org.apache.commons.logging.LogFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.random.Random

@SpringBootApplication
class ReactiveProcessorApplication {

	private val logger = LogFactory.getLog(javaClass)

	private val atomicInteger = AtomicInteger(1)

	@Bean
	fun aggregate(): Function<Flux<String>, Flux<String>> {
		return Function { inbound: Flux<String> ->
			val pipeline = inbound.log()
				.window(Duration.ofSeconds(5), Duration.ofSeconds(5))
				.flatMap { w: Flux<String> ->
					w.reduce(
						""
					) { s1: String, s2: String ->
						val index = if (s2.toInt() % 10 == 0) -1 else 0
						s2[index]
						"$s1 $s2"
					}
				}
				.log()
			addOnErrorResume(pipeline)
		}
	}

	private fun <T> addOnErrorResume(pipeline: Flux<T>): Flux<T> =
		pipeline.onErrorResume {
			addOnErrorResume(pipeline)
		}


	@Bean
	fun testSource(): Supplier<String> {
		return Supplier { atomicInteger.getAndAdd(1).toString() }
	}

	@Bean
	fun testSink(): Consumer<String> {
		return Consumer { payload: String -> logger.info("Data received: $payload") }
	}

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			SpringApplication.run(ReactiveProcessorApplication::class.java, *args)
		}
	}
}

