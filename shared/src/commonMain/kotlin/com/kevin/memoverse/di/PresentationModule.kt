package com.kevin.memoverse.di

import com.kevin.memoverse.presentation.garden.GardenViewModel
import com.kevin.memoverse.presentation.onboarding.OnboardingViewModel
import com.kevin.memoverse.presentation.profile.ProfileViewModel
import com.kevin.memoverse.presentation.progression.ProgressionViewModel
import com.kevin.memoverse.presentation.session.SessionViewModel
import com.kevin.memoverse.presentation.themedetail.ThemeDetailViewModel
import com.kevin.memoverse.presentation.themes.ThemesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { ThemesViewModel(get(), get()) }
    viewModel { GardenViewModel(get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { ProgressionViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SessionViewModel(get(), get(), get(), get(), get()) }
    viewModel { (themeId: String) -> ThemeDetailViewModel(themeId, get(), get(), get(), get()) }
}
