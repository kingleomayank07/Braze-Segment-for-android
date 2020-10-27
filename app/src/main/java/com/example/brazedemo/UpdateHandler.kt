package com.example.brazedemo

import android.content.Context
import com.appboy.Appboy
import com.appboy.ui.contentcards.handlers.DefaultContentCardsUpdateHandler
import com.appboy.ui.contentcards.handlers.IContentCardsUpdateHandler


fun createUserBraze(context: Context, userId: String?, aliasName: String?, aliasLabel: String?) {

    if (!userId.isNullOrEmpty()) {
        Appboy.getInstance(context).changeUser(userId)
    }
    if (!aliasName.isNullOrEmpty() && !aliasLabel.isNullOrEmpty()) {
        Appboy.getInstance(context).currentUser?.addAlias(aliasName, aliasLabel)
    }

}


fun getUpdateHandlerForFeedType(
    desiredFeedType: String,
    disable: Boolean
): IContentCardsUpdateHandler {
    return IContentCardsUpdateHandler { event ->

        // Use the default card update handler for a first
        // pass at sorting the cards. This is not required
        // but is done for convenience.
        val cards = DefaultContentCardsUpdateHandler().handleCardUpdate(event)

        val cardIterator = cards.iterator()

        while (cardIterator.hasNext()) {

            val card = cardIterator.next()

            // disabling user events.
            card.isDismissibleByUser = disable

            // Make sure the card has our custom KVP
            // from the dashboard with the key "feed_type"
            if (card.extras.containsKey("feed_type")) {
                val feedType = card.extras["feed_type"]
                if (desiredFeedType != feedType) {
                    // The card has a feed type, but it doesn't match
                    // our desired feed type, remove it.
                    cardIterator.remove()
                }
            } else {
                // The card doesn't have a feed
                // type at all, remove it
                cardIterator.remove()
            }
        }

        // At this point, all of the cards in this list have
        // a feed type that explicitly matches the value we put
        // in the dashboard.
        cards

    }
}