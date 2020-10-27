package com.example.brazedemo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appboy.Appboy
import com.appboy.events.ContentCardsUpdatedEvent
import com.appboy.events.IEventSubscriber
import com.appboy.support.AppboyLogger
import com.appboy.ui.contentcards.AppboyCardAdapter
import com.appboy.ui.contentcards.AppboyEmptyContentCardsAdapter
import com.appboy.ui.contentcards.handlers.DefaultContentCardsUpdateHandler
import com.appboy.ui.contentcards.handlers.DefaultContentCardsViewBindingHandler
import com.appboy.ui.contentcards.handlers.IContentCardsUpdateHandler
import com.appboy.ui.contentcards.handlers.IContentCardsViewBindingHandler
import com.appboy.ui.contentcards.recycler.SimpleItemTouchHelperCallback
import java.util.*

open class CustomAppBoyFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val mMainThreadLooper = Handler(Looper.getMainLooper())
    private var mShowNetworkUnavailableRunnable: Runnable? = null
    var mRecyclerView: RecyclerView? = null
    private var mCardAdapter: AppboyCardAdapter? = null
    private var mEmptyContentCardsAdapter: AppboyEmptyContentCardsAdapter? = null
    private var mContentCardsSwipeLayout: SwipeRefreshLayout? = null
    private var mContentCardsUpdatedSubscriber: IEventSubscriber<ContentCardsUpdatedEvent>? = null
    private val mDefaultContentCardUpdateHandler: IContentCardsUpdateHandler =
        DefaultContentCardsUpdateHandler()
    private var mCustomContentCardUpdateHandler: IContentCardsUpdateHandler? = null
    private val mDefaultContentCardsViewBindingHandler: IContentCardsViewBindingHandler =
        DefaultContentCardsViewBindingHandler()
    private var mCustomContentCardsViewBindingHandler: IContentCardsViewBindingHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mShowNetworkUnavailableRunnable = NetworkUnavailableRunnable(context?.applicationContext!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.com_appboy_content_cards, container, false)
        mRecyclerView = rootView.findViewById(R.id.com_appboy_content_cards_recycler)
        initializeRecyclerView()
        mContentCardsSwipeLayout = rootView.findViewById(R.id.appboy_content_cards_swipe_container)
        mContentCardsSwipeLayout!!.setOnRefreshListener(this)
        mContentCardsSwipeLayout!!.setColorSchemeResources(
            R.color.com_appboy_content_cards_swipe_refresh_color_1,
            R.color.com_appboy_content_cards_swipe_refresh_color_2,
            R.color.com_appboy_content_cards_swipe_refresh_color_3,
            R.color.com_appboy_content_cards_swipe_refresh_color_4
        )
        return rootView
    }

    override fun onRefresh() {
        Appboy.getInstance(context).requestContentCardsRefresh(false)
        mMainThreadLooper.postDelayed({
            mContentCardsSwipeLayout!!.isRefreshing = false
        }, AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS)
    }

    override fun onResume() {
        super.onResume()
        // Remove the previous subscriber before rebuilding a new one with our new activity.
        Appboy.getInstance(context).removeSingleSubscription(
            mContentCardsUpdatedSubscriber,
            ContentCardsUpdatedEvent::class.java
        )
        if (mContentCardsUpdatedSubscriber == null) {
            mContentCardsUpdatedSubscriber = IEventSubscriber { event: ContentCardsUpdatedEvent ->
                val contentCardsUpdateRunnable =
                    ContentCardsUpdateRunnable(event)
                mMainThreadLooper.post(contentCardsUpdateRunnable)
            }
        }
        Appboy.getInstance(context)
            .subscribeToContentCardsUpdates(mContentCardsUpdatedSubscriber)
        Appboy.getInstance(context).requestContentCardsRefresh(true)
        Appboy.getInstance(context).logContentCardsDisplayed()
    }

    override fun onPause() {
        super.onPause()
        // If the view is going away, we don't care about updating it anymore. Remove the subscription immediately.
        Appboy.getInstance(context).removeSingleSubscription(
            mContentCardsUpdatedSubscriber,
            ContentCardsUpdatedEvent::class.java
        )
        mMainThreadLooper.removeCallbacks(mShowNetworkUnavailableRunnable!!)
        mCardAdapter!!.markOnScreenCardsAsRead()
    }

    var contentCardUpdateHandler: IContentCardsUpdateHandler?
        get() = if (mCustomContentCardUpdateHandler != null) mCustomContentCardUpdateHandler else mDefaultContentCardUpdateHandler
        set(contentCardUpdateHandler) {
            mCustomContentCardUpdateHandler = contentCardUpdateHandler
        }

    private var contentCardsViewBindingHandler: IContentCardsViewBindingHandler?
        get() = if (mCustomContentCardsViewBindingHandler != null) mCustomContentCardsViewBindingHandler else mDefaultContentCardsViewBindingHandler
        set(contentCardsViewBindingHandler) {
            mCustomContentCardsViewBindingHandler = contentCardsViewBindingHandler
        }

    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mRecyclerView != null && mRecyclerView!!.layoutManager != null) {
            outState.putParcelable(
                LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY,
                mRecyclerView!!.layoutManager!!.onSaveInstanceState()
            )
        }
        if (mCardAdapter != null) {
            outState.putStringArrayList(
                KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY,
                mCardAdapter!!.impressedCardIds as ArrayList<String?>
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onViewStateRestored(@Nullable savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            // do nothing and return
            return
        }
        mMainThreadLooper.post {
            val layoutManagerState =
                savedInstanceState.getParcelable<Parcelable>(LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY)
            val layoutManager: RecyclerView.LayoutManager = mRecyclerView?.layoutManager!!
            if (layoutManagerState != null) {
                layoutManager.onRestoreInstanceState(layoutManagerState)
            }
            val savedCardIdImpressions: List<String>? =
                savedInstanceState.getStringArrayList(
                    KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY
                )
            if (savedCardIdImpressions != null) {
                mCardAdapter!!.impressedCardIds = savedCardIdImpressions
            }
        }
    }


    private fun initializeRecyclerView() {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        mCardAdapter = AppboyCardAdapter(
            context, layoutManager, ArrayList(),
            contentCardsViewBindingHandler
        )
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(mRecyclerView)
        snapHelper.onFling(200, 200)
        mRecyclerView!!.adapter = mCardAdapter
        mRecyclerView!!.layoutManager = layoutManager
        attachSwipeHelperCallback()

        // Disable any animations when the items change to avoid any issues when the data changes
        // see https://stackoverflow.com/questions/29331075/recyclerview-blinking-after-notifydatasetchanged

        /*val animator: RecyclerView.ItemAnimator = mRecyclerView.getItemAnimator()
        if (animator is SimpleItemAnimator) {
            (animator as SimpleItemAnimator).setSupportsChangeAnimations(false)
        }*/

        // Add an item divider
        // mRecyclerView!!.addItemDecoration(ContentCardsDividerItemDecoration(context))

        // Create the "empty" adapter
        mEmptyContentCardsAdapter = AppboyEmptyContentCardsAdapter()
    }


    private fun attachSwipeHelperCallback() {
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(mCardAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)
    }

    private inner class ContentCardsUpdateRunnable(private val mEvent: ContentCardsUpdatedEvent) :
        Runnable {
        override fun run() {
            AppboyLogger.v(
                TAG,
                "Updating Content Cards views in response to ContentCardsUpdatedEvent: $mEvent"
            )
            val cardsForRendering = contentCardUpdateHandler!!.handleCardUpdate(mEvent)
            mCardAdapter!!.replaceCards(cardsForRendering)
            mMainThreadLooper.removeCallbacks(mShowNetworkUnavailableRunnable!!)

            // If the update came from storage and is stale, then request a refresh.
            if (mEvent.isFromOfflineStorage && mEvent.isTimestampOlderThan(
                    MAX_CONTENT_CARDS_TTL_SECONDS.toLong()
                )
            ) {
                AppboyLogger.i(
                    TAG, "ContentCards received was older than the max time "
                            + "to live of " + MAX_CONTENT_CARDS_TTL_SECONDS + " seconds, displaying it "
                            + "for now, but requesting an updated view from the server."
                )
                Appboy.getInstance(context?.applicationContext)!!.requestContentCardsRefresh(false)

                // If we don't have any cards to display, we put up the spinner while
                // we wait for the network to return.
                // Eventually displaying an error message if it doesn't.
                if (mEvent.isEmpty) {
                    // Display a loading indicator
                    mContentCardsSwipeLayout!!.isRefreshing = true
                    AppboyLogger.d(
                        TAG, "Old Content Cards was empty, putting up a "
                                + "network spinner and registering the network "
                                + "error message on a delay of " + NETWORK_PROBLEM_WARNING_MS + " ms."
                    )
                    mMainThreadLooper.postDelayed(
                        mShowNetworkUnavailableRunnable!!,
                        NETWORK_PROBLEM_WARNING_MS
                    )
                    return
                }
            }

            // The cards are either fresh from the cache, or came directly from a
            // network request. An empty Content Cards should just display
            // an "empty ContentCards" message.
            if (!mEvent.isEmpty) {
                // The Content Cards contains cards and should be displayed
                swapRecyclerViewAdapter(mCardAdapter)
            } else {
                // The Content Cards is empty and should display an "empty" message to the user.
                swapRecyclerViewAdapter(mEmptyContentCardsAdapter)
            }

            // Stop the refresh animation
            mContentCardsSwipeLayout!!.isRefreshing = false
        }
    }

    private inner class NetworkUnavailableRunnable(private val mApplicationContext: Context) :
        Runnable {
        override fun run() {
            AppboyLogger.v(TAG, "Displaying network unavailable toast.")
            Toast.makeText(
                mApplicationContext,
                mApplicationContext.getString(R.string.com_appboy_feed_connection_error_title),
                Toast.LENGTH_LONG
            ).show()
            swapRecyclerViewAdapter(mEmptyContentCardsAdapter)
            mContentCardsSwipeLayout!!.isRefreshing = false
        }
    }

    private fun swapRecyclerViewAdapter(newAdapter: RecyclerView.Adapter<*>?) {
         if (mRecyclerView != null && mRecyclerView!!.adapter !== newAdapter) {
             mRecyclerView!!.adapter = newAdapter
         }
    }

    companion object {
        private val TAG = AppboyLogger.getAppboyLogTag(CustomAppBoyFragment::class.java)
        private const val MAX_CONTENT_CARDS_TTL_SECONDS = 60
        private const val NETWORK_PROBLEM_WARNING_MS = 5000L
        private const val AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS = 2500L
        private const val LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY =
            "LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY"
        private const val KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY =
            "KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY"
    }
}
