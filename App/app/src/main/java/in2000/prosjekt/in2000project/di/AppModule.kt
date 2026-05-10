package in2000.prosjekt.in2000project.di

import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.KtorFrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.datasource.NveDataSource
import in2000.prosjekt.in2000project.data.repository.WeatherRepository
import in2000.prosjekt.in2000project.data.repository.FrostWeatherRepository
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import in2000.prosjekt.in2000project.data.repository.NveRepository
import in2000.prosjekt.in2000project.data.repository.SearchRepository
import in2000.prosjekt.in2000project.domain.SlabAvalancheRiskAssessor
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
    single { NveDataSource(client = get()) }
    single { LocationDataSource(client = get()) }
    single { FrostClimateRepository(get()) }
    single<WeatherRepository> { FrostWeatherRepository(get()) }
    single { NveRepository(get()) }
    single { SearchRepository(get()) }
    single { LocationRepository(get()) }
    single { SlabAvalancheRiskAssessor() }
    single { MapStateHolder() }
    viewModel { MapViewModel(get()) }
    viewModel { RiskViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { InfoViewModel(get()) }

}