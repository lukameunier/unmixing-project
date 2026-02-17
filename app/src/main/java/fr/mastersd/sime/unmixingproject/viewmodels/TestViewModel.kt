package fr.mastersd.sime.unmixingproject.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.unmixingproject.tflite.TFLiteModelRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val modelRunner: TFLiteModelRunner
) : ViewModel() {

    fun testLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                modelRunner.initialize()
                Log.d("TFLite", "OK - Model loaded successfully: ${modelRunner.isInitialized}")
            } catch (e: Exception) {
                Log.e("TFLite", "PAS OK - Failed to load model: ${e.message}")
            }
        }
    }
}