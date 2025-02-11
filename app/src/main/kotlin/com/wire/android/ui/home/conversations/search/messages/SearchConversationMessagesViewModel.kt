/*
 * Wire
 * Copyright (C) 2024 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.wire.android.ui.home.conversations.search.messages

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.wire.android.ui.home.conversations.search.DEFAULT_SEARCH_QUERY_DEBOUNCE
import com.wire.android.ui.home.conversations.usecase.GetConversationMessagesFromSearchUseCase
import com.wire.android.ui.navArgs
import com.wire.android.util.dispatchers.DispatcherProvider
import com.wire.kalium.logic.data.id.QualifiedID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchConversationMessagesViewModel @Inject constructor(
    private val getSearchMessagesForConversation: GetConversationMessagesFromSearchUseCase,
    private val dispatchers: DispatcherProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val searchConversationMessagesNavArgs: SearchConversationMessagesNavArgs = savedStateHandle.navArgs()
    private val conversationId: QualifiedID = searchConversationMessagesNavArgs.conversationId

    @OptIn(SavedStateHandleSaveableApi::class)
    var searchConversationMessagesState by savedStateHandle.saveable(
        stateSaver = Saver<SearchConversationMessagesState, String>(
            save = { it.searchQuery.text },
            restore = {
                SearchConversationMessagesState(
                    conversationId = conversationId,
                    searchQuery = TextFieldValue(it)
                )
            }
        )
    ) { mutableStateOf(SearchConversationMessagesState(conversationId)) }

    private val mutableSearchQueryFlow = MutableStateFlow(searchConversationMessagesState.searchQuery.text)

    init {
        val messagesResultFlow = mutableSearchQueryFlow
            .onEach {
                searchConversationMessagesState = searchConversationMessagesState.copy(
                    isLoading = true
                )
            }
            .debounce(DEFAULT_SEARCH_QUERY_DEBOUNCE)
            .flatMapLatest { searchTerm ->
                getSearchMessagesForConversation(
                    searchTerm = searchTerm,
                    conversationId = conversationId,
                    lastReadIndex = 0
                ).onEach {
                    searchConversationMessagesState = searchConversationMessagesState.copy(
                        isLoading = false
                    )
                }.flowOn(dispatchers.io())
            }
        searchConversationMessagesState = searchConversationMessagesState.copy(
            searchResult = messagesResultFlow
        )
    }

    fun searchQueryChanged(searchQuery: TextFieldValue) {
        val textQueryChanged = searchConversationMessagesState.searchQuery.text != searchQuery.text
        // we set the state with a searchQuery, immediately to update the UI first
        searchConversationMessagesState = searchConversationMessagesState.copy(
            searchQuery = searchQuery
        )
        if (textQueryChanged && searchQuery.text.isNotBlank()) {
            viewModelScope.launch {
                mutableSearchQueryFlow.emit(searchQuery.text.trim())
            }
        }
    }
}
