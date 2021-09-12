package com.example.springboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class SampleSprintbootApplication: SpringBootServletInitializer()

fun main(args: Array<String>) {
	runApplication<SampleSprintbootApplication>(*args)
}
