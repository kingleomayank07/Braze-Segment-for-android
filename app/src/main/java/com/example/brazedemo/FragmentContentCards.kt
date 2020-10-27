package com.example.brazedemo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.appboy.models.cards.Card
import com.appboy.ui.actions.IAction
import com.appboy.ui.contentcards.AppboyContentCardsManager
import com.appboy.ui.contentcards.listeners.IContentCardsActionListener


class FragmentContentCards : CustomAppBoyFragment() {

    private val desiredFeedType = "Delhi"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentCardUpdate = getUpdateHandlerForFeedType(desiredFeedType, false)
        this.contentCardUpdateHandler = contentCardUpdate

        AppboyContentCardsManager.getInstance().contentCardsActionListener =
            object : IContentCardsActionListener {
                override fun onContentCardClicked(
                    context: Context?,
                    card: Card?,
                    cardAction: IAction?
                ): Boolean {
                    return if (card?.url != null) {
                        findNavController().navigate(Uri.parse(card.url))
                        true
                    } else false
                }

                override fun onContentCardDismissed(context: Context?, card: Card?) {}
            }

    }
}