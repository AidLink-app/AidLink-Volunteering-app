import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.welcom.OrganizationRegistrationActivity
import com.example.welcom.R
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

@RunWith(AndroidJUnit4::class)
class OrganizationRegistrationActivityTest {

    @Before
    fun setup() {
        // Initialize Intents
        Intents.init()
        // Explicitly launch the activity
        ActivityScenario.launch(OrganizationRegistrationActivity::class.java)
    }

    @After
    fun teardown() {
        // Release Intents
        Intents.release()
    }

    @Test
    fun testUIInteractions() {
        // Enter a description
        onView(withId(R.id.etDescription))
            .perform(typeText("Test description"), closeSoftKeyboard())

        // Enter an email
        onView(withId(R.id.etEmail))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Enter a password
        onView(withId(R.id.etPassword))
            .perform(typeText("Password123"), closeSoftKeyboard())

        // Verify text in the description field
        onView(withId(R.id.etDescription))
            .check(matches(withText("Test description")))

        // Verify text in the email field
        onView(withId(R.id.etEmail))
            .check(matches(withText("test@example.com")))

        // Verify text in the password field
        onView(withId(R.id.etPassword))
            .check(matches(withText("Password123")))

        // Simulate clicking the "Submit" button
        onView(withId(R.id.btnSubmit))
            .perform(click())

    }

    @Test
    fun testChooseImage() {
        // Mock the image selection result
        val resultData = Intent()
        val imageUri = Uri.parse("content://mock/image.jpg")
        resultData.data = imageUri
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Specify the intent matcher and respond with the mocked result
        Intents.intending(allOf(hasAction(Intent.ACTION_PICK), hasType("image/*")))
            .respondWith(result)

        // Click the "Choose Profile Picture" button
        onView(withId(R.id.btnChooseImage)).perform(click())

        // Validate that the Intent was sent with the correct action and type
        Intents.intended(allOf(hasAction(Intent.ACTION_PICK), hasType("image/*")))
    }

    @Test
    fun testAttachPDF() {
        // Mock the PDF selection result
        val resultData = Intent()
        val pdfUri = Uri.parse("content://mock/document.pdf")
        resultData.data = pdfUri
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Specify the intent matcher and respond with the mocked result
        Intents.intending(allOf(hasAction(Intent.ACTION_GET_CONTENT), hasType("application/pdf")))
            .respondWith(result)

        // Click the "Attach PDF" button
        onView(withId(R.id.btnUploadPDF)).perform(click())

        // Validate that the Intent was sent with the correct action and type
        Intents.intended(allOf(hasAction(Intent.ACTION_GET_CONTENT), hasType("application/pdf")))
    }
}
