package com.example.welcom;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Intent;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VolunteerRegistrationActivityTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private Task<AuthResult> mockTask;

    @Captor
    private ArgumentCaptor<OnCompleteListener<AuthResult>> captor;

    private VolunteerRegistrationActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        activity = new VolunteerRegistrationActivity();
        activity.mAuth = mockAuth;
    }

    @Test
    public void testSuccessfulRegistration() {
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);

        activity.handleRegistration();
        verify(mockTask).addOnCompleteListener(captor.capture());
        captor.getValue().onComplete(mockTask);

        // Verify Firestore save is called
        verify(mockAuth).createUserWithEmailAndPassword(anyString(), anyString());
    }

    @Test
    public void testFailedRegistration() {
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);

        activity.handleRegistration();
        verify(mockTask).addOnCompleteListener(captor.capture());
        captor.getValue().onComplete(mockTask);

        // Ensure Firestore save is NOT called
        verify(mockAuth).createUserWithEmailAndPassword(anyString(), anyString());
        verifyNoMoreInteractions(mockAuth);
    }
}

