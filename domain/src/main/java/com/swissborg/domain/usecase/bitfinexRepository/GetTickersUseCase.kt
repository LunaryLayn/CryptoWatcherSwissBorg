package com.swissborg.domain.usecase.bitfinexRepository

import android.util.Log
import com.swissborg.domain.repository.BitfinexRepository
import com.swissborg.domain.repository.OnGetTickersCompleted
import kotlinx.coroutines.delay
import javax.inject.Inject

class GetTickersUseCase @Inject constructor(
    private val repository: BitfinexRepository
) {
    @Volatile private var isRunning = true  // Control de ejecución del ciclo

    suspend operator fun invoke(onGetTickersCompleted: OnGetTickersCompleted) {
        while (isRunning) {
            Log.d("MainViewModel", "Una vueltesita")
            repository.getTickers(onGetTickersCompleted)
            delay(5000)
        }
    }

    // Método para detener la ejecución desde el ViewModel
    fun stop() {
        isRunning = false
    }
}