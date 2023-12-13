package org.intellij.markdown

/**
 * API elements marked with this annotation should be considered unstable and might change
 * in the future breaking source/binary compatibility.
 */
@RequiresOptIn(
  message = "This API is experimental and might change in the future.",
  level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class ExperimentalApi
