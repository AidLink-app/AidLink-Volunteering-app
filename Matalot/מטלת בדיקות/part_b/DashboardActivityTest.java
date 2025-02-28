package com.example.welcom;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.view.View;
import android.widget.Button;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DashboardActivityTest {

    @Rule
    public ActivityTestRule<DashboardActivity> activityRule = new ActivityTestRule<>(DashboardActivity.class);

    private DashboardActivity activity;
    private User mockUser;

    @Before
    public void setUp() throws Exception {
        // Get the activity instance from the rule
        activity = activityRule.getActivity();

        // Mock the Firebase User
        mockUser = mock(User.class);

        // Set up the user role, for testing "organization" or "volunteer"
        when(mockUser.getRole()).thenReturn("organization"); // Change to "volunteer" for testing the other case
        UserSession.setUser(mockUser); // Make sure the session is set properly (you may need to implement this)
    }

    @Test
    public void testAddPostButtonVisibilityForOrganization() {
        // Assuming "organization" role is set for the user
        activity.onCreate(null); // Call onCreate() to simulate activity creation
        Button btnAddPost = activity.findViewById(R.id.btnAddPost);

        // Assert that the button is visible for "organization"
        assertEquals(View.VISIBLE, btnAddPost.getVisibility());
    }

    @Test
    public void testAddPostButtonVisibilityForVolunteer() {
        // Assuming "volunteer" role is set for the user
        when(mockUser.getRole()).thenReturn("volunteer");
        activity.onCreate(null); // Call onCreate() to simulate activity creation
        Button btnAddPost = activity.findViewById(R.id.btnAddPost);

        // Assert that the button is gone (hidden) for "volunteer"
        assertEquals(View.GONE, btnAddPost.getVisibility());
    }
}

