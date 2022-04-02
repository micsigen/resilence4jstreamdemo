package com.example.resilence.demo

import org.apache.commons.logging.LogFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import java.lang.RuntimeException
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

@SpringBootApplication
class ReactiveProcessorApplication {
	private val logger = LogFactory.getLog(javaClass)
	private val atomicInteger = AtomicInteger()
	@Bean
	fun aggregate(): Function<Flux<String>, Flux<String>> {
		return Function { inbound: Flux<String> ->
			inbound.log()
				.window(Duration.ofSeconds(30), Duration.ofSeconds(5))
				.flatMap { w: Flux<String> ->
					w.reduce(
						" "
					) { s1: String, s2: String -> "$s1 $s2" }
				}
				.log()
		}
	}

	//Following source and sinks are used for testing only.
	//Test source will send data to the same destination where the processor receives data
	//Test sink will consume data from the same destination where the processor produces data
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

