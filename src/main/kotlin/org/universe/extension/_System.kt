package org.universe.extension

/**
 * Gets the value of the properties of the application indicated by key, or the environment if not found.
 * @param key Key to find the value.
 * @return The value linked to the key or null if not found.
 */
fun getEnvOrProperty(key: String): String? = System.getProperty(key) ?: System.getenv(key)