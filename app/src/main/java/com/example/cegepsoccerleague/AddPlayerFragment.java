package com.example.cegepsoccerleague;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddPlayerFragment extends Fragment implements View.OnClickListener{

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Context context;
    public NavController HomeNavController;
    private ImageView add_player_icon_img_view;
    private TextView add_player_icon_txt;
    private MaterialCardView add_player_icon_cv;
    private TextInputLayout player_first_name_layout, player_last_name_layout, player_age_layout, player_position_layout;
    private MaterialButton add_player_btn;
    public Uri image_uri;
    private String team_id = "",player_id="";
    public Toolbar HomeToolbar;
    private boolean update_has_image = false;
    private static ProgressDialog progressDialog;


    public AddPlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity().getApplicationContext();
        HomeNavController = Navigation.findNavController(getActivity(), R.id.home_host_fragment);
        HomeToolbar = getActivity().findViewById(R.id.home_toolbar);

        add_player_icon_img_view = view.findViewById(R.id.add_player_icon_img_view);
        add_player_icon_txt = view.findViewById(R.id.add_player_icon_txt);
        add_player_icon_cv = view.findViewById(R.id.add_player_icon_cv);
        player_first_name_layout = view.findViewById(R.id.player_first_name_layout);
        player_last_name_layout = view.findViewById(R.id.player_last_name_layout);
        player_age_layout = view.findViewById(R.id.player_age_layout);
        player_position_layout = view.findViewById(R.id.player_position_layout);
        add_player_btn = view.findViewById(R.id.add_player_btn);

        add_player_btn.setOnClickListener(this);
        add_player_icon_cv.setOnClickListener(this);
        add_player_icon_txt.setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        //Get Current User refernece
        user = mAuth.getCurrentUser();

        if(getArguments() != null){
            if(getArguments().getString("from")!=null && getArguments().getString("from").equals("update player")){
                HomeToolbar.setTitle("Update Player");
                add_player_btn.setText("Update Player Info");
                player_id = getArguments().getString("player_id");
                team_id = getArguments().getString("team_id");
                player_first_name_layout.getEditText().setText(getArguments().getString("first_name"));
                player_last_name_layout.getEditText().setText(getArguments().getString("last_name"));
                player_age_layout.getEditText().setText(getArguments().getString("age"));
                player_position_layout.getEditText().setText(getArguments().getString("position"));
                if(!getArguments().getString("player_icon").equals("No Icon")){
                    update_has_image = true;
                    byte[] decodedString = Base64.decode(getArguments().getString("player_icon"), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    add_player_icon_img_view.setImageBitmap(decodedByte);
                }

            }
            else {
                team_id = getArguments().getString("team_id");
            }
        }

    }

    @Override
    public void onClick(View view) {
        if (view == add_player_icon_cv | view == add_player_icon_txt){
            if(update_has_image){
                RemovePhotoDialog();
            }
            else if(image_uri!=null){
                RemovePhotoDialog();
            }
            else {
                SelectProfilePic();
            }
        }
        else if(view == add_player_btn){
            player_first_name_layout.setErrorEnabled(false);
            player_last_name_layout.setErrorEnabled(false);
            player_age_layout.setErrorEnabled(false);
            player_position_layout.setErrorEnabled(false);

            player_first_name_layout.setError(null);
            player_last_name_layout.setError(null);
            player_age_layout.setError(null);
            player_position_layout.setError(null);
            if(TextUtils.isEmpty(player_first_name_layout.getEditText().getText().toString().trim())){
                player_first_name_layout.setError("Required fields are missing!");
            }
            else if(TextUtils.isEmpty(player_last_name_layout.getEditText().getText().toString().trim())){
                player_last_name_layout.setError("Required fields are missing!");
            }
            else if(TextUtils.isEmpty(player_age_layout.getEditText().getText().toString().trim())){
                player_age_layout.setError("Required fields are missing!");
            }
            else if(!TextUtils.isDigitsOnly(player_age_layout.getEditText().getText().toString().trim())){
                player_age_layout.setError("Please enter valid age!");
            }
            else if(TextUtils.isEmpty(player_position_layout.getEditText().getText().toString().trim())){
                player_position_layout.setError("Required fields are missing!");
            }
            else {
                player_first_name_layout.setErrorEnabled(false);
                player_last_name_layout.setErrorEnabled(false);
                player_age_layout.setErrorEnabled(false);
                player_position_layout.setErrorEnabled(false);
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCancelable(false);

                if(getArguments().getString("from")!=null && getArguments().getString("from").equals("update player")){
                    progressDialog.setMessage("Updating Player");
                }
                else {
                    progressDialog.setMessage("Creating New Player");
                }
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setProgress(0);
                progressDialog.show();
                add_player_btn.setEnabled(false);
                if(image_uri!=null){
                    new EncodeImage(image_uri).execute();
                }
                else if(update_has_image){
                    String encoded_league_icon = getArguments().getString("player_icon");
                    AddPlayer(encoded_league_icon);
                }
                else {
                    String encoded_league_icon = "No Icon";
                    AddPlayer(encoded_league_icon);
                }
            }
        }
    }

    private void RemovePhotoDialog() {
        final CharSequence[] options = {"Remove Photo", "Select Other Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Remove Photo")) {
                    image_uri = null;
                    update_has_image = false;
                    add_player_icon_img_view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.add_player_icon));
                } else if (options[item].equals("Select Other Photo")) {
                    dialog.dismiss();
                    SelectProfilePic();

                } else if (options[item].equals("Cancel")) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /*-------- Below Code is for selecting image from galary or camera -----------*/

    private void SelectProfilePic() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permission, 1000);
                        } else {
                            openCamera();
                        }
                    } else {
                        openCamera();
                    }
                } else if (options[item].equals("Choose from Gallery")) {

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");

                    startActivityForResult(Intent.createChooser(intent,"Select Image"), 2);

                } else if (options[item].equals("Cancel")) {

                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(takePictureIntent, 1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    //permisiion from pop up was denied.
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied...", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    add_player_icon_img_view.setImageURI(image_uri);
                    break;
                case 2:
                    //data.getData returns the content URI for the selected Image
                    image_uri = data.getData();
                    add_player_icon_img_view.setImageURI(image_uri);
                    break;
            }

        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /*---------- Below Code is for converting image into Base64 Format to store in database as string ---------*/
    public class EncodeImage extends AsyncTask<Void, Void, String> {
        Uri uri;
        String encodedImage = "";
        public EncodeImage(Uri uri){
            this.uri = uri;
        }
        @Override
        protected String doInBackground(Void... params){

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image_uri);
                InputStream input = context.getContentResolver().openInputStream(image_uri);
                ExifInterface ei;
                if (Build.VERSION.SDK_INT > 23) {
                    ei = new ExifInterface(input);
                }
                else {
                    ei = new ExifInterface(image_uri.getPath());
                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap rotatedBitmap = null;
                switch(orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return encodedImage;

        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(!result.equals("")) {
                String encoded_league_icon = result;
                AddPlayer(encoded_league_icon);

            }
            else {
                progressDialog.dismiss();
                Toast.makeText(context,"Something Went Wrong!\nPlease Try Again..",Toast.LENGTH_LONG).show();
            }
        }

    }

    // Adding Player in database
    private void AddPlayer(final String encoded_league_icon) {

        final String first_name = player_first_name_layout.getEditText().getText().toString().trim();
        final String last_name = player_last_name_layout.getEditText().getText().toString().trim();
        final String age = player_age_layout.getEditText().getText().toString().trim();
        final String position = player_position_layout.getEditText().getText().toString().trim();

        //Creating a data object to add in database
        final Map<String, Object> player_data = new HashMap<>();
        player_data.put("first_name", first_name);
        player_data.put("last_name", last_name);
        player_data.put("age",age);
        player_data.put("position", position);
        player_data.put("team_id",team_id);
        player_data.put("player_icon",encoded_league_icon);

        if(getArguments().getString("from")!=null && getArguments().getString("from").equals("update player")) {
            db.collection("players").document(player_id)
                    .set(player_data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            add_player_btn.setEnabled(true);
                            progressDialog.dismiss();
                            Toast.makeText(context, "Player Updated Successfully!", Toast.LENGTH_LONG).show();
                            HomeNavController.popBackStack();
                            HomeNavController.popBackStack();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            add_player_btn.setEnabled(true);
                            progressDialog.dismiss();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
        else {
            // Add a new document with auto generated ID
            db.collection("players")
                    .add(player_data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            add_player_btn.setEnabled(true);
                            progressDialog.dismiss();
                            Toast.makeText(context, "Player Added Successfully!", Toast.LENGTH_LONG).show();
                            HomeNavController.popBackStack();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            add_player_btn.setEnabled(true);
                            progressDialog.dismiss();
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }
}
