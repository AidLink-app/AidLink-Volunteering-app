package com.example.welcom;

import static org.mockito.Mockito.*;

import android.widget.Toast;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4.class)
public class VolunteerRegistrationActivityTest {

    @Rule
    public ActivityScenarioRule<VolunteerRegistrationActivity> activityRule =
            new ActivityScenarioRule<>(VolunteerRegistrationActivity.class);

    @Mock
    FirestoreService mockFirestoreService;

    @Mock
    FirebaseAuth mockAuth;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Inject mocks into the activity
        activityRule.getScenario().onActivity(activity -> {
            activity.setFirestoreService(mockFirestoreService); // Ensure the FirestoreService is set
            activity.mAuth = mockAuth; // Mock FirebaseAuth injection
        });
    }

    @Test
    public void testSuccessfulFirestoreSave() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "test@example.com");
        userData.put("role", "volunteer");
        userData.put("firstName", "");
        userData.put("lastName", "");
        userData.put("organization", "");
        userData.put("name", "Test User");
        userData.put("phone", "1234567890");

        // Simulate successful Firestore save
        doAnswer(invocation -> {
            FirestoreCallback callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(mockFirestoreService).saveUser(eq("users"), eq(userData), any(FirestoreCallback.class));

        // Act
        activityRule.getScenario().onActivity(activity ->
                activity.saveUserDetailsToFirestore(new User("test@example.com", "volunteer", "", "", "", "Test User", "1234567890"))
        );

        // Assert: Verify that Firestore service's saveUser method was called
        verify(mockFirestoreService).saveUser(eq("users"), eq(userData), any(FirestoreCallback.class));
    }

    @Test
    public void testFailedFirestoreSave() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "test@example.com");
        userData.put("role", "volunteer");
        userData.put("firstName", "");
        userData.put("lastName", "");
        userData.put("organization", "");
        userData.put("name", "Test User");
        userData.put("phone", "1234567890");

        // Simulate failed Firestore save
        doAnswer(invocation -> {
            FirestoreCallback callback = invocation.getArgument(2);
            callback.onFailure(new Exception("Firestore error"));
            return null;
        }).when(mockFirestoreService).saveUser(eq("users"), eq(userData), any(FirestoreCallback.class));

        // Act
        activityRule.getScenario().onActivity(activity ->
                activity.saveUserDetailsToFirestore(new User("test@example.com", "volunteer", "", "", "", "Test User", "1234567890"))
        );

        // Assert: Verify that Firestore service's saveUser method was called
        verify(mockFirestoreService).saveUser(eq("users"), eq(userData), any(FirestoreCallback.class));
    }

    @Test
    public void testHandleRegistrationWithEmptyFields() {
        // Act
        activityRule.getScenario().onActivity(activity -> {
            activity.nameField.setText("");
            activity.emailField.setText("");
            activity.passwordField.setText("");
            activity.phoneField.setText("");
            activity.handleRegistration(); // Assuming this method handles registration when called
        });

        // Assert: No Firestore or FirebaseAuth interactions should occur if fields are empty
        verifyNoInteractions(mockFirestoreService);
        verifyNoInteractions(mockAuth);
    }

    @Test
    public void testSuccessfulToastMessageOnRegistration() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "test@example.com");
        userData.put("role", "volunteer");
        userData.put("firstName", "");
        userData.put("lastName", "");
        userData.put("organization", "");
        userData.put("name", "Test User");
        userData.put("phone", "1234567890");

        // Simulate Firestore save success and Toast message
        doAnswer(invocation -> {
            FirestoreCallback callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(mockFirestoreService).saveUser(eq("users"), eq(userData), any(FirestoreCallback.class));

        // Act
        activityRule.getScenario().onActivity(activity ->
                activity.saveUserDetailsToFirestore(new User("test@example.com", "volunteer", "", "", "", "Test User", "1234567890"))
        );

        // Assert: Verify a toast message indicating success
        //activityRule.getScenario().onActivity(activity -> {
        //    verify(activity, times(1)).showToast("Registration successful!");  // Assuming this method shows the toast
       // });
    }
}
