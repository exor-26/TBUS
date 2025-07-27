package com.example.tbus; // Replace with your actual package name

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target; // Add this for loading the profile picture

public class ThirdFragment extends Fragment {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private SignInButton signInButton;
    private Button btnHelpSupport, btnPrivacyPolicy, btnLogOut;
    private ImageView ivProfilePicture;  // ImageView for profile picture
    private TextView tvUserName;  // TextView for user name

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpage3, container, false);

        // Initialize FirebaseAuth directly in the fragment
        auth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.your_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Initialize views
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);  // Set ImageView
        tvUserName = view.findViewById(R.id.tvUserName);  // Set TextView
        signInButton = view.findViewById(R.id.sign_in_button);
        btnHelpSupport = view.findViewById(R.id.btnHelpSupport);
        btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        btnLogOut = view.findViewById(R.id.btnLogOut);

        SharedPreferences preferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedUserName = preferences.getString("user_name", "");
        String savedProfilePicUrl = preferences.getString("profile_pic_url", "");

        // Set saved user name
        tvUserName.setText(savedUserName);

        // Load profile picture from URL if saved
        if (!savedProfilePicUrl.isEmpty()) {
            Picasso.get().load(savedProfilePicUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ivProfilePicture.setImageBitmap(getCircularBitmap(bitmap));  // Apply circular mask
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    ivProfilePicture.setImageResource(R.drawable.ic_placeholder_profile);  // Fallback if loading fails
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    ivProfilePicture.setImageResource(R.drawable.ic_placeholder_profile);  // Placeholder before loading
                }
            });
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_placeholder_profile);  // Default profile picture
        }

        // Set click listeners
        signInButton.setOnClickListener(v -> signInWithGoogle());
        btnHelpSupport.setOnClickListener(v -> openHelpSupport());
        btnPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        btnLogOut.setOnClickListener(v -> logOut());

        return view;
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        updateUI(user);  // Call to update profile info after sign-in
                    } else {
                        Toast.makeText(getContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Set user name
            tvUserName.setText(user.getDisplayName());

            // Save the user name and profile picture URL to SharedPreferences
            SharedPreferences preferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_name", user.getDisplayName());
            editor.putString("profile_pic_url", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
            editor.apply();

            // Load and apply circular profile picture using Picasso
            Picasso.get().load(user.getPhotoUrl()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ivProfilePicture.setImageBitmap(getCircularBitmap(bitmap));  // Apply circular mask
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    ivProfilePicture.setImageResource(R.drawable.ic_placeholder_profile);  // Fallback if loading fails
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    ivProfilePicture.setImageResource(R.drawable.ic_placeholder_profile);  // Placeholder before loading
                }
            });

            Toast.makeText(getContext(), "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);

        // Create a square bitmap that fits within a circle
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Define paint for the circular effect
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Create a circular path and clip the canvas
        Path path = new Path();
        path.addCircle(diameter / 2, diameter / 2, diameter / 2, Path.Direction.CW);
        canvas.clipPath(path);

        // Draw the bitmap into the canvas within the circular clip
        canvas.drawBitmap(bitmap, (diameter - width) / 2, (diameter - height) / 2, paint);

        return output;
    }
    private void openHelpSupport() {
        // Handle Help and Support action
        Toast.makeText(getContext(), "Help and Support Clicked", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        // Handle Privacy Policy action
        Toast.makeText(getContext(), "Privacy Policy Clicked", Toast.LENGTH_SHORT).show();
    }

    private void logOut() {
        // Create an AlertDialog to confirm logout action
        new AlertDialog.Builder(getContext())
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    // Proceed with logout
                    auth.signOut();
                    googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                        // Clear SharedPreferences on logout
                        SharedPreferences preferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();  // Clear saved data
                        editor.apply();

                        // Reset UI
                        ivProfilePicture.setImageResource(R.drawable.ic_placeholder_profile);  // Reset profile picture
                        tvUserName.setText("");  // Reset username

                        Toast.makeText(getContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // Dismiss the dialog if "No" is clicked
                    dialog.dismiss();
                })
                .show();
    }
}