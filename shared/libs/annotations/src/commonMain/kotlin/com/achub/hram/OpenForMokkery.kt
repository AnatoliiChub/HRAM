package com.achub.hram

/**
 * Marks a class to be opened by the `allOpen` compiler plugin so Mokkery can mock it in tests.
 * SOURCE retention ensures the annotation is erased from compiled bytecode — only the allOpen
 * compiler plugin (which runs during compilation) needs to see it.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class OpenForMokkery

