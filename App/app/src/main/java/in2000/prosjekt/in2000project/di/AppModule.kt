package in2000.prosjekt.in2000project.di

import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.KtorFrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.datasource.NveDataSource
import in2000.prosjekt.in2000project.data.repository.FrostAvalancheRiskRepository
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import in2000.prosjekt.in2000project.data.repository.NveRepository
import in2000.prosjekt.in2000project.data.repository.SearchRepository
import in2000.prosjekt.in2000project.network.KtorClient
import in2000.prosjekt.in2000project.ui.screens.information.InfoViewModel
import in2000.prosjekt.in2000project.ui.screens.map.MapStateHolder
import in2000.prosjekt.in2000project.ui.screens.map.MapViewModel
import in2000.prosjekt.in2000project.ui.viewmodels.RiskViewModel
import in2000.prosjekt.in2000project.ui.viewmodels.SearchViewModel
import in2000.prosjekt.in2000project.ui.viewmodels.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { KtorClient.frostClient }
    single<FrostV0DataSource> { KtorFrostV0DataSource(client = get()) }
    single<NveDataSource> { NveDataSource(client = get()) }
    single<LocationDataSource> { LocationDataSource(client = get()) }
    single { FrostClimateRepository(get()) }
    single { MapStateHolder() }
    single { FrostAvalancheRiskRepository(get()) }
    single { NveRepository(get()) }
    single { SearchRepository(get()) }
    single { LocationRepository(get()) }
    viewModel { MapViewModel(get()) }
    viewModel { RiskViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { InfoViewModel(get()) }

}