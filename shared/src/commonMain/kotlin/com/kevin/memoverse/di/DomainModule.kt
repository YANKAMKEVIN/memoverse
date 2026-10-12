package com.kevin.memoverse.di

import com.kevin.memoverse.domain.usecase.GetThemeFamiliesUseCase
import com.kevin.memoverse.domain.usecase.GetThemeWithVersesUseCase
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import com.kevin.memoverse.domain.usecase.StartLearningUseCase
import com.kevin.memoverse.domain.usecase.SyncReviewNotificationsUseCase
import com.kevin.memoverse.domain.usecase.ToggleVerseSelectionUseCase
import com.kevin.memoverse.domain.exercise.ExerciseFactory
import com.kevin.memoverse.domain.usecase.BuildWateringSessionUseCase
import com.kevin.memoverse.domain.usecase.CheckAnswerUseCase
import com.kevin.memoverse.domain.usecase.MeasureRecitationUseCase
import com.kevin.memoverse.domain.usecase.RecordExerciseResultUseCase
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import com.kevin.memoverse.domain.usecase.ListenToRecitationUseCase
import com.kevin.memoverse.domain.usecase.ResetGardenUseCase
import com.kevin.memoverse.domain.usecase.ObserveTypefaceUseCase
import com.kevin.memoverse.domain.usecase.SelectTypefaceUseCase
import com.kevin.memoverse.domain.usecase.ClaimNotificationPermissionRequestUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetThemeFamiliesUseCase(get()) }
    factory { GetThemeWithVersesUseCase(get()) }
    factory { ToggleVerseSelectionUseCase() }
    factory { ObserveLearningVersesUseCase(get()) }
    factory { StartLearningUseCase(get(), get()) }
    factory { ObserveGardenUseCase(get(), get(), get()) }
    factory { SyncReviewNotificationsUseCase(get(), get(), get(), get()) }
    factory { ExerciseFactory(isSpeechAvailable = get<SpeechRecognitionService>().isAvailable) }
    factory { ListenToRecitationUseCase(get()) }
    factory { ResetGardenUseCase(get()) }
    factory { BuildWateringSessionUseCase(get(), get(), get(), get()) }
    factory { RecordExerciseResultUseCase(get(), get()) }
    factory { CheckAnswerUseCase() }
    factory { MeasureRecitationUseCase() }
    factory { ObserveTypefaceUseCase(get()) }
    factory { SelectTypefaceUseCase(get()) }
    factory { ClaimNotificationPermissionRequestUseCase(get()) }
}
