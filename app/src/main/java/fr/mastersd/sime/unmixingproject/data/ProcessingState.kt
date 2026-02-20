package fr.mastersd.sime.unmixingproject.data

sealed class ProcessingState {
    object Idle : ProcessingState()
    data class Loading(val progress: Float) : ProcessingState()
    data class Success(val separatedTrack: SeparatedTrack) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}