package com.achub.hram

/**
 * Marker annotation to indicate that a class or method should be excluded from
 * Kover code coverage reports.
 *
 * Use with Kover filter configured to exclude this annotation (see
 * build-logic/convention/quality-convention.gradle.kts).
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
annotation class NoCoverage

