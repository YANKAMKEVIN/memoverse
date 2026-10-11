package com.kevin.memoverse.di

import org.koin.core.module.Module

/** Fournit le `SqlDriver` propre à chaque plateforme. */
expect val platformModule: Module
