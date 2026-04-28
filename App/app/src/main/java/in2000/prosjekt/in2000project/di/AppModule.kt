package in2000.prosjekt.in2000project.di

import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.KtorFrostV0DataSource
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import in2000.prosjekt.in2000project.network.KtorClient
import in2000.prosjekt.in2000project.ui.map.MapViewModel
import in2000.prosjekt.in2000project.util.ApiKeys
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { KtorClient.frostClient }
    single<FrostV0DataSource> { KtorFrostV0DataSource(client = get()) }
    single { FrostClimateRepository(get()) }
    viewModel { MapViewModel(get()) }
}