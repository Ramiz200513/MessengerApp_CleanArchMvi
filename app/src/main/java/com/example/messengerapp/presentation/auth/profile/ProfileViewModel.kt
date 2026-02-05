package com.example.messengerapp.presentation.auth.profile

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.domain.usecase.GetCurrentUserUseCase
import com.example.domain.domain.usecase.SaveProfileUseCase
import com.example.domain.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sign

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    init{
        handleIntent(ProfileIntent.LoadProfile)
    }
    fun handleIntent(intent: ProfileIntent){
        when(intent){
            ProfileIntent.LoadProfile -> {
               loadProfile()
            }
            is ProfileIntent.OnAvatarChanged -> { updateAvatarInState(intent.url) }
            is ProfileIntent.OnNameChanged -> {updateNameInState(intent.newName)}
            is ProfileIntent.OnSaveProfile -> {
                saveChanges()
            }
            is ProfileIntent.OnSignOutClicked -> {
                logOut()
            }
        }
    }

    private fun updateAvatarInState(url: String) {
        val currentUser = _state.value.user
        if(currentUser!=null){
            _state.update { it.copy(user = currentUser.copy(photoUrl = url)) }
        }
    }

    private fun saveChanges() {
        val userToSave = _state.value.user ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            saveProfileUseCase(userToSave).onSuccess {
                _state.update { it.copy(isLoading = false, isEditing = false) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun updateNameInState(newName: String) {
        val currentUser = _state.value.user
        _state.update { it.copy(user = currentUser?.copy(username = newName)) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update {it.copy(isLoading = true, error = null)}
            getCurrentUserUseCase().onSuccess { user ->
                _state.update {it.copy(isLoading = false, user = user)}
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun logOut() {
        viewModelScope.launch {
            signOutUseCase()
            _state.update { it.copy(isSignedOut = true) }
        }
    }

}