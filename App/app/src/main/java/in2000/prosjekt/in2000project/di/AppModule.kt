package in2000.prosjekt.in2000project.di

import in2000.prosjekt.in2000project.data.datasource.KtorAvalancheRiskDataSource
import in2000.prosjekt.in2000project.data.datasource.KtorFrostV0DataSource
import in2000.prosjekt.in2000project.data.repository.FrostWeatherRepository
import in2000.prosjekt.in2000project.data.datasource.KtorLocationDataSource
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import in2000.prosjekt.in2000project.data.repository.KartverketLocationRepository
import in2000.prosjekt.in2000project.data.repository.NveAvalancheRiskRepository
import in2000.prosjekt.in2000project.data.repository.SharedPreferencesSearchRepository
import in2000.prosjekt.in2000project.domain.SlabAvalancheRiskAssessor
import in2000.prosjekt.in2000project.domain.usecase.GetAvalancheDangerUseCase
import in2000.prosjekt.in2000project.domain.datasource.AvalancheRiskDataSource
import in2000.prosjekt.in2000project.domain.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.domain.datasource.LocationDataSource
import in2000.prosjekt.in2000project.domain.repository.AvalancheRiskRepository
import in2000.prosjekt.in2000project.domain.repository.ClimateRepository
import in2000.prosjekt.in2000project.domain.repository.LocationRepository
import in2000.prosjekt.in2000project.domain.repository.SearchRepository
import in2000.prosjekt.in2000project.domain.repository.WeatherRepository
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
    single<AvalancheRiskDataSource> { KtorAvalancheRiskDataSource(client = get()) }
    single<LocationDataSource> { KtorLocationDataSource(client = get()) }
    single<ClimateRepository> { FrostClimateRepository(get()) }
    single<WeatherRepository> { FrostWeatherRepository(get()) }
    single<AvalancheRiskRepository> { NveAvalancheRiskRepository(get()) }
    single<SearchRepository> { SharedPreferencesSearchRepository(get()) }
    single<LocationRepository> { KartverketLocationRepository(get()) }
    single { SlabAvalancheRiskAssessor() }
    single { MapStateHolder() }
    single { GetAvalancheDangerUseCase(get(), get(), get()) }
    viewModel { MapViewModel(get()) }
    viewModel { RiskViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { InfoViewModel(get()) }

}