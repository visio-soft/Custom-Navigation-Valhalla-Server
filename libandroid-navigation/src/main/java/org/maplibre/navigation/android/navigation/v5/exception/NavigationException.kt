package org.maplibre.navigation.android.navigation.v5.exception

/**
 * Generic Exception for all things MapLibre Navigation. That throwable indicates conditions
 * that a reasonable application might want to catch.
 *
 * @param message the detail message (which is saved for later retrieval by the
 *  [.getMessage] method).
 * @since 0.2.0
 */
open class NavigationException(message: String) : RuntimeException(message)
