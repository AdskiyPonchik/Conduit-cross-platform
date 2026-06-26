package io.realworld.android

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchEspressoTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val testEmail = "test@test.com"
    private val testPassword = "12345678"

    @Before
    fun setUp() {
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
            .check(matches(hasMinimumChildCount(1)))

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
        onView(withContentDescription("Open navigation drawer"))
            .perform(click())

        Thread.sleep(500)

        onView(withText("Login / Signup"))
            .perform(click())

        Thread.sleep(1000)

        onView(withId(R.id.emailEditText))
            .perform(replaceText(testEmail), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(replaceText(testPassword), closeSoftKeyboard())

        onView(withId(R.id.submitButton))
            .perform(click())
    }

    private fun performSearch(query: String) {
        onView(withId(R.id.action_search))
            .perform(click())

        Thread.sleep(500)
        onView(allOf(
            withClassName(org.hamcrest.Matchers.endsWith("SearchAutoComplete")),
            isDisplayed()
        )).perform(typeText(query), pressImeActionButton())
    }
}

