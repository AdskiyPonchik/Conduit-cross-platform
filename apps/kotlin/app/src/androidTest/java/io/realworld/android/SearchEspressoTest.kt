package io.realworld.android

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realworld.android.data.ArticlesRepo
import io.realworld.android.data.UserRepo
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.view.KeyEvent

@RunWith(AndroidJUnit4::class)
class SearchEspressoTest {
    companion object {
        @Volatile
        private var seededSearchData = false
    }

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val testEmail = "jonas.lambert@example.com"
    private val testPassword = "ou8123"

    @Before
    fun setUp() {
        ensureSearchData()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    // Test 1: Suche nach "swift"
    @Test
    fun testSearchForSwift_showsResults() {
        performLogin()
        Thread.sleep(2000)
        performSearch("swift")
        Thread.sleep(2000)
        onView(withId(R.id.feedRecyclerView))
            .check(matches(isDisplayed()))

        onView(withId(R.id.feedRecyclerView))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                val recyclerView = view as RecyclerView
                val itemCount = recyclerView.adapter?.itemCount ?: 0
                if (itemCount !in 1..20) {
                    throw AssertionError("Expected first search page with 1..20 items, but found $itemCount")
                }
            }

        onView(withId(R.id.errorTextView))
            .check(matches(not(isDisplayed())))
    }

    // Test 2: Suche nach "swift wissen"
    @Test
    fun testSearchForSwiftWissen_showsExactlyOneResult() {
        performLogin()
        Thread.sleep(2000)
        performSearch("swift wissen")
        Thread.sleep(2000)
        onView(withId(R.id.feedRecyclerView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.feedRecyclerView))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                val recyclerView = view as RecyclerView
                val itemCount = recyclerView.adapter?.itemCount ?: 0
                if (itemCount != 1) {
                    throw AssertionError("Expected exactly 1 item, but found $itemCount")
                }
            }

        onView(withId(R.id.errorTextView))
            .check(matches(not(isDisplayed())))
    }

    // Test 3: Suche nach "foo"
    @Test
    fun testSearchForFoo_showsNoResultsWithMessage() {
        performLogin()
        Thread.sleep(2000)
        performSearch("foo")
        Thread.sleep(2000)

        onView(withId(R.id.feedRecyclerView))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                val recyclerView = view as RecyclerView
                val itemCount = recyclerView.adapter?.itemCount ?: 0
                if (itemCount != 0) {
                    throw AssertionError("Expected 0 items, but found $itemCount")
                }
            }

        onView(withId(R.id.errorTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText("Keine Artikel gefunden.")))
    }

    private fun performLogin() {
        if (isSearchActionAvailable()) return

        onView(withContentDescription("Open navigation drawer"))
            .perform(click())

        Thread.sleep(500)

        if (!isViewDisplayed(withText("Login / Signup"))) {
            waitForSearchAction()
            return
        }

        onView(withText("Login / Signup")).perform(click())

        Thread.sleep(1000)

        onView(withId(R.id.emailEditText))
            .perform(replaceText(testEmail), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(replaceText(testPassword), closeSoftKeyboard())

        onView(withId(R.id.submitButton))
            .perform(click())

        waitForSearchAction()
    }

    private fun performSearch(query: String) {
        openSearchAction()

        waitForView(allOf(
            withClassName(org.hamcrest.Matchers.endsWith("SearchAutoComplete")),
            isDisplayed()
        ))

        onView(allOf(
            withClassName(org.hamcrest.Matchers.endsWith("SearchAutoComplete")),
            isDisplayed()
        )).perform(replaceText(query), closeSoftKeyboard())

        try {
            onView(allOf(
                withClassName(org.hamcrest.Matchers.endsWith("SearchAutoComplete")),
                isDisplayed()
            )).perform(pressImeActionButton())
        } catch (_: Throwable) {
            onView(allOf(
                withClassName(org.hamcrest.Matchers.endsWith("SearchAutoComplete")),
                isDisplayed()
            )).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        }

        waitForView(withText(R.string.menu_search))
    }

    private fun openSearchAction(timeoutMs: Long = 8_000) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            try {
                onView(withId(R.id.action_search)).perform(click())
                return
            } catch (_: Throwable) {
                try {
                    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
                    onView(withText(R.string.action_search)).perform(click())
                    return
                } catch (_: Throwable) {
                }
            }
            Thread.sleep(250)
        }
        throw AssertionError("Search action was not available within $timeoutMs ms")
    }

    private fun waitForSearchAction(timeoutMs: Long = 8_000) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            try {
                onView(withId(R.id.action_search)).check(matches(isDisplayed()))
                return
            } catch (_: NoMatchingViewException) {
            } catch (_: AssertionError) {
            }
            Thread.sleep(250)
        }
        throw AssertionError("Expected search action after login, but it never became visible")
    }

    private fun isSearchActionAvailable(): Boolean {
        return try {
            onView(withId(R.id.action_search)).check(matches(isDisplayed()))
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun isViewDisplayed(matcher: org.hamcrest.Matcher<android.view.View>): Boolean {
        return try {
            onView(matcher).check(matches(isDisplayed()))
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun waitForView(matcher: org.hamcrest.Matcher<android.view.View>, timeoutMs: Long = 8_000) {
        val end = System.currentTimeMillis() + timeoutMs
        var lastError: Throwable? = null
        while (System.currentTimeMillis() < end) {
            try {
                onView(matcher).check(matches(isDisplayed()))
                return
            } catch (t: Throwable) {
                lastError = t
            }
            Thread.sleep(200)
        }
        throw AssertionError("View did not become visible within $timeoutMs ms: $matcher", lastError)
    }

    private fun ensureSearchData() {
        if (seededSearchData) return

        synchronized(SearchEspressoTest::class.java) {
            if (seededSearchData) return

            runBlocking {
                val user = UserRepo.login(testEmail, testPassword)
                    ?: throw AssertionError("Test user login failed; cannot seed search data")

                val swiftWissenCount = ArticlesRepo.searchArticles("swift wissen", 0)?.size ?: 0
                if (swiftWissenCount == 0) {
                    ArticlesRepo.createArticle(
                        title = "swift wissen",
                        description = "e2e fixture",
                        body = "fixture for search tests"
                    )
                }

                val swiftCount = ArticlesRepo.searchArticles("swift", 0)?.size ?: 0
                if (swiftCount == 0) {
                    ArticlesRepo.createArticle(
                        title = "swift basics",
                        description = "e2e fixture",
                        body = "fixture for search tests"
                    )
                }
            }

            seededSearchData = true
        }
    }
}

